package com.pizzaria.repository;

import com.pizzaria.enums.Categoria;
import com.pizzaria.model.Pizza;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PizzaRepository extends JpaRepository<Pizza, Long> {

    List<Pizza> findByDeletedFalse();

    List<Pizza> findByCategoriaAndDeletedFalse(Categoria categoria);

    List<Pizza> findByDisponivelTrueAndDeletedFalse();

    Optional<Pizza> findByIdAndDeletedFalse(Long id);
}
