package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.service.PasswordResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/esqueci-senha")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping
    public String abrirTelaEsqueciSenha() {
        return "esqueci-senha";
    }

    @PostMapping("/solicitar")
    public String solicitarCodigo(@RequestParam("email") String email, Model model) {
        String resultado = passwordResetService.solicitarCodigo(email);
        if (!"OK".equals(resultado)) {
            model.addAttribute("erro", resultado);
            return "esqueci-senha";
        }
        model.addAttribute("email", email);
        model.addAttribute("sucesso", "Código de verificação gerado! Confira o console.");
        return "validar-codigo";
    }

    @PostMapping("/validar")
    public String validarCodigo(@RequestParam("email") String email,
                                @RequestParam("codigo") String codigo,
                                Model model) {
        String resultado = passwordResetService.validarCodigo(email, codigo);
        if (!"OK".equals(resultado)) {
            model.addAttribute("email", email);
            model.addAttribute("erro", resultado);
            return "validar-codigo";
        }
        model.addAttribute("email", email);
        model.addAttribute("codigo", codigo);
        return "nova-senha";
    }

    @PostMapping("/redefinir")
    public String redefinirSenha(@RequestParam("email") String email,
                                 @RequestParam("codigo") String codigo,
                                 @RequestParam("novaSenha") String novaSenha,
                                 @RequestParam("confirmarSenha") String confirmarSenha,
                                 Model model) {
        String resultado = passwordResetService.redefinirSenha(email, codigo, novaSenha, confirmarSenha);
        if (!"OK".equals(resultado)) {
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
            model.addAttribute("erro", resultado);
            return "nova-senha";
        }
        return "redirect:/login?senhaRedefinida=true";
    }
}