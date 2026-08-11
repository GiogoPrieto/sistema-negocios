package com.sistema.SistemaNegocios.repository;

import com.sistema.SistemaNegocios.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaContainingIgnoreCase(String categoria);

    // Obtiene solo los productos activos
    List<Producto> findByActivoTrue();

    @Modifying
    @Transactional
    @Query("DELETE FROM Producto p WHERE p.id = :id")
    void eliminarDirectoPorId(@Param("id") Long id);
}