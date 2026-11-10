package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.enums.StatusPedidoEnum;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardService {
    private final PedidoRepository pedidos;
    private final ProdutoRepository produtos;

    public DashboardService(PedidoRepository pedidos, ProdutoRepository produtos) {
        this.pedidos = pedidos;
        this.produtos = produtos;
    }

    public Resumo consultar(LocalDate inicio, LocalDate fim) {
        List<Pedido> periodo = pedidos.findNoPeriodo(
                inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
        Map<StatusPedidoEnum, Long> situacao = new EnumMap<>(StatusPedidoEnum.class);
        for (StatusPedidoEnum status : StatusPedidoEnum.values()) {
            situacao.put(status, 0L);
        }

        Map<LocalDate, BigDecimal> vendas = new LinkedHashMap<>();
        inicio.datesUntil(fim.plusDays(1)).forEach(dia -> vendas.put(dia, BigDecimal.ZERO));
        Map<ProdutoVendido, Long> quantidades = new HashMap<>();
        BigDecimal faturamento = BigDecimal.ZERO;
        long finalizados = 0;
        long semStatus = 0;
        for (Pedido pedido : periodo) {
            StatusPedidoEnum status = pedido.getStatus();
            if (status == null) {
                semStatus++;
                continue;
            }
            situacao.merge(status, 1L, Long::sum);
            if (status != StatusPedidoEnum.FINALIZADO) {
                continue;
            }
            finalizados++;
            BigDecimal total = Objects.requireNonNullElse(pedido.getValorTotal(), BigDecimal.ZERO);
            faturamento = faturamento.add(total);
            vendas.merge(pedido.getDataHoraEntrada().toLocalDate(), total, BigDecimal::add);
            contarProdutos(pedido, quantidades);
        }

        BigDecimal ticket = finalizados == 0 ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(finalizados), 2, RoundingMode.HALF_UP);
        List<VendaDiaria> serie = vendas.entrySet().stream()
                .map(dia -> new VendaDiaria(dia.getKey(), dia.getValue())).toList();
        return new Resumo(inicio, fim, periodo.size(), finalizados, faturamento, ticket, situacao,
                semStatus, serie, ranking(quantidades));
    }

    private void contarProdutos(Pedido pedido, Map<ProdutoVendido, Long> quantidades) {
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            String sabor = pedido.getSabor();
            if (sabor != null && !sabor.isBlank() && pedido.getQuantidade() != null && pedido.getQuantidade() > 0) {
                quantidades.merge(new ProdutoVendido(null, sabor.strip()), pedido.getQuantidade().longValue(), Long::sum);
            }
            return;
        }
        for (ItemPedido item : pedido.getItens()) {
            if (item != null && item.getProdutoId() != null && !item.getProdutoId().isBlank()
                    && item.getQuantidade() != null && item.getQuantidade() > 0) {
                quantidades.merge(new ProdutoVendido(item.getProdutoId(), null), item.getQuantidade().longValue(), Long::sum);
            }
        }
    }

    private List<RankingProduto> ranking(Map<ProdutoVendido, Long> quantidades) {
        var top = quantidades.entrySet().stream()
                .sorted(Map.Entry.<ProdutoVendido, Long>comparingByValue().reversed()
                        .thenComparing(item -> item.getKey().id(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(item -> item.getKey().sabor(), Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(10).toList();
        List<String> ids = top.stream().map(item -> item.getKey().id()).filter(Objects::nonNull).toList();
        Map<String, String> nomes = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Produto produto : produtos.findAllById(ids)) {
                nomes.put(produto.getId(), produto.getNome());
            }
        }
        return top.stream().map(item -> {
            ProdutoVendido produto = item.getKey();
            String nome = produto.id() == null ? produto.sabor() : nomes.get(produto.id());
            if (nome == null || nome.isBlank()) {
                nome = "Produto " + produto.id();
            }
            return new RankingProduto(produto.id(), nome, item.getValue());
        }).toList();
    }

    private record ProdutoVendido(String id, String sabor) {}
    public record VendaDiaria(LocalDate data, BigDecimal faturamento) {}
    public record RankingProduto(String produtoId, String nome, long quantidade) {}
    public record Resumo(LocalDate inicio, LocalDate fim, long totalPedidos, long pedidosFinalizados,
                         BigDecimal faturamento, BigDecimal ticketMedio, Map<StatusPedidoEnum, Long> situacao,
                         long pedidosSemStatus, List<VendaDiaria> vendasDiarias, List<RankingProduto> produtosMaisVendidos) {}
}
