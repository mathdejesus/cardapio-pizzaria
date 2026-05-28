package com.pizzaria.dto;

import com.pizzaria.enums.TamanhoTipo;
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
    private BigDecimal preco;

    @Positive(message = "A quantidade de fatias deve ser positiva")
    private int fatias;
}
