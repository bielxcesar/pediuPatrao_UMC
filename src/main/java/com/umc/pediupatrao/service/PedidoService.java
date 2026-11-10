package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.enums.StatusPedidoEnum;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    // 1. Criar novo pedido
    public Pedido criarPedido(Pedido pedido) {
        if (pedido.getStatus() == null) {
            pedido.setStatus(StatusPedidoEnum.RECEBIDO);
        }
        if (pedido.getDataHoraEntrada() == null) {
            pedido.setDataHoraEntrada(LocalDateTime.now());
        }

        Pedido salvo = pedidoRepository.save(pedido);

        // Audita abertura do pedido no MongoDB (RF05)
        auditoriaService.registrarAuditoria("INSERIR", "Pedido", salvo.getId(), "Todos",
                null, "Pedido #" + salvo.getId() + " - Total: R$ " + salvo.getValorTotal(), "Abertura de novo pedido");

        return salvo;
    }

    // Limita a fila de atendimento sem apagar o histórico.
    public List<Pedido> listarPedidos() {
        List<Pedido> fila = new ArrayList<>(pedidoRepository.findByStatusNotIn(
                List.of(StatusPedidoEnum.FINALIZADO, StatusPedidoEnum.CANCELADO)));
        Comparator<Pedido> porEntrada = Comparator.comparing(
                Pedido::getDataHoraEntrada, Comparator.nullsLast(Comparator.naturalOrder()));
        fila.sort(porEntrada);
        pedidoRepository.findTop6ByStatusOrderByDataHoraSaidaDescDataHoraEntradaDescIdDesc(
                StatusPedidoEnum.FINALIZADO).stream().sorted(porEntrada).forEach(fila::add);
        return fila;
    }

    // Atualiza o status e registra a mudança na auditoria.
    public Pedido atualizarStatus(String id, StatusPedidoEnum novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        StatusPedidoEnum statusAntigo = pedido.getStatus();
        if (statusAntigo == StatusPedidoEnum.CANCELADO) {
            throw new IllegalStateException("O pedido está cancelado.");
        }
        pedido.setStatus(novoStatus);

        if (novoStatus == StatusPedidoEnum.A_CAMINHO || novoStatus == StatusPedidoEnum.FINALIZADO) {
            pedido.setDataHoraSaida(LocalDateTime.now());
        }

        Pedido salvo = pedidoRepository.save(pedido);

        // Auditoria no MongoDB (RF05)
        auditoriaService.registrarAuditoria("ATUALIZAR", "Pedido", salvo.getId(), "status",
                statusAntigo != null ? statusAntigo.name() : "NULO", novoStatus.name(), "Alteração de status do pedido");

        return salvo;
    }

    // 4. Cancelamento de Pedido (Audita e remove da lista de atendimento)
    public void cancelarPedido(String id, String justificativa) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        if (pedido.getStatus() == StatusPedidoEnum.CANCELADO) {
            return;
        }

        // Impede cancelamento se já saiu para entrega ou finalizado
        if (pedido.getStatus() == StatusPedidoEnum.A_CAMINHO || pedido.getStatus() == StatusPedidoEnum.FINALIZADO) {
            throw new RuntimeException("Não é possível cancelar um pedido que já saiu para entrega ou foi finalizado.");
        }

        // REGISTRA A AUDITORIA COMPLETA NO MONGODB (Exigência RF04/RF05)
        auditoriaService.registrarAuditoria("CANCELAMENTO", "Pedido", pedido.getId(), "status",
                pedido.getStatus() == null ? null : pedido.getStatus().name(), "CANCELADO", "Justificativa: " + justificativa);

        pedido.setStatus(StatusPedidoEnum.CANCELADO);
        pedidoRepository.save(pedido);
    }
}
