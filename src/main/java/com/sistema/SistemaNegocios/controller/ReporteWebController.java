package com.sistema.SistemaNegocios.controller;

import com.sistema.SistemaNegocios.model.Producto;
import com.sistema.SistemaNegocios.service.IProductoService;
import com.sistema.SistemaNegocios.service.IVentaService;
import com.sistema.SistemaNegocios.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reportes")
public class ReporteWebController {

    private final IProductoService productoService;
    private final ReporteService reporteService;
    private final IVentaService iVentaService;

    public ReporteWebController(IProductoService productoService, ReporteService reporteService, IVentaService iVentaService) {
        this.productoService = productoService;
        this.reporteService = reporteService;
        this.iVentaService = iVentaService;
    }

    // EXPORTACIÓN EXCEL PRODUCTOS
    @GetMapping("/productos/excel")
    public ResponseEntity<byte[]> descargarProductosExcel() {
        List<Producto> productos = productoService.traerProductos();
        byte[] excelBytes = reporteService.generarReporteProductosExcel(productos);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_productos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    // MAPEO VENTAS PDF
    @GetMapping("/ventas/pdf")
    public ResponseEntity<byte[]> descargarVentasPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        byte[] pdfBytes = reporteService.generarReporteVentasPdf(fechaInicio, fechaFin);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=reporte_ventas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // MAPEO VENTAS EXCEL
    @GetMapping("/ventas/excel")
    public ResponseEntity<byte[]> descargarVentasExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        byte[] excelBytes = reporteService.generarReporteVentasExcel(fechaInicio, fechaFin);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_ventas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    // GENERAR UNICAMENTE FACTURA EN PDF
    @GetMapping("/factura/{id}")
    public ResponseEntity<byte[]> generarFacturaPdf(@PathVariable("id") Long id) {
        byte[] pdfBytes = reporteService.generarFacturaPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=factura_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}