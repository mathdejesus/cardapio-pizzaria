package com.pizzaria.model;

import com.pizzaria.enums.Categoria;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pizzas")
@SQLRestriction("deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pizza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    @ElementCollection
    @CollectionTable(name = "pizza_tamanhos", joinColumns = @JoinColumn(name = "pizza_id"))
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private List<Tamanho> tamanhos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "pizza_ingredientes", joinColumns = @JoinColumn(name = "pizza_id"))
    @Column(name = "ingrediente", nullable = false)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private List<String> ingredientes = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean disponivel = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
