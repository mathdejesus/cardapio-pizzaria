package com.pizzaria.dto;

import jakarta.validation.Valid;
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
public class PedidoRequestDTO {

    @NotEmpty(message = "O pedido deve ter pelo menos um item")
    @Valid
    private List<@NotNull ItemPedidoRequestDTO> itens;
}