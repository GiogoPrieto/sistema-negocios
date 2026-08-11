package com.sistema.SistemaNegocios.service;

import com.sistema.SistemaNegocios.model.Producto;
import com.sistema.SistemaNegocios.repository.IProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService implements IProductoService {

    private final IProductoRepository productoRepository;

    public ProductoService(IProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public List<Producto> traerProductos() {
        return productoRepository.findAll();
    }

    @Override
    public List<Producto> traerProductosActivos() {
        // Retorna únicamente los productos cuyo atributo 'activo' sea true o no sea nulo
        // Si tienes findByActivoTrue() en IProductoRepository, puedes usarlo directamente:
        // return productoRepository.findByActivoTrue();

        return productoRepository.findAll().stream()
                .filter(p -> p.getActivo() != null && p.getActivo())
                .toList();
    }

    @Override
    public Producto crearProducto(Producto producto) {
        if (!validarProducto(producto)) {
            return null;
        }
        producto.setActivo(true);
        return productoRepository.save(producto);
    }

    @Override
    public Producto editarProducto(Long id, Producto producto) {
        Producto existente = buscarProducto(id);
        if (existente == null || !validarProducto(producto)) {
            return null;
        }

        existente.setCodigoBarras(producto.getCodigoBarras());
        existente.setNombre(producto.getNombre());
        existente.setMarca(producto.getMarca());
        existente.setCategoria(producto.getCategoria());
        existente.setPrecio(producto.getPrecio());
        existente.setPrecioCosto(producto.getPrecioCosto());
        existente.setIva(producto.getIva());
        existente.setStock(producto.getStock());
        existente.setFechaVencimiento(producto.getFechaVencimiento());
        existente.setDescripcion(producto.getDescripcion());
        // Mantiene el valor actual de activo si no viene informado desde el formulario
        if (existente.getActivo() == null) {
            existente.setActivo(true);
        }

        return productoRepository.save(existente);
    }

    @Override
    public Producto buscarProducto(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminarProducto(Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.eliminarDirectoPorId(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Producto> buscarPorIdOCategoria(Long id, String categoria) {
        if (id != null) {
            Producto p = productoRepository.findById(id).orElse(null);
            return p != null ? List.of(p) : List.of();
        }

        if (categoria != null && !categoria.trim().isEmpty()) {
            return productoRepository.findByCategoriaContainingIgnoreCase(categoria);
        }

        return productoRepository.findAll();
    }

    private boolean validarProducto(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) return false;
        if (producto.getMarca() == null || producto.getMarca().isBlank()) return false;
        if (producto.getCategoria() == null || producto.getCategoria().isBlank()) return false;
        if (producto.getPrecio() == null || producto.getPrecio() <= 0) return false;
        if (producto.getStock() == null || producto.getStock() < 0) return false;
        return true;
    }
}