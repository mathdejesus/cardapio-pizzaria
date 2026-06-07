package com.pizzaria.dto;

import com.pizzaria.enums.TamanhoTipo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoRequestDTO {

    @NotNull(message = "O ID da pizza é obrigatório")
    private Long pizzaId;

    @NotNull(message = "O tamanho é obrigatório")
    private TamanhoTipo tamanhoTipo;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    private Integer quantidade;
}