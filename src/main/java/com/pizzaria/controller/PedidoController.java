package com.pizzaria.controller;

import com.pizzaria.dto.ItemPedidoResponseDTO;
import com.pizzaria.dto.PedidoRequestDTO;
import com.pizzaria.dto.PedidoResponseDTO;
import com.pizzaria.enums.StatusPedido;
import com.pizzaria.service.PedidoService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final PagedResourcesAssembler<PedidoResponseDTO> pagedResourcesAssembler;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PedidoResponseDTO> criarPedido(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody PedidoRequestDTO request) {
        PedidoResponseDTO response = pedidoService.criarPedido(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PagedModel<EntityModel<PedidoResponseDTO>>> listarMeusPedidos(
            @AuthenticationPrincipal UserDetails user,
            @Parameter(hidden = true) Pageable pageable) {
        Page<PedidoResponseDTO> page = pedidoService.listarPedidosUsuario(user.getUsername(), pageable);
        PagedModel<EntityModel<PedidoResponseDTO>> model = pagedResourcesAssembler.toModel(page, dto ->
                EntityModel.of(dto, linkTo(methodOn(PedidoController.class).buscarPedido(dto.getId(), user)).withSelfRel()));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EntityModel<PedidoResponseDTO>> buscarPedido(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        PedidoResponseDTO dto = pedidoService.buscarPedido(id, user.getUsername());
        EntityModel<PedidoResponseDTO> model = EntityModel.of(dto,
                linkTo(methodOn(PedidoController.class).buscarPedido(id, user)).withSelfRel(),
                linkTo(methodOn(PedidoController.class).listarMeusPedidos(user, Pageable.unpaged())).withRel("pedidos"));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/admin")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PagedModel<EntityModel<PedidoResponseDTO>>> listarTodosPedidos(
            @Parameter(hidden = true) Pageable pageable) {
        Page<PedidoResponseDTO> page = pedidoService.listarTodosPedidos(pageable);
        PagedModel<EntityModel<PedidoResponseDTO>> model = pagedResourcesAssembler.toModel(page, dto ->
                EntityModel.of(dto, linkTo(methodOn(PedidoController.class).buscarPedidoAdminEndpoint(dto.getId())).withSelfRel()));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/admin/status/{status}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PagedModel<EntityModel<PedidoResponseDTO>>> listarPedidosPorStatus(
            @PathVariable StatusPedido status,
            @Parameter(hidden = true) Pageable pageable) {
        Page<PedidoResponseDTO> page = pedidoService.listarPedidosPorStatus(status, pageable);
        PagedModel<EntityModel<PedidoResponseDTO>> model = pagedResourcesAssembler.toModel(page, dto ->
                EntityModel.of(dto, linkTo(methodOn(PedidoController.class).buscarPedidoAdminEndpoint(dto.getId())).withSelfRel()));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/admin/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EntityModel<PedidoResponseDTO>> buscarPedidoAdminEndpoint(@PathVariable Long id) {
        PedidoResponseDTO dto = pedidoService.buscarPedidoAdmin(id);
        EntityModel<PedidoResponseDTO> model = EntityModel.of(dto,
                linkTo(methodOn(PedidoController.class).buscarPedidoAdminEndpoint(id)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @PatchMapping("/admin/{id}/status")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusPedido status) {
        PedidoResponseDTO response = pedidoService.atualizarStatus(id, status);
        return ResponseEntity.ok(response);
    }
}