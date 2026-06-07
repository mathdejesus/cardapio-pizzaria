package com.pizzaria.service;

import com.pizzaria.dto.ItemPedidoRequestDTO;
import com.pizzaria.dto.PedidoRequestDTO;
import com.pizzaria.dto.PedidoResponseDTO;
import com.pizzaria.enums.Categoria;
import com.pizzaria.enums.StatusPedido;
import com.pizzaria.enums.TamanhoTipo;
import com.pizzaria.exception.PedidoNotFoundException;
import com.pizzaria.exception.PizzaNotFoundException;
import com.pizzaria.mapper.PedidoMapper;
import com.pizzaria.model.ItemPedido;
import com.pizzaria.model.Pedido;
import com.pizzaria.model.Pizza;
import com.pizzaria.model.Tamanho;
import com.pizzaria.model.Usuario;
import com.pizzaria.repository.ItemPedidoRepository;
import com.pizzaria.repository.PedidoRepository;
import com.pizzaria.repository.PizzaRepository;
import com.pizzaria.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    PedidoRepository pedidoRepository;

    @Mock
    ItemPedidoRepository itemPedidoRepository;

    @Mock
    PizzaRepository pizzaRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    PedidoMapper pedidoMapper;

    @InjectMocks
    PedidoService pedidoService;

    private Pizza createPizza(Long id) {
        return Pizza.builder()
                .id(id)
                .nome("Margherita")
                .descricao("Mussarela, tomate e manjericão")
                .categoria(Categoria.SALGADA)
                .disponivel(true)
                .deleted(false)
                .tamanhos(List.of(
                        Tamanho.builder().tipo(TamanhoTipo.PEQUENA).preco(BigDecimal.valueOf(30)).fatias(4).build(),
                        Tamanho.builder().tipo(TamanhoTipo.MEDIA).preco(BigDecimal.valueOf(40)).fatias(6).build()
                ))
                .build();
    }

    private Usuario createUsuario(Long id) {
        return Usuario.builder()
                .id(id)
                .email("user@pizzaria.com")
                .senha("hashed")
                .role(com.pizzaria.enums.Role.USER)
                .build();
    }

    private PedidoRequestDTO createRequest(Long pizzaId, TamanhoTipo tipo, int qtd) {
        return PedidoRequestDTO.builder()
                .itens(List.of(ItemPedidoRequestDTO.builder()
                        .pizzaId(pizzaId)
                        .tamanhoTipo(tipo)
                        .quantidade(qtd)
                        .build()))
                .build();
    }

    @Test
    void criarPedido_shouldComputeTotalAndPersist() {
        Usuario usuario = createUsuario(1L);
        Pizza pizza = createPizza(1L);
        Pedido saved = Pedido.builder().id(10L).usuario(usuario).status(StatusPedido.PENDENTE).build();
        PedidoResponseDTO responseDTO = PedidoResponseDTO.builder().id(10L).build();

        when(usuarioRepository.findByEmail("user@pizzaria.com")).thenReturn(Optional.of(usuario));
        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(pizza));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(saved);
        when(pedidoMapper.toResponse(saved)).thenReturn(responseDTO);

        PedidoResponseDTO result = pedidoService.criarPedido("user@pizzaria.com", createRequest(1L, TamanhoTipo.MEDIA, 2));

        assertThat(result.getId()).isEqualTo(10L);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        Pedido persisted = captor.getValue();
        assertThat(persisted.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(80));
        assertThat(persisted.getStatus()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(persisted.getItens()).hasSize(1);
        assertThat(persisted.getItens().get(0).getPrecoUnitario()).isEqualByComparingTo(BigDecimal.valueOf(40));
        assertThat(persisted.getItens().get(0).getPrecoTotal()).isEqualByComparingTo(BigDecimal.valueOf(80));
    }

    @Test
    void criarPedido_shouldThrowWhenPizzaNotFound() {
        Usuario usuario = createUsuario(1L);
        when(usuarioRepository.findByEmail("user@pizzaria.com")).thenReturn(Optional.of(usuario));
        when(pizzaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criarPedido("user@pizzaria.com", createRequest(99L, TamanhoTipo.MEDIA, 1)))
                .isInstanceOf(PizzaNotFoundException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void criarPedido_shouldThrowWhenTamanhoNotAvailableForPizza() {
        Usuario usuario = createUsuario(1L);
        Pizza pizza = createPizza(1L);
        when(usuarioRepository.findByEmail("user@pizzaria.com")).thenReturn(Optional.of(usuario));
        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(pizza));

        assertThatThrownBy(() -> pedidoService.criarPedido("user@pizzaria.com", createRequest(1L, TamanhoTipo.FAMILIA, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FAMILIA");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void buscarPedido_shouldThrowWhenNotFound() {
        when(pedidoRepository.findByIdWithItens(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.buscarPedido(99L, "user@pizzaria.com"))
                .isInstanceOf(PedidoNotFoundException.class);
    }

    @Test
    void buscarPedido_shouldThrowWhenOwnerMismatch() {
        Usuario owner = createUsuario(1L);
        Pedido pedido = Pedido.builder().id(1L).usuario(owner).build();
        when(pedidoRepository.findByIdWithItens(1L)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByEmail("other@pizzaria.com")).thenReturn(Optional.of(createUsuario(2L)));

        assertThatThrownBy(() -> pedidoService.buscarPedido(1L, "other@pizzaria.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pertence");
    }

    @Test
    void listarPedidosUsuario_shouldDelegateToRepository() {
        Usuario usuario = createUsuario(1L);
        Pedido pedido = Pedido.builder().id(1L).usuario(usuario).build();
        Page<Pedido> page = new PageImpl<>(List.of(pedido));
        Pageable pageable = PageRequest.of(0, 10);
        PedidoResponseDTO dto = PedidoResponseDTO.builder().id(1L).build();

        when(usuarioRepository.findByEmail("user@pizzaria.com")).thenReturn(Optional.of(usuario));
        when(pedidoRepository.findByUsuarioId(1L, pageable)).thenReturn(page);
        when(pedidoMapper.toResponse(pedido)).thenReturn(dto);

        Page<PedidoResponseDTO> result = pedidoService.listarPedidosUsuario("user@pizzaria.com", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void atualizarStatus_shouldSetNewStatusAndTimestamp() {
        Pedido pedido = Pedido.builder().id(1L).status(StatusPedido.PENDENTE).build();
        PedidoResponseDTO dto = PedidoResponseDTO.builder().id(1L).status(StatusPedido.CONFIRMADO).build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoMapper.toResponse(pedido)).thenReturn(dto);

        PedidoResponseDTO result = pedidoService.atualizarStatus(1L, StatusPedido.CONFIRMADO);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
        assertThat(pedido.getDataAtualizacao()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
    }

    @Test
    void listarPedidosPorStatus_shouldDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Pedido pedido = Pedido.builder().id(1L).status(StatusPedido.PENDENTE).build();
        Page<Pedido> page = new PageImpl<>(List.of(pedido));
        PedidoResponseDTO dto = PedidoResponseDTO.builder().id(1L).build();

        when(pedidoRepository.findByStatus(StatusPedido.PENDENTE, pageable)).thenReturn(page);
        when(pedidoMapper.toResponse(pedido)).thenReturn(dto);

        Page<PedidoResponseDTO> result = pedidoService.listarPedidosPorStatus(StatusPedido.PENDENTE, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void buscarPedidoAdmin_shouldReturnAnyUserPedidoWithoutOwnershipCheck() {
        Usuario owner = createUsuario(1L);
        Pedido pedido = Pedido.builder().id(1L).usuario(owner).status(StatusPedido.PENDENTE).build();
        PedidoResponseDTO dto = PedidoResponseDTO.builder().id(1L).usuarioId(1L).build();

        when(pedidoRepository.findByIdWithItens(1L)).thenReturn(Optional.of(pedido));
        when(pedidoMapper.toResponse(pedido)).thenReturn(dto);

        PedidoResponseDTO result = pedidoService.buscarPedidoAdmin(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsuarioId()).isEqualTo(1L);
        verify(usuarioRepository, never()).findByEmail(any());
    }

    @Test
    void buscarPedidoAdmin_shouldThrowWhenNotFound() {
        when(pedidoRepository.findByIdWithItens(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.buscarPedidoAdmin(99L))
                .isInstanceOf(PedidoNotFoundException.class);
    }
}
