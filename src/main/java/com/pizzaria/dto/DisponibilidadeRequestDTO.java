package com.pizzaria.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadeRequestDTO {

    @NotNull(message = "O campo disponivel é obrigatório")
    private Boolean disponivel;
}
