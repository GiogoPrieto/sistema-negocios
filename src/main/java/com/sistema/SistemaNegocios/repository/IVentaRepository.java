package com.sistema.SistemaNegocios.repository;

import com.sistema.SistemaNegocios.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IVentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Venta> findByFechaAfter(LocalDateTime inicio);

    List<Venta> findByFechaBefore(LocalDateTime fin);
    // Total por método de pago específico
    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fecha >= :fechaApertura AND UPPER(v.metodoPago) = UPPER(:metodoPago) AND (v.estado IS NULL OR UPPER(v.estado) <> 'ANULADA')")
    Double obtenerTotalPorMetodoDesde(@Param("fechaApertura") LocalDateTime fechaApertura, @Param("metodoPago") String metodoPago);

    // Total general de ventas (todos los métodos combinados)
    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fecha >= :fechaApertura AND (v.estado IS NULL OR UPPER(v.estado) <> 'ANULADA')")
    Double obtenerTotalVentasDesde(@Param("fechaApertura") LocalDateTime fechaApertura);
}
