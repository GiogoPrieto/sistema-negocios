package com.sistema.SistemaNegocios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.sistema.SistemaNegocios")
@EnableJpaRepositories(basePackages = "com.sistema.SistemaNegocios.repository")
public class SistemaNegociosApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaNegociosApplication.class, args);
    }
}