package com.pizzaria.config;

import com.pizzaria.enums.Categoria;
import com.pizzaria.enums.Role;
import com.pizzaria.enums.TamanhoTipo;
import com.pizzaria.model.Pizza;
import com.pizzaria.model.Tamanho;
import com.pizzaria.model.Usuario;
import com.pizzaria.repository.PizzaRepository;
import com.pizzaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final PizzaRepository pizzaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedPizzas();
    }

    private void seedAdmin() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        // Dev: admin@pizzaria.com / admin123
        usuarioRepository.save(Usuario.builder()
                .email("admin@pizzaria.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());
    }

    private void seedPizzas() {
        if (pizzaRepository.count() > 0) {
            return;
        }

        List<PizzaSeed> seeds = List.of(
                // SALGADAS
                seed("Mussarela", "Clássica pizza de mussarela com molho de tomate e orégano.",
                        Categoria.SALGADA, List.of("Mussarela", "Molho de tomate", "Orégano"), false),
                seed("Calabresa", "Calabresa fatiada com cebola e azeitonas.",
                        Categoria.SALGADA, List.of("Calabresa", "Cebola", "Mussarela", "Azeitona"), false),
                seed("Frango com Catupiry", "Frango desfiado temperado com catupiry cremoso.",
                        Categoria.SALGADA, List.of("Frango", "Catupiry", "Mussarela", "Molho de tomate"), false),
                seed("Portuguesa", "Presunto, ovos, cebola, ervilha, mussarela e azeitonas.",
                        Categoria.SALGADA, List.of("Presunto", "Ovo", "Cebola", "Ervilha", "Mussarela", "Azeitona"), false),
                seed("Quatro Queijos", "Mussarela, provolone, gorgonzola e parmesão.",
                        Categoria.SALGADA, List.of("Mussarela", "Provolone", "Gorgonzola", "Parmesão"), false),
                seed("Pepperoni", "Pepperoni com mussarela e molho de tomate.",
                        Categoria.SALGADA, List.of("Pepperoni", "Mussarela", "Molho de tomate"), false),
                seed("Marguerita", "Molho de tomate, mussarela, tomate fresco e manjericão.",
                        Categoria.SALGADA, List.of("Mussarela", "Tomate", "Manjericão", "Molho de tomate"), false),
                seed("Palmito", "Palmito picado com mussarela e molho branco.",
                        Categoria.SALGADA, List.of("Palmito", "Mussarela", "Molho branco"), false),
                seed("Carne Seca com Cebola", "Carne seca desfiada com cebola caramelizada e mussarela.",
                        Categoria.SALGADA, List.of("Carne seca", "Cebola", "Mussarela"), false),
                seed("Frango com Requeijão", "Frango desfiado com requeijão cremoso.",
                        Categoria.SALGADA, List.of("Frango", "Requeijão", "Mussarela"), false),
                seed("Bacon com Cheddar", "Bacon crocante com cheddar derretido.",
                        Categoria.SALGADA, List.of("Bacon", "Cheddar", "Mussarela"), false),
                seed("Atum", "Atum sólido com cebola e azeitonas.",
                        Categoria.SALGADA, List.of("Atum", "Cebola", "Mussarela", "Azeitona"), false),
                seed("Vegetariana", "Seleção de legumes grelhados com mussarela.",
                        Categoria.SALGADA, List.of("Berinjela", "Abobrinha", "Pimentão", "Mussarela", "Tomate"), false),
                seed("Alho e Óleo com Bacon", "Alho refogado no azeite com bacon crocante.",
                        Categoria.SALGADA, List.of("Alho", "Azeite", "Bacon", "Mussarela"), false),
                // DOCES
                seed("Chocolate", "Chocolate ao leite derretido sobre massa tradicional.",
                        Categoria.DOCE, List.of("Chocolate ao leite"), true),
                seed("Chocolate com Morango", "Chocolate com morangos frescos.",
                        Categoria.DOCE, List.of("Chocolate", "Morango"), true),
                seed("Banana com Canela", "Banana caramelizada com canela e açúcar.",
                        Categoria.DOCE, List.of("Banana", "Canela", "Açúcar"), true),
                seed("Romeu e Julieta", "Goiabada com queijo minas em fatias.",
                        Categoria.DOCE, List.of("Goiabada", "Queijo minas"), true),
                seed("Prestigio", "Chocolate com coco ralado.",
                        Categoria.DOCE, List.of("Chocolate", "Coco ralado"), true),
                seed("Nutella com Morango", "Nutella com morangos frescos.",
                        Categoria.DOCE, List.of("Nutella", "Morango"), true)
        );

        seeds.forEach(s -> pizzaRepository.save(Pizza.builder()
                .nome(s.nome())
                .descricao(s.descricao())
                .categoria(s.categoria())
                .ingredientes(s.ingredientes())
                .tamanhos(buildTamanhos(s.doce()))
                .disponivel(true)
                .deleted(false)
                .build()));
    }

    private List<Tamanho> buildTamanhos(boolean doce) {
        if (doce) {
            return List.of(
                    tamanho(TamanhoTipo.PEQUENA, new BigDecimal("32.90"), 4),
                    tamanho(TamanhoTipo.MEDIA, new BigDecimal("44.90"), 6),
                    tamanho(TamanhoTipo.GRANDE, new BigDecimal("54.90"), 8),
                    tamanho(TamanhoTipo.FAMILIA, new BigDecimal("72.90"), 12)
            );
        }
        return List.of(
                tamanho(TamanhoTipo.PEQUENA, new BigDecimal("35.90"), 4),
                tamanho(TamanhoTipo.MEDIA, new BigDecimal("49.90"), 6),
                tamanho(TamanhoTipo.GRANDE, new BigDecimal("59.90"), 8),
                tamanho(TamanhoTipo.FAMILIA, new BigDecimal("79.90"), 12)
        );
    }

    private Tamanho tamanho(TamanhoTipo tipo, BigDecimal preco, int fatias) {
        return Tamanho.builder().tipo(tipo).preco(preco).fatias(fatias).build();
    }

    private PizzaSeed seed(String nome, String descricao, Categoria categoria,
                           List<String> ingredientes, boolean doce) {
        return new PizzaSeed(nome, descricao, categoria, ingredientes, doce);
    }

    private record PizzaSeed(String nome, String descricao, Categoria categoria,
                             List<String> ingredientes, boolean doce) {
    }
}
