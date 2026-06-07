package com.pizzaria.mapper;

import com.pizzaria.dto.PedidoResponseDTO;
import com.pizzaria.model.Pedido;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-06T22:58:37-0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PedidoMapperImpl implements PedidoMapper {

    @Override
    public PedidoResponseDTO toResponse(Pedido pedido) {
        if ( pedido == null ) {
            return null;
        }

        PedidoResponseDTO.PedidoResponseDTOBuilder pedidoResponseDTO = PedidoResponseDTO.builder();

        pedidoResponseDTO.id( pedido.getId() );
        pedidoResponseDTO.itens( toItemResponseList( pedido.getItens() ) );
        pedidoResponseDTO.status( pedido.getStatus() );
        pedidoResponseDTO.total( pedido.getTotal() );
        pedidoResponseDTO.dataCriacao( pedido.getDataCriacao() );
        pedidoResponseDTO.dataAtualizacao( pedido.getDataAtualizacao() );

        return pedidoResponseDTO.build();
    }
}
