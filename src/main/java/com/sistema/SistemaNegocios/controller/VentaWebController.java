package com.sistema.SistemaNegocios.controller;

import com.sistema.SistemaNegocios.model.Cliente;
import com.sistema.SistemaNegocios.model.DetalleVenta;
import com.sistema.SistemaNegocios.model.Producto;
import com.sistema.SistemaNegocios.model.Venta;
import com.sistema.SistemaNegocios.service.IClienteService;
import com.sistema.SistemaNegocios.service.IProductoService;
import com.sistema.SistemaNegocios.service.IVentaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ventas")
public class VentaWebController {

    private final IVentaService iVentaService;
    private final IProductoService iProductoService;
    private final IClienteService iClienteService;

    public VentaWebController(IVentaService iVentaService,
                              IProductoService iProductoService,
                              IClienteService iClienteService) {
        this.iVentaService = iVentaService;
        this.iProductoService = iProductoService;
        this.iClienteService = iClienteService;
    }

    @GetMapping
    public String listarVentas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                               Model model) {

        List<Venta> ventas;

        if (fechaInicio != null || fechaFin != null) {
            ventas = iVentaService.filtrarPorFechas(fechaInicio, fechaFin);
        } else {
            ventas = iVentaService.traerVentas();
        }

        double totalIngresado = ventas.stream()
                .filter(v -> v.getEstado() == null || !v.getEstado().equalsIgnoreCase("ANULADA"))
                .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0.0)
                .sum();

        long totalTransacciones = ventas.stream()
                .filter(v -> v.getEstado() == null || !v.getEstado().equalsIgnoreCase("ANULADA"))
                .count();

        double ticketPromedio = totalTransacciones > 0 ? totalIngresado / totalTransacciones : 0.0;

        model.addAttribute("ventas", ventas);
        model.addAttribute("listaVentas", ventas);
        model.addAttribute("totalIngresado", totalIngresado);
        model.addAttribute("totalTransacciones", totalTransacciones);
        model.addAttribute("ticketPromedio", ticketPromedio);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "ventas/lista";
    }

    // Mapea tanto /ventas/nueva como /ventas/formulario para prevenir errores 404
    @GetMapping({"/nueva", "/formulario"})
    public String mostrarFormularioNuevaVenta(Model model) {
        model.addAttribute("productos", iProductoService.traerProductosActivos());
        model.addAttribute("clientes", iClienteService.traerClientes());
        return "ventas/formulario";
    }

    @PostMapping("/guardar")
    public String guardarVenta(@RequestParam(name = "productoIds", required = false) List<Long> productoIds,
                               @RequestParam(name = "cantidades", required = false) List<Integer> cantidades,
                               @RequestParam(name = "clienteId", required = false) Long clienteId,
                               @RequestParam(name = "metodoPago", defaultValue = "EFECTIVO") String metodoPago) {

        if (productoIds != null && !productoIds.isEmpty()) {
            Venta venta = new Venta();
            venta.setFecha(java.time.LocalDateTime.now());
            venta.setEstado("COMPLETADA");
            venta.setMetodoPago(metodoPago != null ? metodoPago : "EFECTIVO");

            if (clienteId != null) {
                Cliente cliente = iClienteService.buscarPorId(clienteId);
                venta.setCliente(cliente);
            }

            double totalVenta = 0.0;
            List<DetalleVenta> detalles = new ArrayList<>();

            List<Producto> productosActivos = iProductoService.traerProductosActivos();

            for (int i = 0; i < productoIds.size(); i++) {
                Long pId = productoIds.get(i);
                Integer cant = cantidades.get(i);

                Producto producto = productosActivos.stream()
                        .filter(p -> p.getId().equals(pId))
                        .findFirst()
                        .orElse(null);

                if (producto != null) {
                    DetalleVenta detalle = new DetalleVenta();
                    detalle.setVenta(venta);
                    detalle.setProducto(producto);
                    detalle.setCantidad(cant);
                    detalle.setPrecioUnitario(producto.getPrecio());

                    double subtotal = producto.getPrecio() * cant;
                    detalle.setSubtotal(subtotal);

                    detalles.add(detalle);
                    totalVenta += subtotal;

                    producto.setStock(producto.getStock() - cant);
                    iProductoService.crearProducto(producto);
                }
            }

            venta.setTotal(totalVenta);
            venta.setDetalles(detalles);

            Venta ventaGuardada = iVentaService.crearVenta(venta);

            return "redirect:/ventas/ticket/" + ventaGuardada.getId();
        }

        return "redirect:/ventas";
    }

    @GetMapping("/ticket/{id}")
    public String verTicket(@PathVariable("id") Long id, Model model) {
        Venta venta = iVentaService.buscarPorId(id);
        model.addAttribute("venta", venta);
        return "ventas/ticket";
    }

    @PostMapping("/anular/{id}")
    public String anularVenta(@PathVariable("id") Long id, RedirectAttributes redirect) {
        Venta venta = iVentaService.buscarPorId(id);

        if (venta != null) {
            if (venta.getDetalles() != null) {
                for (DetalleVenta detalle : venta.getDetalles()) {
                    Producto producto = detalle.getProducto();
                    if (producto != null) {
                        producto.setStock(producto.getStock() + detalle.getCantidad());
                        iProductoService.crearProducto(producto);
                    }
                }
            }

            iVentaService.anularVenta(id);
            redirect.addFlashAttribute("exito", "Venta #" + id + " anulada correctamente y stock reincorporado.");
        } else {
            redirect.addFlashAttribute("error", "No se encontró la venta que se desea anular.");
        }

        return "redirect:/ventas";
    }
}