package com.pizzaria.dto;

import com.pizzaria.enums.Categoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PizzaResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private Categoria categoria;
    private List<TamanhoResponseDTO> tamanhos;
    private List<String> ingredientes;
    private boolean disponivel;
}
