package com.pizzaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PizzariaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PizzariaApplication.class, args);
    }
}
