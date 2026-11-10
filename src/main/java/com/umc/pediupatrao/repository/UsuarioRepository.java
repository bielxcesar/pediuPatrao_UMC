package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.enums.RoleEnum;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    boolean existsByRole(RoleEnum role);
}