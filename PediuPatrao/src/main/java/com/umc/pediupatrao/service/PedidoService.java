package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.enums.StatusPedidoEnum;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    // 2. Listagem Inteligente (Fila ativa no topo, max 6 finalizados, sem cancelados)
    public List<Pedido> listarPedidos() {
        limparPedidosCancelados();
        limparFinalizadosExcedentes();

        List<Pedido> todos = pedidoRepository.findAll();

        // Ordenação customizada:
        // Prioridade 1: Pedidos em andamento (RECEBIDO, EM_PREPARACAO, PRONTO, SAIU_ENTREGA)
        // Prioridade 2: Pedidos FINALIZADOS no final da lista
        return todos.stream()
                .sorted(Comparator.comparingInt((Pedido p) -> {
                    if (p.getStatus() == StatusPedidoEnum.FINALIZADO) {
                        return 2; // Fica por último
                    }
                    return 1; // Fica no topo
                }).thenComparing(Pedido::getDataHoraEntrada, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    // Transição de Status com regra do carrossel
    public Pedido atualizarStatus(String id, StatusPedidoEnum novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        StatusPedidoEnum statusAntigo = pedido.getStatus();
        pedido.setStatus(novoStatus);

        if (novoStatus == StatusPedidoEnum.A_CAMINHO || novoStatus == StatusPedidoEnum.FINALIZADO) {
            pedido.setDataHoraSaida(LocalDateTime.now());
        }

        Pedido salvo = pedidoRepository.save(pedido);

        // Auditoria no MongoDB (RF05)
        auditoriaService.registrarAuditoria("ATUALIZAR", "Pedido", salvo.getId(), "status",
                statusAntigo != null ? statusAntigo.name() : "NULO", novoStatus.name(), "Alteração de status do pedido");

        // Se acabou de ser finalizado, executa a faxina mantendo no máximo 6 finalizados
        if (novoStatus == StatusPedidoEnum.FINALIZADO) {
            limparFinalizadosExcedentes();
        }

        return salvo;
    }

    // 4. Cancelamento de Pedido (Audita e remove da lista de atendimento)
    public void cancelarPedido(String id, String justificativa) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        // Impede cancelamento se já saiu para entrega ou finalizado
        if (pedido.getStatus() == StatusPedidoEnum.A_CAMINHO || pedido.getStatus() == StatusPedidoEnum.FINALIZADO) {
            throw new RuntimeException("Não é possível cancelar um pedido que já saiu para entrega ou foi finalizado.");
        }

        // REGISTRA A AUDITORIA COMPLETA NO MONGODB (Exigência RF04/RF05)
        auditoriaService.registrarAuditoria("CANCELAMENTO", "Pedido", pedido.getId(), "status",
                pedido.getStatus().name(), "CANCELADO", "Justificativa: " + justificativa);

        // Remove o pedido da tabela da fila de atendimento
        pedidoRepository.deleteById(id);
    }


    // Mantém no máximo os 6 pedidos finalizados mais recentes
    private void limparFinalizadosExcedentes() {
        List<Pedido> finalizados = pedidoRepository.findAll().stream()
                .filter(p -> p.getStatus() == StatusPedidoEnum.FINALIZADO)
                .sorted(Comparator.comparing(Pedido::getDataHoraSaida, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Pedido::getDataHoraEntrada, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        // Se houver mais de 6 finalizados, deleta os mais antigos (FIFO)
        if (finalizados.size() > 6) {
            int excedentes = finalizados.size() - 6;
            for (int i = 0; i < excedentes; i++) {
                pedidoRepository.deleteById(finalizados.get(i).getId());
            }
        }
    }

    // Deleta registros com status CANCELADO mantendo a tela limpa
    private void limparPedidosCancelados() {
        List<Pedido> cancelados = pedidoRepository.findAll().stream()
                .filter(p -> p.getStatus() == StatusPedidoEnum.CANCELADO)
                .collect(Collectors.toList());

        for (Pedido c : cancelados) {
            pedidoRepository.deleteById(c.getId());
        }
    }
}