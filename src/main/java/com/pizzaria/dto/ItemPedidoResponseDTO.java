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
public class ItemPedidoResponseDTO {

    private Long id;
    private Long pizzaId;
    private String pizzaNome;
    private TamanhoTipo tamanhoTipo;
    private int quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal precoTotal;
}