package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.entity.enums.RoleEnum;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrarAuditoria(String operacao, String entidade, String idEntidade, String campo, String valorAntigo, String valorNovo, String justificativa) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuarioLogado = (auth != null) ? auth.getName() : "SISTEMA";
        RoleEnum role = null;

        if (auth != null && !auth.getAuthorities().isEmpty()) {
            try {
                String authority = auth.getAuthorities().stream().findFirst().get().getAuthority().replace("ROLE_", "");
                role = RoleEnum.valueOf(authority);
            } catch (Exception e) {
                role = null;
            }
        }

        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuarioLogado);
        auditoria.setRole(role);
        auditoria.setDataHora(LocalDateTime.now());
        auditoria.setOperacao(operacao);
        auditoria.setEntidadeAfetada(entidade);
        auditoria.setIdEntidade(idEntidade);
        auditoria.setCampoModificado(campo);
        auditoria.setValorAnterior(valorAntigo);
        auditoria.setValorNovo(valorNovo);
        auditoria.setJustificativa(justificativa);

        auditoriaRepository.save(auditoria);
    }
}