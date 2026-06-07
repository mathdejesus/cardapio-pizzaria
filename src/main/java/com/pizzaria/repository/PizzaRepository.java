package com.pizzaria.repository;

import com.pizzaria.enums.Categoria;
import com.pizzaria.model.Pizza;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PizzaRepository extends JpaRepository<Pizza, Long> {

    List<Pizza> findByCategoria(Categoria categoria);

    @Query("SELECT p FROM Pizza p WHERE p.categoria = :categoria AND p.disponivel = true AND p.deleted = false")
    Page<Pizza> findByCategoriaAndDisponivelTrue(@Param("categoria") Categoria categoria, Pageable pageable);

    List<Pizza> findByDisponivelTrue();

    @Query("SELECT p FROM Pizza p WHERE p.deleted = true")
    List<Pizza> findDeleted();

    @Query("SELECT p FROM Pizza p WHERE p.id = :id AND p.deleted = true")
    Optional<Pizza> findDeletedById(@Param("id") Long id);

    @Query("SELECT COUNT(p) FROM Pizza p WHERE p.deleted = false")
    long countNonDeleted();

    @Query("SELECT p FROM Pizza p WHERE :ingrediente MEMBER OF p.ingredientes AND p.disponivel = true AND p.deleted = false")
    Page<Pizza> findByIngrediente(@Param("ingrediente") String ingrediente, Pageable pageable);
}
