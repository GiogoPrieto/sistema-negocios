package com.sistema.SistemaNegocios.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ReporteUtils {

    /**
     * Convierte cualquier número a formato de miles con punto (ej: 100000 -> "100.000")
     */
    public static String formatearMonto(Object valor) {
        if (valor == null) return "0";

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        DecimalFormat df = new DecimalFormat("#,##0", simbolos);

        try {
            return df.format(valor);
        } catch (IllegalArgumentException e) {
            return "0";
        }
    }
}