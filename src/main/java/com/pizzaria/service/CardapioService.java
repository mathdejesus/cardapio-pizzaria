package com.pizzaria.service;

import com.pizzaria.dto.CardapioResponseDTO;
import com.pizzaria.dto.PizzaResponseDTO;
import com.pizzaria.enums.Categoria;
import com.pizzaria.mapper.PizzaMapper;
import com.pizzaria.model.Pizza;
import com.pizzaria.repository.PizzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final PizzaRepository pizzaRepository;
    private final PizzaMapper pizzaMapper;

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "cardapio")
    public CardapioResponseDTO getCardapio() {
        List<Pizza> pizzas = pizzaRepository.findByDisponivelTrue();
        List<PizzaResponseDTO> dtos = pizzas.stream()
                .map(pizzaMapper::toResponse)
                .toList();

        Map<Categoria, List<PizzaResponseDTO>> porCategoria = dtos.stream()
                .collect(Collectors.groupingBy(
                        PizzaResponseDTO::getCategoria,
                        () -> new EnumMap<>(Categoria.class),
                        Collectors.toList()));

        return CardapioResponseDTO.builder()
                .pizzasPorCategoria(porCategoria)
                .totalSaboresDisponiveis(dtos.size())
                .build();
    }
}
