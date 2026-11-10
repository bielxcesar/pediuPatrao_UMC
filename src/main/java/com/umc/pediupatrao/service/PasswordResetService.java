package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String solicitarCodigo(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return "E-mail não encontrado no sistema.";
        }

        Usuario usuario = usuarioOpt.get();

        // Verifica se a conta está temporariamente bloqueada por excesso de erros
        if (usuario.getBloqueioCodigoAte() != null && LocalDateTime.now().isBefore(usuario.getBloqueioCodigoAte())) {
            long minutosRestantes = Duration.between(LocalDateTime.now(), usuario.getBloqueioCodigoAte()).toMinutes() + 1;
            return "Aguarde " + minutosRestantes + " minuto(s) para solicitar um novo código (bloqueado por excesso de tentativas).";
        }

        String codigo = String.format("%06d", new Random().nextInt(999999));
        usuario.setCodigoRecuperacao(codigo);
        usuario.setExpiracaoCodigo(LocalDateTime.now().plusMinutes(15));
        usuario.setTentativasCodigo(0);
        usuarioRepository.save(usuario);

        System.out.println("CÓDIGO DE RECUPERAÇÃO PARA [" + email + "]: " + codigo);

        return "OK";
    }

    public String validarCodigo(String email, String codigo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return "E-mail não encontrado.";
        }

        Usuario usuario = usuarioOpt.get();

        // Verifica se já está bloqueado
        if (usuario.getBloqueioCodigoAte() != null && LocalDateTime.now().isBefore(usuario.getBloqueioCodigoAte())) {
            long minutos = Duration.between(LocalDateTime.now(), usuario.getBloqueioCodigoAte()).toMinutes() + 1;
            return "Acesso bloqueado por " + minutos + " minuto(s) devido a 5 tentativas incorretas.";
        }

        // Verifica se o código expirou (15 minutos)
        if (usuario.getExpiracaoCodigo() == null || LocalDateTime.now().isAfter(usuario.getExpiracaoCodigo())) {
            return "O código temporário expirou. Solicite um novo.";
        }

        // Valida se o código está correto
        if (usuario.getCodigoRecuperacao() == null || !usuario.getCodigoRecuperacao().equals(codigo)) {
            int tentativas = usuario.getTentativasCodigo() + 1;
            usuario.setTentativasCodigo(tentativas);

            if (tentativas >= 5) {
                usuario.setBloqueioCodigoAte(LocalDateTime.now().plusMinutes(5));
                usuario.setTentativasCodigo(0);
                usuarioRepository.save(usuario);
                return "Você errou o código 5 vezes. Acesso bloqueado por 5 minutos.";
            }

            usuarioRepository.save(usuario);
            int restantes = 5 - tentativas;
            return "Código incorreto. Você tem mais " + restantes + " tentativa(s) antes do bloqueio.";
        }

        usuario.setTentativasCodigo(0);
        usuario.setBloqueioCodigoAte(null);
        usuarioRepository.save(usuario);

        return "OK";
    }

    public String redefinirSenha(String email, String codigo, String novaSenha, String confirmarSenha) {
        if (!novaSenha.equals(confirmarSenha)) {
            return "As senhas digitadas não coincidem.";
        }

        if (novaSenha.length() < 6) {
            return "A nova senha deve ter no mínimo 6 caracteres.";
        }

        String validacao = validarCodigo(email, codigo);
        if (!"OK".equals(validacao)) {
            return validacao;
        }

        Usuario usuario = usuarioRepository.findByEmail(email).get();
        usuario.setPassword(passwordEncoder.encode(novaSenha));

        // Limpa as credenciais temporárias
        usuario.setCodigoRecuperacao(null);
        usuario.setExpiracaoCodigo(null);
        usuario.setTentativasCodigo(0);
        usuario.setBloqueioCodigoAte(null);

        usuarioRepository.save(usuario);
        return "OK";
    }
}