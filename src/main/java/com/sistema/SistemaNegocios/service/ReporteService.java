package com.sistema.SistemaNegocios.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sistema.SistemaNegocios.model.DetalleVenta;
import com.sistema.SistemaNegocios.model.Producto;
import com.sistema.SistemaNegocios.model.Venta;
import com.sistema.SistemaNegocios.repository.IProductoRepository;
import com.sistema.SistemaNegocios.repository.IVentaRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteService {

    private final IVentaRepository ventaRepository;
    private final IProductoRepository productoRepository;

    public ReporteService(IVentaRepository ventaRepository, IProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    // 1. GENERAR PDF DE FACTURA INDIVIDUAL DE VENTA
    public byte[] generarFacturaPdf(Long idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + idVenta));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // FUENTES
            Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 23, 42));
            Font fontFacturaTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(37, 99, 235));
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font fontRegular = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(21, 128, 61));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // ENCABEZADO SUPERIOR
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60, 40});

            // Columna Izquierda: Datos del Negocio
            PdfPCell cellEmpresa = new PdfPCell();
            cellEmpresa.setBorder(PdfPCell.NO_BORDER);
            cellEmpresa.addElement(new Paragraph("SISTEMA DE VENTAS", fontEmpresa));
            cellEmpresa.addElement(new Paragraph("RUC: 80000000-1", fontSubtitulo));
            cellEmpresa.addElement(new Paragraph("Dirección: Av. Principal N° 123", fontRegular));
            cellEmpresa.addElement(new Paragraph("Teléfono: +595 981 000 000", fontRegular));
            headerTable.addCell(cellEmpresa);

            // Columna Derecha: Datos de la Factura
            PdfPCell cellFactura = new PdfPCell();
            cellFactura.setBorder(PdfPCell.NO_BORDER);
            cellFactura.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellFactura.addElement(new Paragraph("FACTURA DE VENTA", fontFacturaTitulo));
            cellFactura.addElement(new Paragraph("N° Venta: #" + venta.getId(), fontSubtitulo));
            cellFactura.addElement(new Paragraph("Fecha: " + (venta.getFecha() != null ? venta.getFecha().format(formatter) : "-"), fontRegular));
            cellFactura.addElement(new Paragraph("Método de Pago: " + (venta.getMetodoPago() != null ? venta.getMetodoPago() : "EFECTIVO"), fontRegular));
            headerTable.addCell(cellFactura);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // SECCIÓN CLIENTE
            PdfPTable clienteTable = new PdfPTable(1);
            clienteTable.setWidthPercentage(100);

            PdfPCell cellCliente = new PdfPCell();
            cellCliente.setBackgroundColor(new Color(248, 250, 252));
            cellCliente.setPadding(10);
            cellCliente.setBorderColor(new Color(226, 232, 240));

            String nombreCliente = "Sin Nombre / Consumidor Final";
            String rucCliente = "X";

            if (venta.getCliente() != null) {
                if (venta.getCliente().getNombre() != null && !venta.getCliente().getNombre().isEmpty()) {
                    nombreCliente = venta.getCliente().getNombre();
                }
                if (venta.getCliente().getRuc() != null && !venta.getCliente().getRuc().isEmpty()) {
                    rucCliente = venta.getCliente().getRuc();
                }
            }

            cellCliente.addElement(new Paragraph("DATOS DEL CLIENTE", fontSubtitulo));
            cellCliente.addElement(new Paragraph("Cliente: " + nombreCliente, fontRegular));
            cellCliente.addElement(new Paragraph("RUC / Cédula: " + rucCliente, fontRegular));

            clienteTable.addCell(cellCliente);
            document.add(clienteTable);
            document.add(new Paragraph(" "));

            // TABLA DE PRODUCTOS
            PdfPTable tableDetalles = new PdfPTable(4);
            tableDetalles.setWidthPercentage(100);
            tableDetalles.setWidths(new float[]{45, 15, 20, 20});

            String[] headers = {"Producto", "Cantidad", "Precio Unit.", "Subtotal"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontHeaderTabla));
                cell.setBackgroundColor(new Color(37, 99, 235));
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tableDetalles.addCell(cell);
            }

            if (venta.getDetalles() != null) {
                for (DetalleVenta d : venta.getDetalles()) {
                    String pNombre = (d.getProducto() != null && d.getProducto().getNombre() != null)
                            ? d.getProducto().getNombre() : "Producto";
                    double pPrecio = d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0.0;
                    int pCantidad = d.getCantidad();
                    double pSubtotal = pPrecio * pCantidad;

                    PdfPCell cProd = new PdfPCell(new Phrase(pNombre, fontRegular));
                    PdfPCell cCant = new PdfPCell(new Phrase(String.valueOf(pCantidad), fontRegular));
                    PdfPCell cPrec = new PdfPCell(new Phrase("$ " + String.format("%.0f", pPrecio), fontRegular));
                    PdfPCell cSubt = new PdfPCell(new Phrase("$ " + String.format("%.0f", pSubtotal), fontRegular));

                    cCant.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cPrec.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cSubt.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    cProd.setPadding(6); cCant.setPadding(6); cPrec.setPadding(6); cSubt.setPadding(6);

                    tableDetalles.addCell(cProd);
                    tableDetalles.addCell(cCant);
                    tableDetalles.addCell(cPrec);
                    tableDetalles.addCell(cSubt);
                }
            }

            document.add(tableDetalles);
            document.add(new Paragraph(" "));

            // TOTAL DE LA FACTURA
            double total = venta.getTotal() != null ? venta.getTotal() : 0.0;
            Paragraph pTotal = new Paragraph("TOTAL FACTURA: $ " + String.format("%.0f", total), fontTotal);
            pTotal.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(pTotal);

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar factura PDF: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // 2. GENERAR PDF DE VENTAS (Con filtro de fechas y Ganancia Total)
    public byte[] generarReporteVentasPdf(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Venta> ventas;

        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime desde = fechaInicio.atStartOfDay();
            LocalDateTime hasta = fechaFin.atTime(LocalTime.MAX);
            ventas = ventaRepository.findByFechaBetween(desde, hasta);
        } else {
            ventas = ventaRepository.findAll();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Paragraph titulo = new Paragraph("Reporte de Historial de Ventas", fontTitulo);
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            titulo.setSpacingAfter(15);
            document.add(titulo);

            // Variables para métricas
            double totalIngresado = 0.0;
            double costoTotal = 0.0;

            for (Venta v : ventas) {
                if (v.getEstado() == null || "COMPLETADA".equalsIgnoreCase(v.getEstado())) {
                    if (v.getTotal() != null) {
                        totalIngresado += v.getTotal();
                    }
                    if (v.getDetalles() != null) {
                        for (DetalleVenta d : v.getDetalles()) {
                            if (d.getProducto() != null) {
                                double costoUnitario = (d.getProducto() != null && d.getProducto().getPrecioCosto() != null)
                                        ? d.getProducto().getPrecioCosto()
                                        : 0.0;
                                costoTotal += (costoUnitario * d.getCantidad());
                            }
                        }
                    }
                }
            }

            double gananciaTotal = totalIngresado - costoTotal;

            // Cuadro de Resumen Financiero en el PDF
            PdfPTable resumenTable = new PdfPTable(3);
            resumenTable.setWidthPercentage(100);
            resumenTable.setSpacingAfter(15);

            Font fontResumenHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font fontResumenVal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(21, 128, 61));

            PdfPCell cell1 = new PdfPCell(new Paragraph("TOTAL INGRESADO\n$ " + String.format("%.0f", totalIngresado), fontResumenVal));
            PdfPCell cell2 = new PdfPCell(new Paragraph("COSTO TOTAL\n$ " + String.format("%.0f", costoTotal), fontResumenHeader));
            PdfPCell cell3 = new PdfPCell(new Paragraph("GANANCIA NETO\n$ " + String.format("%.0f", gananciaTotal), fontResumenVal));

            cell1.setBackgroundColor(new Color(240, 253, 244));
            cell2.setBackgroundColor(new Color(248, 250, 252));
            cell3.setBackgroundColor(new Color(220, 252, 231));

            cell1.setPadding(8); cell2.setPadding(8); cell3.setPadding(8);

            resumenTable.addCell(cell1);
            resumenTable.addCell(cell2);
            resumenTable.addCell(cell3);

            document.add(resumenTable);

            // Tabla Principal de Ventas
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3.5f, 5.0f, 2.5f, 2.5f});

            String[] headers = {"N° Venta", "Fecha", "Detalles", "Total", "Estado"};
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                cell.setBackgroundColor(new Color(37, 99, 235));
                cell.setPadding(6);
                table.addCell(cell);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            Font fontData = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

            for (Venta v : ventas) {
                table.addCell(new Phrase("#" + v.getId(), fontData));
                table.addCell(new Phrase(v.getFecha() != null ? v.getFecha().format(formatter) : "", fontData));

                StringBuilder sb = new StringBuilder();
                if (v.getDetalles() != null) {
                    for (DetalleVenta d : v.getDetalles()) {
                        sb.append("• ").append(d.getProducto().getNombre())
                                .append(" x").append(d.getCantidad()).append("\n");
                    }
                }
                table.addCell(new Phrase(sb.toString().trim(), fontData));
                table.addCell(new Phrase("$ " + (v.getTotal() != null ? String.format("%.0f", v.getTotal()) : "0"), fontData));
                table.addCell(new Phrase(v.getEstado() != null ? v.getEstado() : "COMPLETADA", fontData));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de ventas: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // 3. GENERAR EXCEL DE VENTAS (Con filtro de fechas)
    public byte[] generarReporteVentasExcel(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Venta> ventas;

        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime desde = fechaInicio.atStartOfDay();
            LocalDateTime hasta = fechaFin.atTime(LocalTime.MAX);
            ventas = ventaRepository.findByFechaBetween(desde, hasta);
        } else {
            ventas = ventaRepository.findAll();
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Historial de Ventas");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] columnas = {"ID Venta", "Fecha", "Total", "Estado"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowNum = 1;

            for (Venta v : ventas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(v.getId() != null ? v.getId() : 0);
                row.createCell(1).setCellValue(v.getFecha() != null ? v.getFecha().format(formatter) : "");
                row.createCell(2).setCellValue(v.getTotal() != null ? v.getTotal() : 0.0);
                row.createCell(3).setCellValue(v.getEstado() != null ? v.getEstado() : "COMPLETADA");
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel de ventas: " + e.getMessage(), e);
        }
    }

    // 4. GENERAR EXCEL DE PRODUCTOS (Inventario)
    public byte[] generarReporteProductosExcel(List<Producto> productos) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Productos");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] columnas = {"ID", "Nombre", "Categoría", "Precio", "Stock"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Producto prod : productos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(prod.getId() != null ? prod.getId() : 0);
                row.createCell(1).setCellValue(prod.getNombre() != null ? prod.getNombre() : "");
                row.createCell(2).setCellValue(prod.getCategoria() != null ? prod.getCategoria() : "");
                row.createCell(3).setCellValue(prod.getPrecio() != null ? prod.getPrecio() : 0.0);
                row.createCell(4).setCellValue(prod.getStock() != null ? prod.getStock() : 0);
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel de productos: " + e.getMessage(), e);
        }
    }

}