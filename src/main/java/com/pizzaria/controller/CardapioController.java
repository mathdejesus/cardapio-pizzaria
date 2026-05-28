package com.pizzaria.controller;

import com.pizzaria.dto.CardapioResponseDTO;
import com.pizzaria.service.CardapioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cardapio")
@RequiredArgsConstructor
public class CardapioController {

    private final CardapioService cardapioService;

    @GetMapping
    public ResponseEntity<CardapioResponseDTO> getCardapio() {
        return ResponseEntity.ok(cardapioService.getCardapio());
    }
}
