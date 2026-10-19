package com.umc.pediupatrao.config;

import com.umc.pediupatrao.service.UsuarioDetailsService;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Liberta recursos estáticos e forwards do Thymeleaf
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // Rotas públicas de recuperação de senha (CORRIGIDO AQUI)
                        .requestMatchers("/esqueci-senha/**").permitAll()

                        // ==========================================
                        // ADMIN: Controle total de usuários (Bloqueado em Clientes)
                        // ==========================================
                        .requestMatchers("/usuarios", "/usuarios/**", "/api/usuarios", "/api/usuarios/**")
                        .hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                        // ==========================================
                        // AUDITORIA: Apenas Admin e Gerente
                        // ==========================================
                        .requestMatchers("/auditoria", "/auditoria/**", "/api/auditoria", "/api/auditoria/**")
                        .hasAnyAuthority("ADMIN", "ROLE_ADMIN", "GERENTE", "ROLE_GERENTE")

                        // ==========================================
                        // GERENTE: Exclusividade financeira (Descontos e Cancelamentos)
                        // ==========================================
                        .requestMatchers("/api/pedidos/*/desconto", "/api/pedidos/*/cancelar")
                        .hasAnyAuthority("GERENTE", "ROLE_GERENTE")

                        // ==========================================
                        // CLIENTES: A solução definitiva (Páginas vs API)
                        // ==========================================

                        // 1. Acesso às páginas visuais (Tiramos o HttpMethod.GET para o Thymeleaf funcionar)
                        .requestMatchers("/clientes", "/clientes/")
                        .hasAnyAuthority("ATENDENTE", "ROLE_ATENDENTE", "GERENTE", "ROLE_GERENTE")

                        .requestMatchers("/clientes/novo", "/clientes/editar/**")
                        .hasAnyAuthority("ATENDENTE", "ROLE_ATENDENTE")

                        // 2. Proteção rigorosa na API (Onde a exigência do professor atua)
                        .requestMatchers(HttpMethod.GET, "/api/clientes", "/api/clientes/**")
                        .hasAnyAuthority("ATENDENTE", "ROLE_ATENDENTE", "GERENTE", "ROLE_GERENTE")

                        .requestMatchers(HttpMethod.POST, "/api/clientes", "/api/clientes/**")
                        .hasAnyAuthority("ATENDENTE", "ROLE_ATENDENTE")

                        .requestMatchers(HttpMethod.PUT, "/api/clientes", "/api/clientes/**")
                        .hasAnyAuthority("ATENDENTE", "ROLE_ATENDENTE")

                        .requestMatchers(HttpMethod.DELETE, "/api/clientes", "/api/clientes/**")
                        .hasAnyAuthority("ATENDENTE", "ROLE_ATENDENTE")

                        // Qualquer outra requisição precisa apenas de login válido
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.sendRedirect("/"); // Redireciona para a página inicial após login bem-sucedido
        };
    }
}