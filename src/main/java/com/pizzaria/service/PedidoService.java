package com.pizzaria.service;

import com.pizzaria.dto.ItemPedidoRequestDTO;
import com.pizzaria.dto.ItemPedidoResponseDTO;
import com.pizzaria.dto.PedidoRequestDTO;
import com.pizzaria.dto.PedidoResponseDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final PizzaRepository pizzaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoMapper pedidoMapper;

    @Transactional
    public PedidoResponseDTO criarPedido(String email, PedidoRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        List<ItemPedido> itens = request.getItens().stream()
                .map(itemReq -> criarItemPedido(itemReq, usuario))
                .collect(Collectors.toList());

        BigDecimal total = itens.stream()
                .map(ItemPedido::getPrecoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Pedido pedido = Pedido.builder()
                .usuario(usuario)
                .itens(itens)
                .status(StatusPedido.PENDENTE)
                .total(total)
                .dataCriacao(LocalDateTime.now())
                .build();

        itens.forEach(item -> item.setPedido(pedido));

        Pedido salvo = pedidoRepository.save(pedido);
        return pedidoMapper.toResponse(salvo);
    }

    private ItemPedido criarItemPedido(ItemPedidoRequestDTO itemReq, Usuario usuario) {
        Pizza pizza = pizzaRepository.findById(itemReq.getPizzaId())
                .orElseThrow(() -> new PizzaNotFoundException(itemReq.getPizzaId()));

        Tamanho tamanho = pizza.getTamanhos().stream()
                .filter(t -> t.getTipo() == itemReq.getTamanhoTipo())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tamanho " + itemReq.getTamanhoTipo() + " não disponível para esta pizza"));

        BigDecimal precoUnitario = tamanho.getPreco();
        BigDecimal precoTotal = precoUnitario.multiply(BigDecimal.valueOf(itemReq.getQuantidade()));

        return ItemPedido.builder()
                .pizza(pizza)
                .tamanhoTipo(itemReq.getTamanhoTipo())
                .quantidade(itemReq.getQuantidade())
                .precoUnitario(precoUnitario)
                .precoTotal(precoTotal)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarPedidosUsuario(String email, Pageable pageable) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return pedidoRepository.findByUsuarioId(usuario.getId(), pageable).map(pedidoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedido(Long id, String email) {
        Pedido pedido = pedidoRepository.findByIdWithItens(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!pedido.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Pedido não pertence ao usuário");
        }

        return pedidoMapper.toResponse(pedido);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarTodosPedidos(Pageable pageable) {
        return pedidoRepository.findAll(pageable).map(pedidoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarPedidosPorStatus(StatusPedido status, Pageable pageable) {
        return pedidoRepository.findByStatus(status, pageable).map(pedidoMapper::toResponse);
    }

    @Transactional
    public PedidoResponseDTO atualizarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));

        pedido.setStatus(novoStatus);
        pedido.setDataAtualizacao(LocalDateTime.now());

        return pedidoMapper.toResponse(pedido);
    }
}