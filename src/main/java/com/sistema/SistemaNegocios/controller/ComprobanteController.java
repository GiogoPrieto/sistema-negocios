package com.sistema.SistemaNegocios.controller;

import com.sistema.SistemaNegocios.model.Venta;
import com.sistema.SistemaNegocios.service.ComprobantePdfService;
import com.sistema.SistemaNegocios.service.IVentaService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayInputStream;

@Controller
@RequestMapping("/comprobantes")
public class ComprobanteController {

    private final IVentaService ventaService;
    private final ComprobantePdfService comprobantePdfService;

    public ComprobanteController(IVentaService ventaService, ComprobantePdfService comprobantePdfService) {
        this.ventaService = ventaService;
        this.comprobantePdfService = comprobantePdfService;
    }

    // Endpoint para Ticket
    @GetMapping("/ticket/{id}")
    public ResponseEntity<InputStreamResource> verTicket(@PathVariable Long id) {
        Venta venta = ventaService.buscarPorId(id);
        if (venta == null) return ResponseEntity.notFound().build();

        ByteArrayInputStream bis = comprobantePdfService.generarTicketPdf(venta);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=ticket_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    // Endpoint para Factura A4
    @GetMapping("/factura/{id}")
    public ResponseEntity<InputStreamResource> verFactura(@PathVariable Long id) {
        Venta venta = ventaService.buscarPorId(id);
        if (venta == null) return ResponseEntity.notFound().build();

        ByteArrayInputStream bis = comprobantePdfService.generarFacturaPdf(venta);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=factura_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}