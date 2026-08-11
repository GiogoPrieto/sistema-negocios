package com.sistema.SistemaNegocios.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.sistema.SistemaNegocios.model.DetalleVenta;
import com.sistema.SistemaNegocios.model.Venta;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class ComprobantePdfService {

    // 1. Método para generar Ticket POS (80mm)
    public ByteArrayInputStream generarTicketPdf(Venta venta) {
        Document document = new Document(new Rectangle(226, 700), 10, 10, 15, 15);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font fontSubHeader = FontFactory.getFont(FontFactory.HELVETICA, 8);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8);
            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 7);
            Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            Paragraph empresa = new Paragraph("SISTEMA POS", fontHeader);
            empresa.setAlignment(Element.ALIGN_CENTER);
            document.add(empresa);

            Paragraph datosComercio = new Paragraph(
                    "NOMBRE DEL NEGOCIO\n" +
                            "RUC / CIF: 12345678-9\n" +
                            "Av. Principal 123 - Ciudad\n" +
                            "TEL: +595 981 000 000\n\n", fontSubHeader);
            datosComercio.setAlignment(Element.ALIGN_CENTER);
            document.add(datosComercio);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            Paragraph infoTicket = new Paragraph(
                    "Factura Simplificada\n" +
                            "N°: 001-001-" + String.format("%07d", venta.getId()) + "\n" +
                            "Fecha: " + venta.getFecha().format(formatter) + "\n" +
                            "Forma de pago: Efectivo\n", fontNormal);
            infoTicket.setAlignment(Element.ALIGN_LEFT);
            document.add(infoTicket);

            document.add(crearLineaDivisoria());

            PdfPTable tableHeader = new PdfPTable(2);
            tableHeader.setWidthPercentage(100);
            tableHeader.setWidths(new float[]{3f, 1f});

            PdfPCell c1 = new PdfPCell(new Phrase("PRODUCTO", fontBold));
            c1.setBorder(Rectangle.NO_BORDER);
            PdfPCell c2 = new PdfPCell(new Phrase("SUBTOTAL", fontBold));
            c2.setBorder(Rectangle.NO_BORDER);
            c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

            tableHeader.addCell(c1);
            tableHeader.addCell(c2);
            document.add(tableHeader);

            document.add(crearLineaDivisoria());

            for (DetalleVenta det : venta.getDetalles()) {
                Paragraph prodNombre = new Paragraph(det.getProducto().getNombre(), fontBold);
                document.add(prodNombre);

                PdfPTable itemRow = new PdfPTable(2);
                itemRow.setWidthPercentage(100);
                itemRow.setWidths(new float[]{3f, 1f});

                double subtotal = det.getCantidad() * det.getPrecioUnitario();

                PdfPCell cellDetalle = new PdfPCell(new Phrase(
                        det.getCantidad() + " x $" + String.format("%,.0f", det.getPrecioUnitario()), fontSmall));
                cellDetalle.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellSubtotal = new PdfPCell(new Phrase(
                        "$" + String.format("%,.0f", subtotal), fontNormal));
                cellSubtotal.setBorder(Rectangle.NO_BORDER);
                cellSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);

                itemRow.addCell(cellDetalle);
                itemRow.addCell(cellSubtotal);
                document.add(itemRow);
            }

            document.add(crearLineaDivisoria());

            Paragraph pTotal = new Paragraph("TOTAL $" + String.format("%,.0f", venta.getTotal()), fontTotal);
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            document.add(pTotal);

            document.add(new Paragraph("\n"));

            PdfContentByte cb = writer.getDirectContent();
            Barcode128 barcode128 = new Barcode128();
            barcode128.setCode(String.format("%012d", venta.getId()));
            barcode128.setCodeType(Barcode128.CODE128);
            Image code128Image = barcode128.createImageWithBarcode(cb, null, null);
            code128Image.setAlignment(Element.ALIGN_CENTER);
            code128Image.scalePercent(100);
            document.add(code128Image);

            Paragraph atencion = new Paragraph("\nFue atendido por: Cajero Principal", fontSmall);
            atencion.setAlignment(Element.ALIGN_CENTER);
            document.add(atencion);

            Paragraph garantia = new Paragraph(
                    "\nEste ticket es imprescindible para cualquier cambio o devolución. " +
                            "Dispone de 30 días para realizar cualquier cambio o devolución " +
                            "siempre y cuando el producto esté sin usar y con empaque original.", fontSmall);
            garantia.setAlignment(Element.ALIGN_CENTER);
            document.add(garantia);

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // 2. Método para generar Factura Formato A4 (Este es el que te solicitaba el controlador)
    public ByteArrayInputStream generarFacturaPdf(Venta venta) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph titulo = new Paragraph("COMPROBANTE DE VENTA", fontHeader);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph(" ", fontNormal));

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.addCell(new Phrase("N° de Venta: #" + venta.getId(), fontBold));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            infoTable.addCell(new Phrase("Fecha: " + venta.getFecha().format(formatter), fontBold));
            infoTable.addCell(new Phrase("Estado: " + (venta.getEstado() != null ? venta.getEstado() : "COMPLETADA"), fontNormal));
            infoTable.addCell(new Phrase("Condición de Pago: Contado", fontNormal));
            document.add(infoTable);

            document.add(new Paragraph("\n", fontNormal));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 4f, 2f, 2f});

            table.addCell(new PdfPCell(new Phrase("Cant.", fontBold)));
            table.addCell(new PdfPCell(new Phrase("Descripción", fontBold)));
            table.addCell(new PdfPCell(new Phrase("Precio Unit.", fontBold)));
            table.addCell(new PdfPCell(new Phrase("Total", fontBold)));

            for (DetalleVenta det : venta.getDetalles()) {
                table.addCell(new Phrase(String.valueOf(det.getCantidad()), fontNormal));
                table.addCell(new Phrase(det.getProducto().getNombre(), fontNormal));
                table.addCell(new Phrase("$ " + String.format("%,.0f", det.getPrecioUnitario()), fontNormal));
                double sub = det.getCantidad() * det.getPrecioUnitario();
                table.addCell(new Phrase("$ " + String.format("%,.0f", sub), fontNormal));
            }
            document.add(table);

            document.add(new Paragraph("\n", fontNormal));

            Paragraph totalFinal = new Paragraph("TOTAL A PAGAR: $ " + String.format("%,.0f", venta.getTotal()), fontHeader);
            totalFinal.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalFinal);

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private Paragraph crearLineaDivisoria() {
        Paragraph p = new Paragraph("------------------------------------------------------------------",
                FontFactory.getFont(FontFactory.HELVETICA, 7));
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }
}