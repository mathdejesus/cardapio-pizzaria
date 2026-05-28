package com.pizzaria.service;

import com.pizzaria.dto.*;
import com.pizzaria.enums.Categoria;
import com.pizzaria.exception.CategoriaInvalidaException;
import com.pizzaria.exception.PizzaNotFoundException;
import com.pizzaria.model.Pizza;
import com.pizzaria.model.Tamanho;
import com.pizzaria.repository.PizzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PizzaService {

    private final PizzaRepository pizzaRepository;

    @Transactional(readOnly = true)
    public List<PizzaResponseDTO> findAll() {
        return pizzaRepository.findByDeletedFalse().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PizzaResponseDTO findById(Long id) {
        return toResponse(findActivePizza(id));
    }

    @Transactional(readOnly = true)
    public List<PizzaResponseDTO> findByCategoria(String categoria) {
        Categoria cat = parseCategoria(categoria);
        return pizzaRepository.findByCategoriaAndDeletedFalse(cat).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PizzaResponseDTO create(PizzaRequestDTO request) {
        Pizza pizza = Pizza.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .categoria(request.getCategoria())
                .tamanhos(mapTamanhos(request.getTamanhos()))
                .ingredientes(new ArrayList<>(request.getIngredientes()))
                .disponivel(true)
                .deleted(false)
                .build();

        return toResponse(pizzaRepository.save(pizza));
    }

    @Transactional
    public PizzaResponseDTO update(Long id, PizzaRequestDTO request) {
        Pizza pizza = findActivePizza(id);
        pizza.setNome(request.getNome());
        pizza.setDescricao(request.getDescricao());
        pizza.setCategoria(request.getCategoria());
        pizza.setTamanhos(mapTamanhos(request.getTamanhos()));
        pizza.setIngredientes(new ArrayList<>(request.getIngredientes()));

        return toResponse(pizzaRepository.save(pizza));
    }

    @Transactional
    public void softDelete(Long id) {
        Pizza pizza = findActivePizza(id);
        pizza.setDeleted(true);
        pizzaRepository.save(pizza);
    }

    @Transactional
    public PizzaResponseDTO updateDisponibilidade(Long id, DisponibilidadeRequestDTO request) {
        Pizza pizza = findActivePizza(id);
        pizza.setDisponivel(request.getDisponivel());
        return toResponse(pizzaRepository.save(pizza));
    }

    private Pizza findActivePizza(Long id) {
        return pizzaRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PizzaNotFoundException(id));
    }

    private Categoria parseCategoria(String categoria) {
        try {
            return Categoria.valueOf(categoria.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new CategoriaInvalidaException(categoria);
        }
    }

    private List<Tamanho> mapTamanhos(List<TamanhoRequestDTO> tamanhos) {
        return tamanhos.stream()
                .map(t -> Tamanho.builder()
                        .tipo(t.getTipo())
                        .preco(t.getPreco())
                        .fatias(t.getFatias())
                        .build())
                .toList();
    }

    public PizzaResponseDTO toResponse(Pizza pizza) {
        return PizzaResponseDTO.builder()
                .id(pizza.getId())
                .nome(pizza.getNome())
                .descricao(pizza.getDescricao())
                .categoria(pizza.getCategoria())
                .tamanhos(pizza.getTamanhos().stream()
                        .map(t -> TamanhoResponseDTO.builder()
                                .tipo(t.getTipo())
                                .preco(t.getPreco())
                                .fatias(t.getFatias())
                                .build())
                        .toList())
                .ingredientes(pizza.getIngredientes())
                .disponivel(pizza.isDisponivel())
                .build();
    }
}
