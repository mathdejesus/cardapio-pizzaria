package com.pizzaria.mapper;

import com.pizzaria.dto.PizzaRequestDTO;
import com.pizzaria.dto.PizzaResponseDTO;
import com.pizzaria.dto.TamanhoRequestDTO;
import com.pizzaria.dto.TamanhoResponseDTO;
import com.pizzaria.model.Pizza;
import com.pizzaria.model.Tamanho;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PizzaMapper {

    PizzaResponseDTO toResponse(Pizza pizza);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disponivel", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Pizza toEntity(PizzaRequestDTO dto);

    TamanhoResponseDTO toTamanhoResponse(Tamanho tamanho);

    Tamanho toTamanhoEntity(TamanhoRequestDTO dto);

    default List<String> toIngredientes(Pizza pizza) {
        return pizza.getIngredientes() != null ? List.copyOf(pizza.getIngredientes()) : List.of();
    }

    default List<String> toIngredientesFromDTO(PizzaRequestDTO dto) {
        return dto.getIngredientes() != null ? List.copyOf(dto.getIngredientes()) : List.of();
    }
}
