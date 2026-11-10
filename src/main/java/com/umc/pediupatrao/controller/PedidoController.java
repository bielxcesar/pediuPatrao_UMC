package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.enums.StatusPedidoEnum;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteService clienteService;

    // 1. Listagem de Pedidos com Fila e Rotação Automática
    @GetMapping
    public String listarPedidosPagina(Model model) {
        List<Pedido> pedidos = pedidoService.listarPedidos();

        for (Pedido p : pedidos) {
            if ((p.getClienteNome() == null || p.getClienteNome().trim().isEmpty()) && p.getClienteId() != null) {
                clienteService.buscarPorId(p.getClienteId())
                        .ifPresent(cliente -> p.setClienteNome(cliente.getNome()));
            }

            if (p.getValorTotal() == null || p.getValorTotal().compareTo(BigDecimal.ZERO) == 0) {
                if (p.getValorUnitario() != null && p.getQuantidade() != null) {
                    BigDecimal subtotal = p.getValorUnitario().multiply(new BigDecimal(p.getQuantidade()));
                    BigDecimal desc = (p.getDesconto() != null) ? p.getDesconto() : BigDecimal.ZERO;
                    p.setValorTotal(subtotal.subtract(desc));
                }
            }
        }

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("content", "pedidos :: content");
        return "layout";
    }

    // 2. Formulário para Novo Pedido
    @GetMapping("/novo")
    public String novoPedidoForm(Model model) {
        model.addAttribute("pedido", new Pedido());
        model.addAttribute("clientes", clienteService.listarClientes());
        return "pedidos-form";
    }

    // 3. Salvar Pedido
    @PostMapping("/salvar")
    public String salvarPedido(@ModelAttribute Pedido pedido,
                               @RequestParam(value = "percentualDesconto", required = false) BigDecimal percentualDesconto,
                               RedirectAttributes redirectAttributes) {
        try {
            if (pedido.getClienteId() != null && !pedido.getClienteId().isEmpty()) {
                clienteService.buscarPorId(pedido.getClienteId())
                        .ifPresent(cliente -> pedido.setClienteNome(cliente.getNome()));
            }

            if (percentualDesconto != null && percentualDesconto.compareTo(BigDecimal.ZERO) > 0) {
                if (percentualDesconto.compareTo(new BigDecimal("20")) > 0) {
                    throw new RuntimeException("O desconto máximo permitido é de 20%.");
                }
                if (pedido.getValorUnitario() != null && pedido.getQuantidade() != null) {
                    BigDecimal subtotal = pedido.getValorUnitario().multiply(new BigDecimal(pedido.getQuantidade()));
                    BigDecimal valorDesconto = subtotal.multiply(percentualDesconto).divide(new BigDecimal("100"));
                    pedido.setDesconto(valorDesconto);
                }
            } else {
                pedido.setDesconto(BigDecimal.ZERO);
            }

            pedidoService.criarPedido(pedido);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido registrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao registrar pedido: " + e.getMessage());
        }
        return "redirect:/pedidos";
    }

    // 4. Alteração de Status
    @PostMapping("/{id}/status")
    public String atualizarStatus(@PathVariable String id,
                                  @RequestParam("status") String statusStr,
                                  RedirectAttributes redirectAttributes) {
        try {
            StatusPedidoEnum novoStatus = StatusPedidoEnum.valueOf(statusStr);
            pedidoService.atualizarStatus(id, novoStatus);
            redirectAttributes.addFlashAttribute("sucesso", "Status atualizado para: " + novoStatus);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao alterar status: " + e.getMessage());
        }
        return "redirect:/pedidos";
    }

    // 5. Cancelamento de Pedido (Exclusivo Gerente)
    @PostMapping("/{id}/cancelar")
    public String cancelarPedido(@PathVariable String id,
                                 @RequestParam("justificativa") String justificativa,
                                 RedirectAttributes redirectAttributes) {
        try {
            pedidoService.cancelarPedido(id, justificativa);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido cancelado com sucesso e auditado!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cancelar pedido: " + e.getMessage());
        }
        return "redirect:/pedidos";
    }
}