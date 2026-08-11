package com.sistema.SistemaNegocios.service;

import com.sistema.SistemaNegocios.model.Producto;
import java.util.List;

public interface IProductoService {

    List<Producto> traerProductos();

    List<Producto> traerProductosActivos();

    Producto crearProducto(Producto producto);

    Producto editarProducto(Long id, Producto producto);

    Producto buscarProducto(Long id);

    boolean eliminarProducto(Long id);

    List<Producto> buscarPorIdOCategoria(Long id, String categoria);
}