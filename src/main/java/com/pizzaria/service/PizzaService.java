package com.pizzaria.service;

import com.pizzaria.dto.*;
import com.pizzaria.enums.Categoria;
import com.pizzaria.exception.CategoriaInvalidaException;
import com.pizzaria.exception.PizzaNotFoundException;
import com.pizzaria.mapper.PizzaMapper;
import com.pizzaria.model.Pizza;
import com.pizzaria.repository.PizzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PizzaService {

    private final PizzaRepository pizzaRepository;
    private final PizzaMapper pizzaMapper;

    @Transactional(readOnly = true)
    public Page<PizzaResponseDTO> findAll(Pageable pageable) {
        return pizzaRepository.findAll(pageable).map(pizzaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PizzaResponseDTO findById(Long id) {
        return pizzaMapper.toResponse(findActivePizza(id));
    }

    @Transactional(readOnly = true)
    public List<PizzaResponseDTO> findByCategoria(String categoria) {
        Categoria cat = parseCategoria(categoria);
        return pizzaRepository.findByCategoria(cat).stream()
                .map(pizzaMapper::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "cardapio", allEntries = true)
    public PizzaResponseDTO create(PizzaRequestDTO request) {
        Pizza pizza = pizzaMapper.toEntity(request);
        pizza.setDisponivel(true);
        pizza.setDeleted(false);
        pizza.setIngredientes(new ArrayList<>(request.getIngredientes()));

        return pizzaMapper.toResponse(pizzaRepository.save(pizza));
    }

    @Transactional
    @CacheEvict(value = "cardapio", allEntries = true)
    public PizzaResponseDTO update(Long id, PizzaRequestDTO request) {
        Pizza pizza = findActivePizza(id);
        Pizza mapped = pizzaMapper.toEntity(request);
        pizza.setNome(mapped.getNome());
        pizza.setDescricao(mapped.getDescricao());
        pizza.setCategoria(mapped.getCategoria());
        pizza.setTamanhos(mapped.getTamanhos());
        pizza.setIngredientes(new ArrayList<>(request.getIngredientes()));

        return pizzaMapper.toResponse(pizza);
    }

    @Transactional
    @CacheEvict(value = "cardapio", allEntries = true)
    public void softDelete(Long id) {
        Pizza pizza = findActivePizza(id);
        pizza.setDeleted(true);
    }

    @Transactional
    @CacheEvict(value = "cardapio", allEntries = true)
    public PizzaResponseDTO updateDisponibilidade(Long id, DisponibilidadeRequestDTO request) {
        Pizza pizza = findActivePizza(id);
        pizza.setDisponivel(request.getDisponivel());
        return pizzaMapper.toResponse(pizza);
    }

    private Pizza findActivePizza(Long id) {
        return pizzaRepository.findById(id)
                .orElseThrow(() -> new PizzaNotFoundException(id));
    }

    private Categoria parseCategoria(String categoria) {
        try {
            return Categoria.valueOf(categoria.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new CategoriaInvalidaException(categoria);
        }
    }
}
