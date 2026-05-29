package com.pizzaria.dto;

import com.pizzaria.enums.TamanhoTipo;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TamanhoRequestDTO {

    @NotNull(message = "O tipo do tamanho é obrigatório")
    private TamanhoTipo tipo;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser positivo")
    @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 8 dígitos inteiros e 2 decimais")
    private BigDecimal preco;

    @NotNull(message = "A quantidade de fatias é obrigatória")
    @Positive(message = "A quantidade de fatias deve ser positiva")
    private Integer fatias;
}
