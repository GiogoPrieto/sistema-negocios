package com.sistema.SistemaNegocios.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_barras")
    private String codigoBarras;

    private String nombre;
    private String marca;
    private String descripcion;
    private String categoria;

    private Double precio;

    @Column(name = "precio_costo")
    private Double precioCosto;

    private Integer iva = 10; // 10, 5 o 0

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    private Integer stock;
    private Boolean activo;

    public Producto() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Double getPrecioCosto() { return precioCosto; }
    public void setPrecioCosto(Double precioCosto) { this.precioCosto = precioCosto; }

    public Integer getIva() { return iva; }
    public void setIva(Integer iva) { this.iva = iva; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}