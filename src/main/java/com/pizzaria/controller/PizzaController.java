package com.pizzaria.controller;

import com.pizzaria.dto.DisponibilidadeRequestDTO;
import com.pizzaria.dto.PizzaRequestDTO;
import com.pizzaria.dto.PizzaResponseDTO;
import com.pizzaria.service.PizzaService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/pizzas")
@RequiredArgsConstructor
public class PizzaController {

    private final PizzaService pizzaService;
    private final PagedResourcesAssembler<PizzaResponseDTO> pagedResourcesAssembler;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<PizzaResponseDTO>>> findAll(
            @Parameter(hidden = true) Pageable pageable) {
        Page<PizzaResponseDTO> page = pizzaService.findAll(pageable);
        PagedModel<EntityModel<PizzaResponseDTO>> model = pagedResourcesAssembler.toModel(page, dto ->
                EntityModel.of(dto, linkTo(methodOn(PizzaController.class).findById(dto.getId())).withSelfRel()));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PizzaResponseDTO>> findById(@PathVariable Long id) {
        PizzaResponseDTO dto = pizzaService.findById(id);
        EntityModel<PizzaResponseDTO> model = EntityModel.of(dto,
                linkTo(methodOn(PizzaController.class).findById(id)).withSelfRel(),
                linkTo(methodOn(PizzaController.class).findAll(Pageable.unpaged())).withRel("pizzas"));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<PagedModel<EntityModel<PizzaResponseDTO>>> findByCategoria(
            @PathVariable String categoria,
            @Parameter(hidden = true) Pageable pageable) {
        Page<PizzaResponseDTO> page = pizzaService.findByCategoria(categoria, pageable);
        PagedModel<EntityModel<PizzaResponseDTO>> model = pagedResourcesAssembler.toModel(page, dto ->
                EntityModel.of(dto, linkTo(methodOn(PizzaController.class).findById(dto.getId())).withSelfRel()));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/ingrediente/{ingrediente}")
    public ResponseEntity<PagedModel<EntityModel<PizzaResponseDTO>>> findByIngrediente(
            @PathVariable String ingrediente,
            @Parameter(hidden = true) Pageable pageable) {
        Page<PizzaResponseDTO> page = pizzaService.findByIngrediente(ingrediente, pageable);
        PagedModel<EntityModel<PizzaResponseDTO>> model = pagedResourcesAssembler.toModel(page, dto ->
                EntityModel.of(dto, linkTo(methodOn(PizzaController.class).findById(dto.getId())).withSelfRel()));
        return ResponseEntity.ok(model);
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

    @GetMapping("/deleted")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<PizzaResponseDTO>> findDeleted() {
        return ResponseEntity.ok(pizzaService.findDeleted());
    }

    @PatchMapping("/{id}/restore")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PizzaResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(pizzaService.restore(id));
    }
}
