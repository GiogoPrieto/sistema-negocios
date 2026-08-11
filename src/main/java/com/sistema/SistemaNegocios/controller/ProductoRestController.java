package com.sistema.SistemaNegocios.controller;

import com.sistema.SistemaNegocios.model.Producto;
import com.sistema.SistemaNegocios.service.IProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")

public class ProductoRestController {

    private final IProductoService productoService;

    public ProductoRestController(IProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping

    public List<Producto> traeProductos() {
        return productoService.traerProductos();
    }

    @GetMapping("/{id}")

    public ResponseEntity<?> buscarProducto(@PathVariable Long id) {

        Producto producto = productoService.buscarProducto(id);
        if (producto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encuentra un producto con ese ID");
        }
        return ResponseEntity.ok(producto);

    }

    @PostMapping

    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        Producto productoCreado = productoService.crearProducto(producto);

        if (productoCreado == null) {
            return ResponseEntity.badRequest()
                    .body("Los datos del producto no son validos");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoCreado);

    }

    @PutMapping("/{id}")

    public ResponseEntity<?> editarProducto(@PathVariable Long id, @RequestBody Producto productoAModificar) {

        Producto productoEditado = productoService.editarProducto(id, productoAModificar);

        if (productoEditado == null) {
            return ResponseEntity.badRequest()
                    .body("No fue posible editar el producto");
        }

        return ResponseEntity.ok(productoEditado);

    }

    @DeleteMapping("/{id}")

    public ResponseEntity<String> eliminarProducto(@PathVariable Long id) {
        boolean eliminido = productoService.eliminarProducto(id);
        if (eliminido == false) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encuentra un producto con ese ID");
        }

        return ResponseEntity.ok("Producto eliminado correctamente");
    }

}






