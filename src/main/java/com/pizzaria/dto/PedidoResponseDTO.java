package com.pizzaria.dto;

import com.pizzaria.enums.StatusPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {

    private Long id;
    private Long usuarioId;
    private List<ItemPedidoResponseDTO> itens;
    private StatusPedido status;
    private BigDecimal total;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}