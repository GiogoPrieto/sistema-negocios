package com.sistema.SistemaNegocios.util; // o .service según tu carpeta

import com.sistema.SistemaNegocios.model.Producto;
import com.sistema.SistemaNegocios.repository.IProductoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class ImportadorService {

    private final IProductoRepository productoRepository;

    public ImportadorService(IProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public void importarProductosDesdeExcel(MultipartFile archivo) throws Exception {
        try (InputStream is = archivo.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() <= 1) {
                throw new IllegalArgumentException("El archivo Excel está vacío o no contiene datos.");
            }

            // Detectar índices de columnas leyendo el encabezado (Fila 0)
            Row headerRow = sheet.getRow(0);
            int colNombre = -1, colMarca = -1, colCategoria = -1, colPrecio = -1, colStock = -1, colDesc = -1;

            for (Cell cell : headerRow) {
                String header = obtenerTextoCelda(cell).toLowerCase();
                if (header.contains("nombre")) colNombre = cell.getColumnIndex();
                else if (header.contains("marca")) colMarca = cell.getColumnIndex();
                else if (header.contains("categor")) colCategoria = cell.getColumnIndex();
                else if (header.contains("precio")) colPrecio = cell.getColumnIndex();
                else if (header.contains("stock")) colStock = cell.getColumnIndex();
                else if (header.contains("descrip")) colDesc = cell.getColumnIndex();
            }

            // Mapeo por defecto si no coinciden los títulos de los encabezados
            if (colNombre == -1) colNombre = 1; // Columna B por defecto (si la A es ID)
            if (colMarca == -1) colMarca = 2;
            if (colCategoria == -1) colCategoria = 3;
            if (colPrecio == -1) colPrecio = 4;
            if (colStock == -1) colStock = 5;
            if (colDesc == -1) colDesc = 6;

            // Recorrer las filas de datos desde la 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nombre = obtenerTextoCelda(row.getCell(colNombre));

                // Si la columna por defecto falló, reintenta buscar en la primera columna (índice 0)
                if (nombre.isBlank() && colNombre != 0) {
                    nombre = obtenerTextoCelda(row.getCell(0));
                }

                if (nombre.isBlank()) continue; // Omitir filas vacías

                String marca = colMarca < row.getLastCellNum() ? obtenerTextoCelda(row.getCell(colMarca)) : "";
                String categoria = colCategoria < row.getLastCellNum() ? obtenerTextoCelda(row.getCell(colCategoria)) : "";
                double precio = colPrecio < row.getLastCellNum() ? obtenerNumeroCelda(row.getCell(colPrecio)) : 0.0;
                int stock = colStock < row.getLastCellNum() ? (int) obtenerNumeroCelda(row.getCell(colStock)) : 0;
                String descripcion = colDesc < row.getLastCellNum() ? obtenerTextoCelda(row.getCell(colDesc)) : "";

                Producto p = new Producto();
                p.setNombre(nombre);
                p.setMarca(marca);
                p.setCategoria(categoria);
                p.setPrecio(precio);
                p.setStock(stock);
                p.setDescripcion(descripcion);
                p.setActivo(true); // Asegurar que el producto nazca habilitado en el sistema

                productoRepository.save(p);
            }
        }
    }

    private String obtenerTextoCelda(Cell cell) {
        if (cell == null) return "";
        CellType type = cell.getCellType();

        if (type == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (type == CellType.NUMERIC) {
            double numeric = cell.getNumericCellValue();
            if (numeric == (long) numeric) {
                return String.valueOf((long) numeric);
            }
            return String.valueOf(numeric);
        } else if (type == CellType.FORMULA) {
            try {
                return cell.getStringCellValue().trim();
            } catch (Exception e) {
                return String.valueOf(cell.getNumericCellValue());
            }
        }
        return "";
    }

    private double obtenerNumeroCelda(Cell cell) {
        if (cell == null) return 0.0;
        CellType type = cell.getCellType();

        if (type == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (type == CellType.STRING) {
            try {
                // Limpia formatos como "$ 90.000" -> "90000"
                String val = cell.getStringCellValue()
                        .replaceAll("[^0-9,.]", "")
                        .replace(".", "")
                        .replace(",", ".");
                return val.isEmpty() ? 0.0 : Double.parseDouble(val);
            } catch (Exception e) {
                return 0.0;
            }
        } else if (type == CellType.FORMULA) {
            try {
                return cell.getNumericCellValue();
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}