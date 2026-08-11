package com.sistema.SistemaNegocios.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "cajas")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaApertura;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaCierre;

    private Double montoInicial = 0.0;
    private Double ventasEfectivo = 0.0;
    private Double ventasDebito = 0.0;
    private Double ventasCredito = 0.0;
    private Double ventasTransferencia = 0.0;
    private Double ventasQr = 0.0;
    private Double totalVentasGeneral = 0.0;

    private Double montoEsperado = 0.0;
    private Double montoReal = 0.0;
    private Double diferencia = 0.0;

    private String estado; // "ABIERTA" o "CERRADA"
    private String observaciones;

    public Caja() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public Double getMontoInicial() { return montoInicial; }
    public void setMontoInicial(Double montoInicial) { this.montoInicial = montoInicial; }

    public Double getVentasEfectivo() { return ventasEfectivo; }
    public void setVentasEfectivo(Double ventasEfectivo) { this.ventasEfectivo = ventasEfectivo; }

    public Double getVentasDebito() { return ventasDebito; }
    public void setVentasDebito(Double ventasDebito) { this.ventasDebito = ventasDebito; }

    public Double getVentasCredito() { return ventasCredito; }
    public void setVentasCredito(Double ventasCredito) { this.ventasCredito = ventasCredito; }

    public Double getVentasTransferencia() { return ventasTransferencia; }
    public void setVentasTransferencia(Double ventasTransferencia) { this.ventasTransferencia = ventasTransferencia; }

    public Double getVentasQr() { return ventasQr; }
    public void setVentasQr(Double ventasQr) { this.ventasQr = ventasQr; }

    public Double getTotalVentasGeneral() { return totalVentasGeneral; }
    public void setTotalVentasGeneral(Double totalVentasGeneral) { this.totalVentasGeneral = totalVentasGeneral; }

    public Double getMontoEsperado() { return montoEsperado; }
    public void setMontoEsperado(Double montoEsperado) { this.montoEsperado = montoEsperado; }

    public Double getMontoReal() { return montoReal; }
    public void setMontoReal(Double montoReal) { this.montoReal = montoReal; }

    public Double getDiferencia() { return diferencia; }
    public void setDiferencia(Double diferencia) { this.diferencia = diferencia; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}