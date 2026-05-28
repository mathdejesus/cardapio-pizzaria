package com.pizzaria.dto;

import com.pizzaria.enums.TamanhoTipo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TamanhoResponseDTO {

    private TamanhoTipo tipo;
    private BigDecimal preco;
    private int fatias;
}
