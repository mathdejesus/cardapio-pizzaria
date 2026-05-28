package com.pizzaria.dto;

import com.pizzaria.enums.Categoria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PizzaRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "A descrição é obrigatória")
    private String descricao;

    @NotNull(message = "A categoria é obrigatória")
    private Categoria categoria;

    @NotEmpty(message = "Informe ao menos um tamanho")
    @Valid
    private List<TamanhoRequestDTO> tamanhos;

    @NotEmpty(message = "Informe ao menos um ingrediente")
    private List<String> ingredientes;
}
