package com.pizzaria.controller;

import com.pizzaria.config.TestConfig;
import com.pizzaria.dto.ItemPedidoRequestDTO;
import com.pizzaria.dto.PedidoRequestDTO;
import com.pizzaria.dto.PedidoResponseDTO;
import com.pizzaria.enums.StatusPedido;
import com.pizzaria.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class PedidoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PedidoService pedidoService;

    @Autowired
    ObjectMapper objectMapper;

    private PedidoRequestDTO createValidRequest() {
        return PedidoRequestDTO.builder()
                .itens(List.of(ItemPedidoRequestDTO.builder()
                        .pizzaId(1L)
                        .tamanhoTipo(com.pizzaria.enums.TamanhoTipo.MEDIA)
                        .quantidade(2)
                        .build()))
                .build();
    }

    @Test
    void criarPedido_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@pizzaria.com", roles = "USER")
    void criarPedido_asUser_shouldReturn201() throws Exception {
        PedidoResponseDTO response = PedidoResponseDTO.builder()
                .id(10L)
                .usuarioId(1L)
                .status(StatusPedido.PENDENTE)
                .build();

        when(pedidoService.criarPedido(eq("user@pizzaria.com"), any(PedidoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/pedidos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser(username = "user@pizzaria.com", roles = "USER")
    void listarMeusPedidos_asUser_shouldReturn200() throws Exception {
        PedidoResponseDTO dto = PedidoResponseDTO.builder().id(1L).build();
        when(pedidoService.listarPedidosUsuario(eq("user@pizzaria.com"), any()))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pedidoResponseDTOList[0].id").exists());
    }

    @Test
    @WithMockUser(username = "user@pizzaria.com", roles = "USER")
    void listarTodosPedidos_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/pedidos/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@pizzaria.com", roles = "ADMIN")
    void atualizarStatus_asAdmin_shouldReturn200() throws Exception {
        PedidoResponseDTO response = PedidoResponseDTO.builder()
                .id(1L)
                .status(StatusPedido.CONFIRMADO)
                .build();
        when(pedidoService.atualizarStatus(eq(1L), eq(StatusPedido.CONFIRMADO))).thenReturn(response);

        mockMvc.perform(patch("/api/pedidos/admin/1/status")
                        .param("status", "CONFIRMADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));
    }

    @Test
    @WithMockUser(username = "admin@pizzaria.com", roles = "ADMIN")
    void buscarPedidoAdmin_asAdmin_shouldReturn200() throws Exception {
        PedidoResponseDTO response = PedidoResponseDTO.builder()
                .id(1L)
                .usuarioId(1L)
                .status(StatusPedido.PENDENTE)
                .build();
        when(pedidoService.buscarPedidoAdmin(eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/pedidos/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioId").value(1));
    }

    @Test
    @WithMockUser(username = "user@pizzaria.com", roles = "USER")
    void buscarPedidoAdmin_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/pedidos/admin/1"))
                .andExpect(status().isForbidden());
    }
}
