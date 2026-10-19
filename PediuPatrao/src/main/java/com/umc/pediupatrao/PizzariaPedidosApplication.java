package com.umc.pediupatrao;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.enums.RoleEnum;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class PizzariaPedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PizzariaPedidosApplication.class, args);
    }

    @Bean
    public CommandLineRunner injetarUsuarios(UsuarioRepository repository, PasswordEncoder encoder) {
        return args -> {
            repository.deleteAll();

            // 1. Criando ADMIN
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("gabriel.fernandez.a8@gmail.com");
            admin.setPassword(encoder.encode("123456"));
            admin.setRole(RoleEnum.ADMIN);
            repository.save(admin);

            // 2. Criando GERENTE
            Usuario gerente = new Usuario();
            gerente.setUsername("gerente");
            gerente.setEmail("gerente@pediupatrao.com");
            gerente.setPassword(encoder.encode("123456"));
            gerente.setRole(RoleEnum.GERENTE);
            repository.save(gerente);

            // 3. Criando ATENDENTE
            Usuario atendente = new Usuario();
            atendente.setUsername("atendente");
            atendente.setEmail("atendente@pediupatrao.com");
            atendente.setPassword(encoder.encode("123456"));
            atendente.setRole(RoleEnum.ATENDENTE);
            repository.save(atendente);

            System.out.println("//////////////////////////////////////////////////////////////////////");
            System.out.println(" AS 3 CONTAS FORAM RECRIADAS COM SUCESSO!");
            System.out.println(" admin     | Email:️ admin@pediupatrao.com     | Codigo: 123456");
            System.out.println(" gerente   | Email:️ gerente@pediupatrao.com   | Codigo: 123456");
            System.out.println(" atendente | Email:️ atendente@pediupatrao.com | Codigo: 123456");
            System.out.println("//////////////////////////////////////////////////////////////////////");
        };
    }
}