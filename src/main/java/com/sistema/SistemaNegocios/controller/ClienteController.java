package com.sistema.SistemaNegocios.controller;

import com.sistema.SistemaNegocios.model.Cliente;
import com.sistema.SistemaNegocios.model.Venta;
import com.sistema.SistemaNegocios.service.IClienteService;
import com.sistema.SistemaNegocios.service.IVentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final IClienteService clienteService;
    private final IVentaService ventaService;

    public ClienteController(IClienteService clienteService, IVentaService ventaService) {
        this.clienteService = clienteService;
        this.ventaService = ventaService;
    }

    // Listar clientes en HTML
    @GetMapping
    public String listarClientes(Model model) {
        List<Cliente> clientes = clienteService.traerClientes();
        model.addAttribute("clientes", clientes);
        return "clientes/lista";
    }

    // Guardar o Editar cliente desde formulario tradicional
    @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute Cliente cliente) {
        clienteService.guardarCliente(cliente);
        return "redirect:/clientes";
    }

    // Historial y consumo acumulado de un cliente específico
    @GetMapping("/historial/{id}")
    public String historialCliente(@PathVariable("id") Long id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id);
        if (cliente == null) {
            return "redirect:/clientes";
        }

        // Obtener todas las ventas asociadas a este cliente
        List<Venta> compras = ventaService.traerVentas().stream()
                .filter(v -> v.getCliente() != null && v.getCliente().getId().equals(id))
                .filter(v -> v.getEstado() == null || !v.getEstado().equalsIgnoreCase("ANULADA"))
                .collect(Collectors.toList());

        double totalConsumido = compras.stream()
                .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0.0)
                .sum();

        model.addAttribute("cliente", cliente);
        model.addAttribute("compras", compras);
        model.addAttribute("totalConsumido", String.format("%.0f", totalConsumido));
        model.addAttribute("totalCompras", compras.size());

        return "clientes/historial";
    }

    // Endpoint AJAX para guardar cliente desde el Modal de Ventas
    @PostMapping("/guardar-ajax")
    @ResponseBody
    public ResponseEntity<Cliente> guardarClienteAjax(@RequestBody Cliente cliente) {
        Cliente clienteGuardado = clienteService.guardarCliente(cliente);
        return ResponseEntity.ok(clienteGuardado);
    }
}