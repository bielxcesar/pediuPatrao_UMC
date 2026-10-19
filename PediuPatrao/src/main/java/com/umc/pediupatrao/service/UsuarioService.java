package com.umc.pediupatrao.service;

import com.mongodb.internal.connection.Authenticator;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Busca um usuário por ID
    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    // Busca um usuário por username
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    // Salva um novo usuário (criptografando a senha)
    public void salvarUsuario(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); // Criptografa a senha
        usuarioRepository.save(usuario);
    }

    // Deleta um usuário pelo ID
    public void deletarUsuario(String id) {
        usuarioRepository.deleteById(id);
    }

    // Lista todos os usuários
    public Iterable<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // Atualiza o usuário
    public void atualizarUsuario(String id, Usuario usuarioAtualizado) {
        usuarioRepository.findById(id).ifPresent(usuarioExistente -> {
            usuarioExistente.setUsername(usuarioAtualizado.getUsername());

            if (usuarioAtualizado.getRole() != null && !usuarioAtualizado.getRole().equals(usuarioExistente.getRole())) {
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

                // CORREÇÃO: Verifica tanto "ADMIN" quanto "ROLE_ADMIN"
                boolean isAdmin = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));

                if (!isAdmin) {
                    throw new RuntimeException("Acesso negado: Apenas administradores podem alterar perfil.");
                }
                usuarioExistente.setRole(usuarioAtualizado.getRole());
            }

            if (usuarioAtualizado.getPassword() != null && !usuarioAtualizado.getPassword().isBlank()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioAtualizado.getPassword()));
            }

            usuarioRepository.save(usuarioExistente);
        });
    }
}