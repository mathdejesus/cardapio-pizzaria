package com.pizzaria.service;

import com.pizzaria.dto.PizzaRequestDTO;
import com.pizzaria.dto.PizzaResponseDTO;
import com.pizzaria.dto.TamanhoRequestDTO;
import com.pizzaria.enums.Categoria;
import com.pizzaria.enums.TamanhoTipo;
import com.pizzaria.exception.PizzaNotFoundException;
import com.pizzaria.mapper.PizzaMapper;
import com.pizzaria.model.Pizza;
import com.pizzaria.repository.PizzaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PizzaServiceTest {

    @Mock
    PizzaRepository pizzaRepository;

    @Mock
    PizzaMapper pizzaMapper;

    @InjectMocks
    PizzaService pizzaService;

    private Pizza createPizza(Long id) {
        return Pizza.builder()
                .id(id)
                .nome("Margherita")
                .descricao("Mussarela, tomate e manjericão")
                .categoria(Categoria.SALGADA)
                .disponivel(true)
                .deleted(false)
                .build();
    }

    private PizzaResponseDTO createResponseDTO(Long id) {
        return PizzaResponseDTO.builder()
                .id(id)
                .nome("Margherita")
                .descricao("Mussarela, tomate e manjericão")
                .categoria(Categoria.SALGADA)
                .disponivel(true)
                .build();
    }

    private PizzaRequestDTO createRequestDTO() {
        return PizzaRequestDTO.builder()
                .nome("Margherita")
                .descricao("Mussarela, tomate e manjericão")
                .categoria(Categoria.SALGADA)
                .tamanhos(List.of(new TamanhoRequestDTO(TamanhoTipo.MEDIA, BigDecimal.valueOf(40), 8)))
                .ingredientes(List.of("Mussarela", "Tomate", "Manjericão"))
                .build();
    }

    @Test
    void findAll_shouldReturnPagedResults() {
        PageRequest pageable = PageRequest.of(0, 10);
        Pizza pizza = createPizza(1L);
        PizzaResponseDTO dto = createResponseDTO(1L);
        Page<Pizza> pizzaPage = new PageImpl<>(List.of(pizza));

        when(pizzaRepository.findAll(pageable)).thenReturn(pizzaPage);
        when(pizzaMapper.toResponse(pizza)).thenReturn(dto);

        Page<PizzaResponseDTO> result = pizzaService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void findById_shouldReturnPizzaWhenExists() {
        Pizza pizza = createPizza(1L);
        PizzaResponseDTO dto = createResponseDTO(1L);

        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(pizza));
        when(pizzaMapper.toResponse(pizza)).thenReturn(dto);

        PizzaResponseDTO result = pizzaService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNome()).isEqualTo("Margherita");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(pizzaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pizzaService.findById(99L))
                .isInstanceOf(PizzaNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findByCategoria_shouldReturnFilteredPizzas() {
        Pizza pizza = createPizza(1L);
        PizzaResponseDTO dto = createResponseDTO(1L);

        when(pizzaRepository.findByCategoria(Categoria.SALGADA)).thenReturn(List.of(pizza));
        when(pizzaMapper.toResponse(pizza)).thenReturn(dto);

        List<PizzaResponseDTO> result = pizzaService.findByCategoria("SALGADA");

        assertThat(result).hasSize(1);
    }

    @Test
    void create_shouldSaveAndReturnPizza() {
        PizzaRequestDTO request = createRequestDTO();
        Pizza pizza = createPizza(null);
        Pizza savedPizza = createPizza(1L);
        PizzaResponseDTO dto = createResponseDTO(1L);

        when(pizzaMapper.toEntity(request)).thenReturn(pizza);
        when(pizzaRepository.save(pizza)).thenReturn(savedPizza);
        when(pizzaMapper.toResponse(savedPizza)).thenReturn(dto);

        PizzaResponseDTO result = pizzaService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(pizzaRepository).save(pizza);
    }

    @Test
    void update_shouldModifyAndReturnPizza() {
        Pizza pizza = createPizza(1L);
        PizzaRequestDTO request = createRequestDTO();
        Pizza mapped = createPizza(null);
        mapped.setNome("Calabresa");
        PizzaResponseDTO dto = createResponseDTO(1L);
        dto.setNome("Calabresa");

        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(pizza));
        when(pizzaMapper.toEntity(request)).thenReturn(mapped);
        when(pizzaMapper.toResponse(pizza)).thenReturn(dto);

        PizzaResponseDTO result = pizzaService.update(1L, request);

        assertThat(result.getNome()).isEqualTo("Calabresa");
    }

    @Test
    void softDelete_shouldSetDeletedTrue() {
        Pizza pizza = createPizza(1L);
        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(pizza));

        pizzaService.softDelete(1L);

        assertThat(pizza.isDeleted()).isTrue();
    }

    @Test
    void updateDisponibilidade_shouldToggleAvailability() {
        Pizza pizza = createPizza(1L);
        pizza.setDisponivel(false);
        PizzaResponseDTO dto = createResponseDTO(1L);
        dto.setDisponivel(true);

        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(pizza));
        when(pizzaMapper.toResponse(pizza)).thenReturn(dto);

        PizzaResponseDTO result = pizzaService.updateDisponibilidade(1L,
                new com.pizzaria.dto.DisponibilidadeRequestDTO(true));

        assertThat(result.isDisponivel()).isTrue();
    }
}
