package com.pizzaria.metrics;

import com.pizzaria.repository.PizzaRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CardapioMetrics {

    private final Counter loginAttempts;
    private final Counter loginFailures;

    public CardapioMetrics(MeterRegistry registry, PizzaRepository pizzaRepository) {
        this.loginAttempts = Counter.builder("cardapio.login.attempts")
                .description("Total de tentativas de login")
                .register(registry);
        this.loginFailures = Counter.builder("cardapio.login.failures")
                .description("Total de logins com credenciais inválidas")
                .register(registry);
        Gauge.builder("cardapio.pizzas.active", pizzaRepository, PizzaRepository::countNonDeleted)
                .description("Quantidade de pizzas ativas (não deletadas)")
                .register(registry);
    }

    public void incrementLoginAttempts() {
        loginAttempts.increment();
    }

    public void incrementLoginFailures() {
        loginFailures.increment();
    }
}
