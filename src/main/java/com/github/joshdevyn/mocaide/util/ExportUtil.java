package com.github.joshdevyn.mocaide.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for exporting table data to various formats
 * Supports CSV, Excel, JSON, and XML export formats
 */
public class ExportUtil {
    
    public enum ExportFormat {
        CSV("CSV Files", "csv"),
        EXCEL("Excel Files", "xlsx"),
        JSON("JSON Files", "json"),
        XML("XML Files", "xml");
        
        private final String description;
        private final String extension;
        
        ExportFormat(String description, String extension) {
            this.description = description;
            this.extension = extension;
        }
        
        public String getDescription() { return description; }
        public String getExtension() { return extension; }
        
        @Override
        public String toString() {
            return description + " (*." + extension + ")";
        }
    }
    
    /**
     * Show export dialog and export table data
     */
    public static void showExportDialog(JComponent parent, TableModel tableModel, String suggestedFileName) {
        JFileChooser fileChooser = new JFileChooser();
        
        // Add file filters for different formats
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            @Override
            public String getDescription() {
                return ExportFormat.CSV.toString();
            }
        });
        
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xlsx");
            }
            @Override
            public String getDescription() {
                return ExportFormat.EXCEL.toString();
            }
        });
        
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
            }
            @Override
            public String getDescription() {
                return ExportFormat.JSON.toString();
            }
        });
        
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
            }
            @Override
            public String getDescription() {
                return ExportFormat.XML.toString();
            }
        });
        
        // Set default file name
        fileChooser.setSelectedFile(new File(suggestedFileName + ".csv"));
        
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName().toLowerCase();
            
            try {
                if (fileName.endsWith(".csv")) {
                    exportToCSV(tableModel, selectedFile);
                } else if (fileName.endsWith(".xlsx")) {
                    exportToExcel(tableModel, selectedFile);
                } else if (fileName.endsWith(".json")) {
                    exportToJSON(tableModel, selectedFile);
                } else if (fileName.endsWith(".xml")) {
                    exportToXML(tableModel, selectedFile);
                } else {
                    // Default to CSV if no extension or unknown extension
                    File csvFile = new File(selectedFile.getPath() + ".csv");
                    exportToCSV(tableModel, csvFile);
                }
                
                JOptionPane.showMessageDialog(parent, 
                    "Export completed successfully to:\n" + selectedFile.getAbsolutePath(), 
                    "Export Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent, 
                    "Error during export: " + e.getMessage(), 
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Export selected table data
     */
    public static void showExportDialog(JComponent parent, JTable table, String suggestedFileName) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(parent, 
                "No rows selected. Please select rows to export.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        setupFileFilters(fileChooser);
        fileChooser.setSelectedFile(new File(suggestedFileName + "_selected.csv"));
        
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName().toLowerCase();
            
            try {
                if (fileName.endsWith(".csv")) {
                    exportSelectedToCSV(table, selectedFile);
                } else if (fileName.endsWith(".xlsx")) {
                    exportSelectedToExcel(table, selectedFile);
                } else if (fileName.endsWith(".json")) {
                    exportSelectedToJSON(table, selectedFile);
                } else if (fileName.endsWith(".xml")) {
                    exportSelectedToXML(table, selectedFile);
                } else {
                    File csvFile = new File(selectedFile.getPath() + ".csv");
                    exportSelectedToCSV(table, csvFile);
                }
                
                JOptionPane.showMessageDialog(parent, 
                    "Export completed successfully to:\n" + selectedFile.getAbsolutePath(), 
                    "Export Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent, 
                    "Error during export: " + e.getMessage(), 
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private static void setupFileFilters(JFileChooser fileChooser) {
        for (ExportFormat format : ExportFormat.values()) {
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith("." + format.getExtension());
                }
                @Override
                public String getDescription() {
                    return format.toString();
                }
            });
        }
    }
    
    // CSV Export Methods
    public static void exportToCSV(TableModel tableModel, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write headers
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                if (j > 0) writer.write(",");
                writer.write("\"" + escapeCSV(tableModel.getColumnName(j)) + "\"");
            }
            writer.write("\n");
            
            // Write data
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    if (j > 0) writer.write(",");
                    Object value = tableModel.getValueAt(i, j);
                    String valueStr = value != null ? value.toString() : "";
                    writer.write("\"" + escapeCSV(valueStr) + "\"");
                }
                writer.write("\n");
            }
        }
    }
    
    public static void exportSelectedToCSV(JTable table, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write headers
            for (int j = 0; j < table.getColumnCount(); j++) {
                if (j > 0) writer.write(",");
                writer.write("\"" + escapeCSV(table.getColumnName(j)) + "\"");
            }
            writer.write("\n");
            
            // Write selected rows
            int[] selectedRows = table.getSelectedRows();
            for (int rowIndex : selectedRows) {
                for (int j = 0; j < table.getColumnCount(); j++) {
                    if (j > 0) writer.write(",");
                    Object value = table.getValueAt(rowIndex, j);
                    String valueStr = value != null ? value.toString() : "";
                    writer.write("\"" + escapeCSV(valueStr) + "\"");
                }
                writer.write("\n");
            }
        }
    }
    
    // Excel Export Methods
    public static void exportToExcel(TableModel tableModel, File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Cell cell = headerRow.createCell(j);
                cell.setCellValue(tableModel.getColumnName(j));
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    Cell cell = row.createCell(j);
                    Object value = tableModel.getValueAt(i, j);
                    setCellValue(cell, value);
                }
            }
            
            // Auto-size columns
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                sheet.autoSizeColumn(j);
            }
            
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
        }
    }
    
    public static void exportSelectedToExcel(JTable table, File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Selected Data");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int j = 0; j < table.getColumnCount(); j++) {
                Cell cell = headerRow.createCell(j);
                cell.setCellValue(table.getColumnName(j));
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows for selected data
            int[] selectedRows = table.getSelectedRows();
            for (int i = 0; i < selectedRows.length; i++) {
                Row row = sheet.createRow(i + 1);
                int sourceRow = selectedRows[i];
                for (int j = 0; j < table.getColumnCount(); j++) {
                    Cell cell = row.createCell(j);
                    Object value = table.getValueAt(sourceRow, j);
                    setCellValue(cell, value);
                }
            }
            
            // Auto-size columns
            for (int j = 0; j < table.getColumnCount(); j++) {
                sheet.autoSizeColumn(j);
            }
            
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
        }
    }
    
    // JSON Export Methods
    public static void exportToJSON(TableModel tableModel, File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            ObjectNode rowNode = mapper.createObjectNode();
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object value = tableModel.getValueAt(i, j);
                String columnName = tableModel.getColumnName(j);
                
                if (value == null) {
                    rowNode.putNull(columnName);
                } else if (value instanceof Number) {
                    rowNode.put(columnName, value.toString());
                } else if (value instanceof Boolean) {
                    rowNode.put(columnName, (Boolean) value);
                } else {
                    rowNode.put(columnName, value.toString());
                }
            }
            arrayNode.add(rowNode);
        }
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);
    }
    
    public static void exportSelectedToJSON(JTable table, File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        
        int[] selectedRows = table.getSelectedRows();
        for (int rowIndex : selectedRows) {
            ObjectNode rowNode = mapper.createObjectNode();
            for (int j = 0; j < table.getColumnCount(); j++) {
                Object value = table.getValueAt(rowIndex, j);
                String columnName = table.getColumnName(j);
                
                if (value == null) {
                    rowNode.putNull(columnName);
                } else if (value instanceof Number) {
                    rowNode.put(columnName, value.toString());
                } else if (value instanceof Boolean) {
                    rowNode.put(columnName, (Boolean) value);
                } else {
                    rowNode.put(columnName, value.toString());
                }
            }
            arrayNode.add(rowNode);
        }
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);
    }
    
    // XML Export Methods
    public static void exportToXML(TableModel tableModel, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<data>\n");
            writer.write("  <exportInfo>\n");
            writer.write("    <timestamp>" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "</timestamp>\n");
            writer.write("    <rowCount>" + tableModel.getRowCount() + "</rowCount>\n");
            writer.write("    <columnCount>" + tableModel.getColumnCount() + "</columnCount>\n");
            writer.write("  </exportInfo>\n");
            writer.write("  <rows>\n");
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.write("    <row index=\"" + i + "\">\n");
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    String columnName = escapeXML(tableModel.getColumnName(j));
                    Object value = tableModel.getValueAt(i, j);
                    String valueStr = value != null ? escapeXML(value.toString()) : "";
                    writer.write("      <" + columnName + ">" + valueStr + "</" + columnName + ">\n");
                }
                writer.write("    </row>\n");
            }
            
            writer.write("  </rows>\n");
            writer.write("</data>\n");
        }
    }
    
    public static void exportSelectedToXML(JTable table, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<data>\n");
            writer.write("  <exportInfo>\n");
            writer.write("    <timestamp>" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "</timestamp>\n");
            writer.write("    <rowCount>" + table.getSelectedRowCount() + "</rowCount>\n");
            writer.write("    <columnCount>" + table.getColumnCount() + "</columnCount>\n");
            writer.write("  </exportInfo>\n");
            writer.write("  <rows>\n");
            
            int[] selectedRows = table.getSelectedRows();
            for (int i = 0; i < selectedRows.length; i++) {
                int rowIndex = selectedRows[i];
                writer.write("    <row index=\"" + i + "\" originalIndex=\"" + rowIndex + "\">\n");
                for (int j = 0; j < table.getColumnCount(); j++) {
                    String columnName = escapeXML(table.getColumnName(j));
                    Object value = table.getValueAt(rowIndex, j);
                    String valueStr = value != null ? escapeXML(value.toString()) : "";
                    writer.write("      <" + columnName + ">" + valueStr + "</" + columnName + ">\n");
                }
                writer.write("    </row>\n");
            }
            
            writer.write("  </rows>\n");
            writer.write("</data>\n");
        }
    }
    
    // Utility methods
    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            try {
                cell.setCellValue(Double.parseDouble(value.toString()));
            } catch (NumberFormatException e) {
                cell.setCellValue(value.toString());
            }
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    private static String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
    
    private static String escapeXML(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
