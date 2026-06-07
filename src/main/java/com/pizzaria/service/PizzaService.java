package com.pizzaria.service;

import com.pizzaria.dto.*;
import com.pizzaria.enums.Categoria;
import com.pizzaria.enums.TamanhoTipo;
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

import java.util.List;
import java.util.Set;

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
    public Page<PizzaResponseDTO> findByCategoria(String categoria, Pageable pageable) {
        Categoria cat = parseCategoria(categoria);
        return pizzaRepository.findByCategoriaAndDisponivelTrue(cat, pageable).map(pizzaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PizzaResponseDTO> findByIngrediente(String ingrediente, Pageable pageable) {
        return pizzaRepository.findByIngrediente(ingrediente, pageable).map(pizzaMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "cardapio", allEntries = true)
    public PizzaResponseDTO create(PizzaRequestDTO request) {
        validateTamanhos(request.getTamanhos());
        Pizza pizza = pizzaMapper.toEntity(request);
        pizza.setDisponivel(true);
        pizza.setDeleted(false);
        pizza.setIngredientes(pizzaMapper.toIngredientesFromDTO(request));
        pizza.setImagemUrl(request.getImagemUrl());

        return pizzaMapper.toResponse(pizzaRepository.save(pizza));
    }

    @Transactional
    @CacheEvict(value = "cardapio", allEntries = true)
    public PizzaResponseDTO update(Long id, PizzaRequestDTO request) {
        validateTamanhos(request.getTamanhos());
        Pizza pizza = findActivePizza(id);
        Pizza mapped = pizzaMapper.toEntity(request);
        pizza.setNome(mapped.getNome());
        pizza.setDescricao(mapped.getDescricao());
        pizza.setCategoria(mapped.getCategoria());
        pizza.setTamanhos(mapped.getTamanhos());
        pizza.setIngredientes(pizzaMapper.toIngredientesFromDTO(request));
        pizza.setImagemUrl(request.getImagemUrl());

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

    @Transactional(readOnly = true)
    public List<PizzaResponseDTO> findDeleted() {
        return pizzaRepository.findDeleted().stream()
                .map(pizzaMapper::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "cardapio", allEntries = true)
    public PizzaResponseDTO restore(Long id) {
        Pizza pizza = pizzaRepository.findDeletedById(id)
                .orElseThrow(() -> new PizzaNotFoundException(id));
        pizza.setDeleted(false);
        return pizzaMapper.toResponse(pizza);
    }

    private Pizza findActivePizza(Long id) {
        return pizzaRepository.findById(id)
                .orElseThrow(() -> new PizzaNotFoundException(id));
    }

    private void validateTamanhos(List<TamanhoRequestDTO> tamanhos) {
        Set<TamanhoTipo> tipos = tamanhos.stream()
                .map(TamanhoRequestDTO::getTipo)
                .collect(java.util.stream.Collectors.toSet());
        if (tipos.size() != tamanhos.size()) {
            throw new IllegalArgumentException("Não é possível cadastrar tamanhos com tipos duplicados");
        }
    }

    private Categoria parseCategoria(String categoria) {
        try {
            return Categoria.valueOf(categoria.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new CategoriaInvalidaException(categoria);
        }
    }
}
