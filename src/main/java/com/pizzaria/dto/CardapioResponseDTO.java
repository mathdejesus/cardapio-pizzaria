package com.pizzaria.dto;

import com.pizzaria.enums.Categoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardapioResponseDTO {

    private Map<Categoria, List<PizzaResponseDTO>> pizzasPorCategoria;
    private int totalSaboresDisponiveis;
}
