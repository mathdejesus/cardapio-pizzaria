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
    date = "2026-05-29T09:32:36-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class PizzaMapperImpl implements PizzaMapper {

    @Override
    public PizzaResponseDTO toResponse(Pizza pizza) {
        if ( pizza == null ) {
            return null;
        }

        PizzaResponseDTO.PizzaResponseDTOBuilder pizzaResponseDTO = PizzaResponseDTO.builder();

        pizzaResponseDTO.id( pizza.getId() );
        pizzaResponseDTO.nome( pizza.getNome() );
        pizzaResponseDTO.descricao( pizza.getDescricao() );
        pizzaResponseDTO.categoria( pizza.getCategoria() );
        pizzaResponseDTO.tamanhos( tamanhoListToTamanhoResponseDTOList( pizza.getTamanhos() ) );
        List<String> list1 = pizza.getIngredientes();
        if ( list1 != null ) {
            pizzaResponseDTO.ingredientes( new ArrayList<String>( list1 ) );
        }
        pizzaResponseDTO.disponivel( pizza.isDisponivel() );

        return pizzaResponseDTO.build();
    }

    @Override
    public Pizza toEntity(PizzaRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Pizza.PizzaBuilder pizza = Pizza.builder();

        pizza.nome( dto.getNome() );
        pizza.descricao( dto.getDescricao() );
        pizza.categoria( dto.getCategoria() );
        pizza.tamanhos( tamanhoRequestDTOListToTamanhoList( dto.getTamanhos() ) );
        List<String> list1 = dto.getIngredientes();
        if ( list1 != null ) {
            pizza.ingredientes( new ArrayList<String>( list1 ) );
        }

        return pizza.build();
    }

    @Override
    public TamanhoResponseDTO toTamanhoResponse(Tamanho tamanho) {
        if ( tamanho == null ) {
            return null;
        }

        TamanhoResponseDTO.TamanhoResponseDTOBuilder tamanhoResponseDTO = TamanhoResponseDTO.builder();

        tamanhoResponseDTO.tipo( tamanho.getTipo() );
        tamanhoResponseDTO.preco( tamanho.getPreco() );
        tamanhoResponseDTO.fatias( tamanho.getFatias() );

        return tamanhoResponseDTO.build();
    }

    @Override
    public Tamanho toTamanhoEntity(TamanhoRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Tamanho.TamanhoBuilder tamanho = Tamanho.builder();

        tamanho.tipo( dto.getTipo() );
        tamanho.preco( dto.getPreco() );
        if ( dto.getFatias() != null ) {
            tamanho.fatias( dto.getFatias() );
        }

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
