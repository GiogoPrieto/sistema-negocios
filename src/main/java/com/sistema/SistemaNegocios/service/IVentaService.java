package com.sistema.SistemaNegocios.service;

import com.sistema.SistemaNegocios.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IVentaService {

    List<Venta> traerVentas();

    Page<Venta> traerVentasPaginadas(Pageable pageable);

    Venta buscarVenta(Long id);

    Venta buscarPorId(Long id);

    List<Venta> filtrarPorFechas(LocalDate fechaInicio, LocalDate fechaFin);

    boolean anularVenta(Long id);

    Venta crearVenta(Venta venta);
}