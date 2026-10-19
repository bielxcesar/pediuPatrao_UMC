package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.repository.AuditoriaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(AuditoriaRepository auditoriaRepository){
        this.auditoriaRepository = auditoriaRepository;
    }

    @GetMapping
    public String listarAuditoria(Model model){
        model.addAttribute("registros", auditoriaRepository.findAll());
        return "auditoria/lista";
    }
}