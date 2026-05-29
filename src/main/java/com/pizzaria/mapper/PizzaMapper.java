package com.pizzaria.mapper;

import com.pizzaria.dto.PizzaRequestDTO;
import com.pizzaria.dto.PizzaResponseDTO;
import com.pizzaria.dto.TamanhoRequestDTO;
import com.pizzaria.dto.TamanhoResponseDTO;
import com.pizzaria.model.Pizza;
import com.pizzaria.model.Tamanho;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PizzaMapper {

    PizzaResponseDTO toResponse(Pizza pizza);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disponivel", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Pizza toEntity(PizzaRequestDTO dto);

    TamanhoResponseDTO toTamanhoResponse(Tamanho tamanho);

    Tamanho toTamanhoEntity(TamanhoRequestDTO dto);
}
