package com.sistema.SistemaNegocios.service;

import com.sistema.SistemaNegocios.model.Venta;
import com.sistema.SistemaNegocios.repository.IVentaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class VentaService implements IVentaService {

    private final IVentaRepository ventaRepository;

    public VentaService(IVentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    public List<Venta> traerVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public Page<Venta> traerVentasPaginadas(Pageable pageable) {
        return ventaRepository.findAll(pageable);
    }

    @Override
    public Venta buscarVenta(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public Venta buscarPorId(Long id) {
        return buscarVenta(id);
    }

    @Override
    public List<Venta> filtrarPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
            return ventaRepository.findByFechaBetween(inicio, fin);
        } else if (fechaInicio != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            return ventaRepository.findByFechaAfter(inicio);
        } else if (fechaFin != null) {
            LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
            return ventaRepository.findByFechaBefore(fin);
        }
        return traerVentas();
    }

    @Override
    public boolean anularVenta(Long id) {
        Venta venta = buscarVenta(id);
        if (venta != null) {
            venta.setEstado("ANULADA");
            ventaRepository.save(venta);
            return true;
        }
        return false;
    }
    @Override
    public Venta crearVenta(Venta venta) {
        return ventaRepository.save(venta);
    }
}