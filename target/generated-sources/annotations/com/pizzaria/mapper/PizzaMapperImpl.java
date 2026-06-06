package com.pizzaria.mapper;

import com.pizzaria.dto.PizzaRequestDTO;
import com.pizzaria.dto.PizzaResponseDTO;
import com.pizzaria.dto.TamanhoRequestDTO;
import com.pizzaria.dto.TamanhoResponseDTO;
import com.pizzaria.model.Pizza;
import com.pizzaria.model.Tamanho;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-05T20:41:34-0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PizzaMapperImpl implements PizzaMapper {

    @Override
    public PizzaResponseDTO toResponse(Pizza pizza) {
        if ( pizza == null ) {
            return null;
        }

        PizzaResponseDTO.PizzaResponseDTOBuilder pizzaResponseDTO = PizzaResponseDTO.builder();

        pizzaResponseDTO.categoria( pizza.getCategoria() );
        pizzaResponseDTO.descricao( pizza.getDescricao() );
        pizzaResponseDTO.disponivel( pizza.isDisponivel() );
        pizzaResponseDTO.id( pizza.getId() );
        List<String> list = pizza.getIngredientes();
        if ( list != null ) {
            pizzaResponseDTO.ingredientes( new ArrayList<String>( list ) );
        }
        pizzaResponseDTO.nome( pizza.getNome() );
        pizzaResponseDTO.tamanhos( tamanhoListToTamanhoResponseDTOList( pizza.getTamanhos() ) );

        return pizzaResponseDTO.build();
    }

    @Override
    public Pizza toEntity(PizzaRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Pizza.PizzaBuilder pizza = Pizza.builder();

        pizza.categoria( dto.getCategoria() );
        pizza.descricao( dto.getDescricao() );
        List<String> list = dto.getIngredientes();
        if ( list != null ) {
            pizza.ingredientes( new ArrayList<String>( list ) );
        }
        pizza.nome( dto.getNome() );
        pizza.tamanhos( tamanhoRequestDTOListToTamanhoList( dto.getTamanhos() ) );

        return pizza.build();
    }

    @Override
    public TamanhoResponseDTO toTamanhoResponse(Tamanho tamanho) {
        if ( tamanho == null ) {
            return null;
        }

        TamanhoResponseDTO.TamanhoResponseDTOBuilder tamanhoResponseDTO = TamanhoResponseDTO.builder();

        tamanhoResponseDTO.fatias( tamanho.getFatias() );
        tamanhoResponseDTO.preco( tamanho.getPreco() );
        tamanhoResponseDTO.tipo( tamanho.getTipo() );

        return tamanhoResponseDTO.build();
    }

    @Override
    public Tamanho toTamanhoEntity(TamanhoRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Tamanho.TamanhoBuilder tamanho = Tamanho.builder();

        if ( dto.getFatias() != null ) {
            tamanho.fatias( dto.getFatias() );
        }
        tamanho.preco( dto.getPreco() );
        tamanho.tipo( dto.getTipo() );

        return tamanho.build();
    }

    protected List<TamanhoResponseDTO> tamanhoListToTamanhoResponseDTOList(List<Tamanho> list) {
        if ( list == null ) {
            return null;
        }

        List<TamanhoResponseDTO> list1 = new ArrayList<TamanhoResponseDTO>( list.size() );
        for ( Tamanho tamanho : list ) {
            list1.add( toTamanhoResponse( tamanho ) );
        }

        return list1;
    }

    protected List<Tamanho> tamanhoRequestDTOListToTamanhoList(List<TamanhoRequestDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<Tamanho> list1 = new ArrayList<Tamanho>( list.size() );
        for ( TamanhoRequestDTO tamanhoRequestDTO : list ) {
            list1.add( toTamanhoEntity( tamanhoRequestDTO ) );
        }

        return list1;
    }
}
