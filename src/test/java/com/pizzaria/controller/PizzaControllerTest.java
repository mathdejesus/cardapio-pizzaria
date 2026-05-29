package com.pizzaria.controller;

import com.pizzaria.config.TestConfig;
import com.pizzaria.dto.PizzaResponseDTO;
import com.pizzaria.enums.Categoria;
import com.pizzaria.service.PizzaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
class PizzaControllerTest {

    @Autowired
    TestRestTemplate rest;

    @MockBean
    PizzaService pizzaService;

    @Test
    void listPizzas_shouldReturnPage() {
        PageImpl<PizzaResponseDTO> page = new PageImpl<>(List.of(
                PizzaResponseDTO.builder().id(1L).nome("Margherita").categoria(Categoria.SALGADA).build()
        ));

        when(pizzaService.findAll(any())).thenReturn(page);

        ResponseEntity<String> result = rest.getForEntity("/api/pizzas?page=0&size=10", String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).contains("Margherita");
    }

    @Test
    void getPizzaById_shouldReturnPizza() {
        PizzaResponseDTO dto = PizzaResponseDTO.builder().id(1L).nome("Margherita").categoria(Categoria.SALGADA).build();
        when(pizzaService.findById(1L)).thenReturn(dto);

        ResponseEntity<PizzaResponseDTO> result = rest.getForEntity("/api/pizzas/1", PizzaResponseDTO.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getNome()).isEqualTo("Margherita");
    }

    @Test
    void getPizzaByCategoria_shouldReturnFiltered() {
        when(pizzaService.findByCategoria("SALGADA")).thenReturn(List.of(
                PizzaResponseDTO.builder().id(1L).nome("Margherita").categoria(Categoria.SALGADA).build()
        ));

        ResponseEntity<List> result = rest.getForEntity("/api/pizzas/categoria/SALGADA", List.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createPizza_withoutAuth_shouldReturnUnauthorized() {
        ResponseEntity<Void> result = rest.postForEntity("/api/pizzas", null, Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
