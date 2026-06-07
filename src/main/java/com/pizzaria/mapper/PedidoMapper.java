package com.pizzaria.mapper;

import com.pizzaria.dto.ItemPedidoResponseDTO;
import com.pizzaria.dto.PedidoResponseDTO;
import com.pizzaria.model.ItemPedido;
import com.pizzaria.model.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    PedidoResponseDTO toResponse(Pedido pedido);

    default ItemPedidoResponseDTO toItemResponse(ItemPedido item) {
        return ItemPedidoResponseDTO.builder()
                .id(item.getId())
                .pizzaId(item.getPizza().getId())
                .pizzaNome(item.getPizza().getNome())
                .tamanhoTipo(item.getTamanhoTipo())
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .precoTotal(item.getPrecoTotal())
                .build();
    }

    default List<ItemPedidoResponseDTO> toItemResponseList(List<ItemPedido> itens) {
        return itens.stream().map(this::toItemResponse).collect(Collectors.toList());
    }
}