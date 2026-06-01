package com.pizzaria.exception;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Muitas tentativas de login. Aguarde e tente novamente.");
    }
}
