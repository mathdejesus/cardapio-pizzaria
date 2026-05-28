package com.pizzaria.controller;

import com.pizzaria.dto.DisponibilidadeRequestDTO;
import com.pizzaria.dto.PizzaRequestDTO;
import com.pizzaria.dto.PizzaResponseDTO;
import com.pizzaria.service.PizzaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pizzas")
@RequiredArgsConstructor
public class PizzaController {

    private final PizzaService pizzaService;

    @GetMapping
    public ResponseEntity<List<PizzaResponseDTO>> findAll() {
        return ResponseEntity.ok(pizzaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PizzaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(pizzaService.findById(id));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<PizzaResponseDTO>> findByCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(pizzaService.findByCategoria(categoria));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PizzaResponseDTO> create(@Valid @RequestBody PizzaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pizzaService.create(request));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PizzaResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PizzaRequestDTO request) {
        return ResponseEntity.ok(pizzaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pizzaService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/disponibilidade")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PizzaResponseDTO> updateDisponibilidade(
            @PathVariable Long id,
            @Valid @RequestBody DisponibilidadeRequestDTO request) {
        return ResponseEntity.ok(pizzaService.updateDisponibilidade(id, request));
    }
}
