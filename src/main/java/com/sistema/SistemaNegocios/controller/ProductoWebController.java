package com.sistema.SistemaNegocios.controller;

import com.sistema.SistemaNegocios.model.Producto;
import com.sistema.SistemaNegocios.repository.IProductoRepository;
import com.sistema.SistemaNegocios.service.*;
import com.sistema.SistemaNegocios.util.ImportadorService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional; // Importante para confirmar la actualización
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductoWebController {

    private final IProductoService productoService;
    private final ImportadorService importadorService;
    private final IProductoRepository iProductoRepository;

    public ProductoWebController(IProductoService productoService, ImportadorService importadorService, IProductoRepository iProductoRepository) {
        this.productoService = productoService;
        this.importadorService = importadorService;
        this.iProductoRepository = iProductoRepository;
    }

    @GetMapping
    public String listarProductos(@RequestParam(required = false) Long id,
                                  @RequestParam(required = false) String nombre,
                                  @RequestParam(required = false) String categoria,
                                  @RequestParam(required = false) String vencimiento,
                                  Model model) {

        List<Producto> todos = productoService.traerProductos();
        LocalDate hoy = LocalDate.now();

        // Obtener las categorías únicas de los productos activos
        List<String> categoriasExistentes = todos.stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .map(Producto::getCategoria)
                .filter(c -> c != null && !c.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();

        // Filtrar por estado ACTIVO (Evitar NullPointer y asegurar filtrado)
        List<Producto> productos = todos.stream()
                .filter(p -> p.getActivo() == null || Boolean.TRUE.equals(p.getActivo()))
                .filter(p -> id == null || p.getId().equals(id))
                .filter(p -> nombre == null || nombre.trim().isEmpty() ||
                        (p.getNombre() != null && p.getNombre().toLowerCase().contains(nombre.trim().toLowerCase())))
                .filter(p -> categoria == null || categoria.trim().isEmpty() ||
                        (p.getCategoria() != null && p.getCategoria().equalsIgnoreCase(categoria.trim())))
                .filter(p -> {
                    if (vencimiento == null || vencimiento.trim().isEmpty()) {
                        return true;
                    }
                    if (p.getFechaVencimiento() == null) {
                        return false;
                    }

                    long diasRestantes = ChronoUnit.DAYS.between(hoy, p.getFechaVencimiento());

                    switch (vencimiento) {
                        case "7":
                            return diasRestantes >= 0 && diasRestantes <= 7;
                        case "15":
                            return diasRestantes >= 0 && diasRestantes <= 15;
                        case "30":
                            return diasRestantes >= 0 && diasRestantes <= 30;
                        default:
                            return true;
                    }
                })
                .toList();

        model.addAttribute("productos", productos);
        model.addAttribute("listaProductos", productos);
        model.addAttribute("categoriasExistentes", categoriasExistentes);

        model.addAttribute("idBusqueda", id);
        model.addAttribute("nombreBusqueda", nombre);
        model.addAttribute("categoriaBusqueda", categoria);
        model.addAttribute("vencimientoBusqueda", vencimiento);

        // ATENCIÓN: Si tu archivo HTML está directamente en "templates/lista.html", retorna "lista".
        // Si está en "templates/productos/lista.html", déjalo como "productos/lista".
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/formulario"; // Ajustado según la ruta raíz de templates
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto) {
        if (producto.getId() == null) {
            producto.setActivo(true);
            productoService.crearProducto(producto);
        } else {
            productoService.editarProducto(producto.getId(), producto);
        }
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Producto producto = productoService.buscarProducto(id);
        if (producto == null) {
            return "redirect:/productos";
        }
        model.addAttribute("producto", producto);
        return "productos/formulario"; // Ajustado según la ruta raíz de templates
    }

    // ELIMINAR CORREGIDO CON CONTEXTO TRANSACCIONAL
    @PostMapping("/eliminar/{id}")
    @Transactional
    public String eliminarProducto(@PathVariable("id") Long id, RedirectAttributes redirect) {
        Producto prod = iProductoRepository.findById(id).orElse(null);
        if (prod != null) {
            prod.setActivo(false);
            iProductoRepository.saveAndFlush(prod);
            redirect.addFlashAttribute("exito", "Producto eliminado correctamente.");
        } else {
            redirect.addFlashAttribute("error", "No se encontró el producto a eliminar.");
        }
        return "redirect:/productos";
    }

    @PostMapping("/importar")
    public String importarProductos(@RequestParam("archivo") MultipartFile archivo, RedirectAttributes redirect) {
        if (archivo.isEmpty()) {
            redirect.addFlashAttribute("error", "Por favor selecciona un archivo Excel válido.");
            return "redirect:/productos";
        }

        try {
            importadorService.importarProductosDesdeExcel(archivo);
            redirect.addFlashAttribute("exito", "¡Productos importados correctamente desde el archivo!");
        } catch (Exception e) {
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Error al procesar el archivo Excel: " + e.getMessage());
        }

        return "redirect:/productos";
    }
}