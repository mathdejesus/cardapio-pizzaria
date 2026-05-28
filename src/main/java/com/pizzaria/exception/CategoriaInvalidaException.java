package com.pizzaria.exception;

public class CategoriaInvalidaException extends RuntimeException {

    public CategoriaInvalidaException(String categoria) {
        super("Categoria inválida: " + categoria + ". Use SALGADA ou DOCE.");
    }
}
