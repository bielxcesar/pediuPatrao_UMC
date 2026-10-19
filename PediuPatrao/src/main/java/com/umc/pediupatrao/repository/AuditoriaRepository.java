package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Auditoria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends MongoRepository<Auditoria, String> {
}