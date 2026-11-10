package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.enums.StatusPedidoEnum;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends MongoRepository<Pedido, String> {
    @Query("{ 'dataHoraEntrada': { '$gte': ?0, '$lt': ?1 } }")
    List<Pedido> findNoPeriodo(LocalDateTime inicio, LocalDateTime fimExclusivo);

    List<Pedido> findByClienteId(String clienteId);

    List<Pedido> findByStatusNotIn(List<StatusPedidoEnum> status);

    List<Pedido> findTop6ByStatusOrderByDataHoraSaidaDescDataHoraEntradaDescIdDesc(StatusPedidoEnum status);
}
