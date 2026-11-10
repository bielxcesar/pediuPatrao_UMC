package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.enums.RoleEnum;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarios, PasswordEncoder encoder,
                                  @Value("${PEDIU_ADMIN_PASSWORD:}") String senhaInicial) {
        return args -> {
            if (senhaInicial.isBlank() || usuarios.existsByRole(RoleEnum.ADMIN)) {
                return;
            }
            if (usuarios.findByUsername("admin").isPresent()) {
                throw new IllegalStateException("O nome admin já está em uso. Restaure o acesso administrativo no banco.");
            }
            if (senhaInicial.length() < 12 || senhaInicial.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalArgumentException("PEDIU_ADMIN_PASSWORD deve ter ao menos 12 caracteres e até 72 bytes.");
            }
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setRole(RoleEnum.ADMIN);
            admin.setPassword(encoder.encode(senhaInicial));
            usuarios.save(admin);
        };
    }
}
