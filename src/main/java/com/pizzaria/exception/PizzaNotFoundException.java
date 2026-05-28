package com.pizzaria.exception;

public class PizzaNotFoundException extends RuntimeException {

    public PizzaNotFoundException(Long id) {
        super("Pizza não encontrada com id: " + id);
    }
}
