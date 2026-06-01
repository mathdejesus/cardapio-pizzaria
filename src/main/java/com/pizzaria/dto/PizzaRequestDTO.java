package com.pizzaria.dto;

import com.pizzaria.enums.Categoria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 1, max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(min = 1, max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    @NotNull(message = "A categoria é obrigatória")
    private Categoria categoria;

    @NotEmpty(message = "Informe ao menos um tamanho")
    @Valid
    private List<@NotNull TamanhoRequestDTO> tamanhos;

    @NotEmpty(message = "Informe ao menos um ingrediente")
    @Size(min = 1, max = 50, message = "Deve haver no máximo 50 ingredientes")
    private List<@NotBlank @Size(min = 1, max = 200, message = "Cada ingrediente deve ter no máximo 200 caracteres") String> ingredientes;
}
