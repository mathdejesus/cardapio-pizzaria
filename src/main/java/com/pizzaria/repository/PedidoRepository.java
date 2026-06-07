package com.pizzaria.repository;

import com.pizzaria.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId ORDER BY p.dataCriacao DESC")
    Page<Pedido> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId AND p.status IN :statuses ORDER BY p.dataCriacao DESC")
    Page<Pedido> findByUsuarioIdAndStatusIn(@Param("usuarioId") Long usuarioId, @Param("statuses") List<com.pizzaria.enums.StatusPedido> statuses, Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE p.status = :status ORDER BY p.dataCriacao DESC")
    Page<Pedido> findByStatus(@Param("status") com.pizzaria.enums.StatusPedido status, Pageable pageable);

    Optional<Pedido> findByIdWithItens(Long id);
}