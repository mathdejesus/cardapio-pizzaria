package com.pizzaria.controller;

import com.pizzaria.config.TestConfig;
import com.pizzaria.dto.CardapioResponseDTO;
import com.pizzaria.dto.PizzaResponseDTO;
import com.pizzaria.enums.Categoria;
import com.pizzaria.service.CardapioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
class CardapioControllerTest {

    @Autowired
    TestRestTemplate rest;

    @MockBean
    CardapioService cardapioService;

    @Test
    void getCardapio_shouldReturnGroupedMenu() {
        CardapioResponseDTO response = CardapioResponseDTO.builder()
                .pizzasPorCategoria(Map.of(
                        Categoria.SALGADA, List.of(
                                PizzaResponseDTO.builder().id(1L).nome("Margherita").categoria(Categoria.SALGADA).build()
                        ),
                        Categoria.DOCE, List.of(
                                PizzaResponseDTO.builder().id(2L).nome("Chocolate").categoria(Categoria.DOCE).build()
                        )
                ))
                .totalSaboresDisponiveis(2)
                .build();

        when(cardapioService.getCardapio()).thenReturn(response);

        ResponseEntity<CardapioResponseDTO> result = rest.getForEntity("/api/cardapio", CardapioResponseDTO.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getTotalSaboresDisponiveis()).isEqualTo(2);
        assertThat(result.getBody().getPizzasPorCategoria()).containsKeys(Categoria.SALGADA, Categoria.DOCE);
    }
}
