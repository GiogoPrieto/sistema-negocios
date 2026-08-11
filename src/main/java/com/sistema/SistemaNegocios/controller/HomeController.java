package com.sistema.SistemaNegocios.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        // Cambia "login" o "dashboard" por el nombre de tu vista principal en src/main/resources/templates
        return "redirect:/productos";
    }
}