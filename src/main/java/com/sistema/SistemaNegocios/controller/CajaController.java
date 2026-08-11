package com.sistema.SistemaNegocios.controller;

import com.sistema.SistemaNegocios.model.Caja;
import com.sistema.SistemaNegocios.repository.ICajaRepository;
import com.sistema.SistemaNegocios.repository.IVentaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/caja")
public class CajaController {

    private final ICajaRepository cajaRepository;
    private final IVentaRepository iVentaRepository;

    public CajaController(ICajaRepository cajaRepository, IVentaRepository iVentaRepository) {
        this.cajaRepository = cajaRepository;
        this.iVentaRepository = iVentaRepository;
    }

    @GetMapping
    public String index(Model model) {
        Optional<Caja> cajaAbierta = cajaRepository.findByEstado("ABIERTA");
        List<Caja> historial = cajaRepository.findByEstadoOrderByFechaAperturaDesc("CERRADA");

        if (cajaAbierta.isPresent()) {
            Caja caja = cajaAbierta.get();

            // Totales por método de pago
            Double efectivo = obtenerMonto(iVentaRepository.obtenerTotalPorMetodoDesde(caja.getFechaApertura(), "EFECTIVO"));
            Double debito = obtenerMonto(iVentaRepository.obtenerTotalPorMetodoDesde(caja.getFechaApertura(), "TARJETA_DEBITO"));
            Double credito = obtenerMonto(iVentaRepository.obtenerTotalPorMetodoDesde(caja.getFechaApertura(), "TARJETA_CREDITO"));
            Double transferencia = obtenerMonto(iVentaRepository.obtenerTotalPorMetodoDesde(caja.getFechaApertura(), "TRANSFERENCIA"));
            Double qr = obtenerMonto(iVentaRepository.obtenerTotalPorMetodoDesde(caja.getFechaApertura(), "QR"));

            Double totalGeneral = obtenerMonto(iVentaRepository.obtenerTotalVentasDesde(caja.getFechaApertura()));

            caja.setVentasEfectivo(efectivo);
            caja.setVentasDebito(debito);
            caja.setVentasCredito(credito);
            caja.setVentasTransferencia(transferencia);
            caja.setVentasQr(qr);
            caja.setTotalVentasGeneral(totalGeneral);

            // El esperado en caja física sigue siendo Monto Inicial + Efectivo
            caja.setMontoEsperado(caja.getMontoInicial() + efectivo);

            model.addAttribute("cajaAbierta", caja);
        }

        model.addAttribute("historial", historial);
        return "caja/gestion";
    }

    @PostMapping("/abrir")
    public String abrirCaja(@RequestParam("montoInicial") Double montoInicial, RedirectAttributes redirect) {
        if (cajaRepository.findByEstado("ABIERTA").isPresent()) {
            redirect.addFlashAttribute("error", "Ya existe una caja abierta actualmente.");
            return "redirect:/caja";
        }

        Caja caja = new Caja();
        caja.setFechaApertura(LocalDateTime.now());
        caja.setMontoInicial(montoInicial != null ? montoInicial : 0.0);
        caja.setEstado("ABIERTA");

        cajaRepository.save(caja);
        redirect.addFlashAttribute("exito", "Caja abierta correctamente con $" + montoInicial);
        return "redirect:/caja";
    }

    @PostMapping("/cerrar")
    public String cerrarCaja(@RequestParam("idCaja") Long idCaja,
                             @RequestParam("montoReal") Double montoReal,
                             @RequestParam(value = "observaciones", required = false) String observaciones,
                             RedirectAttributes redirect) {

        Caja caja = cajaRepository.findById(idCaja).orElse(null);

        if (caja != null && "ABIERTA".equals(caja.getEstado())) {
            caja.setFechaCierre(LocalDateTime.now());
            caja.setMontoReal(montoReal);

            Double efectivo = obtenerMonto(iVentaRepository.obtenerTotalPorMetodoDesde(caja.getFechaApertura(), "EFECTIVO"));
            Double debito = obtenerMonto(iVentaRepository.obtenerTotalPorMetodoDesde(caja.getFechaApertura(), "TARJETA_DEBITO"));
            Double credito = obtenerMonto(iVentaRepository.obtenerTotalPorMetodoDesde(caja.getFechaApertura(), "TARJETA_CREDITO"));
            Double transferencia = obtenerMonto(iVentaRepository.obtenerTotalPorMetodoDesde(caja.getFechaApertura(), "TRANSFERENCIA"));
            Double qr = obtenerMonto(iVentaRepository.obtenerTotalPorMetodoDesde(caja.getFechaApertura(), "QR"));
            Double totalGeneral = obtenerMonto(iVentaRepository.obtenerTotalVentasDesde(caja.getFechaApertura()));

            caja.setVentasEfectivo(efectivo);
            caja.setVentasDebito(debito);
            caja.setVentasCredito(credito);
            caja.setVentasTransferencia(transferencia);
            caja.setVentasQr(qr);
            caja.setTotalVentasGeneral(totalGeneral);

            Double totalEsperadoFisico = caja.getMontoInicial() + efectivo;
            caja.setMontoEsperado(totalEsperadoFisico);
            caja.setDiferencia(montoReal - totalEsperadoFisico);

            caja.setObservaciones(observaciones);
            caja.setEstado("CERRADA");

            cajaRepository.save(caja);
            redirect.addFlashAttribute("exito", "Caja cerrada y arqueo completado exitosamente.");
        } else {
            redirect.addFlashAttribute("error", "Error al procesar el cierre de caja.");
        }

        return "redirect:/caja";
    }

    private Double obtenerMonto(Double valor) {
        return valor != null ? valor : 0.0;
    }
}