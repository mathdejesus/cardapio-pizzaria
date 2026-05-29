package com.pizzaria.service;

import com.pizzaria.dto.CardapioResponseDTO;
import com.pizzaria.dto.PizzaResponseDTO;
import com.pizzaria.enums.Categoria;
import com.pizzaria.mapper.PizzaMapper;
import com.pizzaria.model.Pizza;
import com.pizzaria.repository.PizzaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardapioServiceTest {

    @Mock
    PizzaRepository pizzaRepository;

    @Mock
    PizzaMapper pizzaMapper;

    @InjectMocks
    CardapioService cardapioService;

    @Test
    void getCardapio_shouldReturnGroupedByCategory() {
        Pizza pizzaSalgada = Pizza.builder().id(1L).categoria(Categoria.SALGADA).build();
        Pizza pizzaDoce = Pizza.builder().id(2L).categoria(Categoria.DOCE).build();
        PizzaResponseDTO dtoSalgada = PizzaResponseDTO.builder().id(1L).categoria(Categoria.SALGADA).build();
        PizzaResponseDTO dtoDoce = PizzaResponseDTO.builder().id(2L).categoria(Categoria.DOCE).build();

        when(pizzaRepository.findByDisponivelTrue()).thenReturn(List.of(pizzaSalgada, pizzaDoce));
        when(pizzaMapper.toResponse(pizzaSalgada)).thenReturn(dtoSalgada);
        when(pizzaMapper.toResponse(pizzaDoce)).thenReturn(dtoDoce);

        CardapioResponseDTO result = cardapioService.getCardapio();

        assertThat(result.getPizzasPorCategoria()).containsKeys(Categoria.SALGADA, Categoria.DOCE);
        assertThat(result.getTotalSaboresDisponiveis()).isEqualTo(2);
    }

    @Test
    void getCardapio_shouldReturnEmptyWhenNoPizzasAvailable() {
        when(pizzaRepository.findByDisponivelTrue()).thenReturn(List.of());

        CardapioResponseDTO result = cardapioService.getCardapio();

        assertThat(result.getPizzasPorCategoria()).isEmpty();
        assertThat(result.getTotalSaboresDisponiveis()).isZero();
    }
}
