package com.sistema.SistemaNegocios.service;

import java.time.LocalDate;

public interface IReporteService {
    byte[] generarReporteVentasPdf(LocalDate fechaInicio, LocalDate fechaFin);
}