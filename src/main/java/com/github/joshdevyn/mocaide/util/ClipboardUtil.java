package com.github.joshdevyn.mocaide.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Utility class for clipboard operations with multiple format support
 */
public class ClipboardUtil {
    
    public enum ClipboardFormat {
        PLAIN_TEXT("Plain Text"),
        CSV("CSV"),
        TSV("Tab Separated"),
        JSON("JSON"),
        HTML_TABLE("HTML Table");
        
        private final String description;
        
        ClipboardFormat(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
    
    /**
     * Show copy format dialog and copy data to clipboard
     */
    public static void showCopyDialog(Component parent, TableModel tableModel) {
        String[] options = {"Plain Text", "CSV", "Tab Separated", "JSON", "HTML Table"};
        int choice = JOptionPane.showOptionDialog(parent,
            "Select copy format:",
            "Copy Format",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
            
        if (choice == -1) return; // User cancelled
        
        ClipboardFormat format = ClipboardFormat.values()[choice];
        copyToClipboard(tableModel, format);
        
        JOptionPane.showMessageDialog(parent, 
            String.format("Data copied to clipboard as %s format", format.getDescription()),
            "Copy Complete",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show copy format dialog and copy selected data to clipboard
     */
    public static void showCopyDialog(Component parent, JTable table) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(parent, 
                "No rows selected. Please select rows to copy.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String[] options = {"Plain Text", "CSV", "Tab Separated", "JSON", "HTML Table"};
        int choice = JOptionPane.showOptionDialog(parent,
            "Select copy format:",
            "Copy Format",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
            
        if (choice == -1) return; // User cancelled
        
        ClipboardFormat format = ClipboardFormat.values()[choice];
        copySelectedToClipboard(table, format);
        
        JOptionPane.showMessageDialog(parent, 
            String.format("Selected data copied to clipboard as %s format", format.getDescription()),
            "Copy Complete",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Copy all table data to clipboard in specified format
     */
    public static void copyToClipboard(TableModel tableModel, ClipboardFormat format) {
        String data = formatTableData(tableModel, format);
        copyStringToClipboard(data);
    }
    
    /**
     * Copy selected table data to clipboard in specified format
     */
    public static void copySelectedToClipboard(JTable table, ClipboardFormat format) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) return;
        
        String data = formatSelectedTableData(table, selectedRows, format);
        copyStringToClipboard(data);
    }
    
    /**
     * Simple copy methods for default formats
     */
    public static void copyToClipboard(TableModel tableModel) {
        copyToClipboard(tableModel, ClipboardFormat.PLAIN_TEXT);
    }
    
    public static void copySelectedToClipboard(JTable table) {
        copySelectedToClipboard(table, ClipboardFormat.PLAIN_TEXT);
    }
    
    private static String formatTableData(TableModel tableModel, ClipboardFormat format) {
        switch (format) {
            case PLAIN_TEXT:
                return formatAsPlainText(tableModel);
            case CSV:
                return formatAsCSV(tableModel);
            case TSV:
                return formatAsTSV(tableModel);
            case JSON:
                return formatAsJSON(tableModel);
            case HTML_TABLE:
                return formatAsHTML(tableModel);
            default:
                return formatAsPlainText(tableModel);
        }
    }
    
    private static String formatSelectedTableData(JTable table, int[] selectedRows, ClipboardFormat format) {
        switch (format) {
            case PLAIN_TEXT:
                return formatSelectedAsPlainText(table, selectedRows);
            case CSV:
                return formatSelectedAsCSV(table, selectedRows);
            case TSV:
                return formatSelectedAsTSV(table, selectedRows);
            case JSON:
                return formatSelectedAsJSON(table, selectedRows);
            case HTML_TABLE:
                return formatSelectedAsHTML(table, selectedRows);
            default:
                return formatSelectedAsPlainText(table, selectedRows);
        }
    }
    
    private static String formatAsPlainText(TableModel tableModel) {
        StringBuilder sb = new StringBuilder();
        
        // Headers
        for (int j = 0; j < tableModel.getColumnCount(); j++) {
            if (j > 0) sb.append("\t");
            sb.append(tableModel.getColumnName(j));
        }
        sb.append("\n");
        
        // Data
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                if (j > 0) sb.append("\t");
                Object value = tableModel.getValueAt(i, j);
                sb.append(value != null ? value.toString() : "");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    private static String formatSelectedAsPlainText(JTable table, int[] selectedRows) {
        StringBuilder sb = new StringBuilder();
        
        // Headers
        for (int j = 0; j < table.getColumnCount(); j++) {
            if (j > 0) sb.append("\t");
            sb.append(table.getColumnName(j));
        }
        sb.append("\n");
        
        // Selected data
        for (int rowIndex : selectedRows) {
            for (int j = 0; j < table.getColumnCount(); j++) {
                if (j > 0) sb.append("\t");
                Object value = table.getValueAt(rowIndex, j);
                sb.append(value != null ? value.toString() : "");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    private static String formatAsCSV(TableModel tableModel) {
        StringBuilder sb = new StringBuilder();
        
        // Headers
        for (int j = 0; j < tableModel.getColumnCount(); j++) {
            if (j > 0) sb.append(",");
            sb.append("\"").append(escapeCSV(tableModel.getColumnName(j))).append("\"");
        }
        sb.append("\n");
        
        // Data
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                if (j > 0) sb.append(",");
                Object value = tableModel.getValueAt(i, j);
                String valueStr = value != null ? value.toString() : "";
                sb.append("\"").append(escapeCSV(valueStr)).append("\"");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    private static String formatSelectedAsCSV(JTable table, int[] selectedRows) {
        StringBuilder sb = new StringBuilder();
        
        // Headers
        for (int j = 0; j < table.getColumnCount(); j++) {
            if (j > 0) sb.append(",");
            sb.append("\"").append(escapeCSV(table.getColumnName(j))).append("\"");
        }
        sb.append("\n");
        
        // Selected data
        for (int rowIndex : selectedRows) {
            for (int j = 0; j < table.getColumnCount(); j++) {
                if (j > 0) sb.append(",");
                Object value = table.getValueAt(rowIndex, j);
                String valueStr = value != null ? value.toString() : "";
                sb.append("\"").append(escapeCSV(valueStr)).append("\"");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    private static String formatAsTSV(TableModel tableModel) {
        StringBuilder sb = new StringBuilder();
        
        // Headers
        for (int j = 0; j < tableModel.getColumnCount(); j++) {
            if (j > 0) sb.append("\t");
            sb.append(tableModel.getColumnName(j).replace("\t", " "));
        }
        sb.append("\n");
        
        // Data
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                if (j > 0) sb.append("\t");
                Object value = tableModel.getValueAt(i, j);
                String valueStr = value != null ? value.toString() : "";
                sb.append(valueStr.replace("\t", " "));
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    private static String formatSelectedAsTSV(JTable table, int[] selectedRows) {
        StringBuilder sb = new StringBuilder();
        
        // Headers
        for (int j = 0; j < table.getColumnCount(); j++) {
            if (j > 0) sb.append("\t");
            sb.append(table.getColumnName(j).replace("\t", " "));
        }
        sb.append("\n");
        
        // Selected data
        for (int rowIndex : selectedRows) {
            for (int j = 0; j < table.getColumnCount(); j++) {
                if (j > 0) sb.append("\t");
                Object value = table.getValueAt(rowIndex, j);
                String valueStr = value != null ? value.toString() : "";
                sb.append(valueStr.replace("\t", " "));
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    private static String formatAsJSON(TableModel tableModel) {
        try {
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
            
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
        } catch (Exception e) {
            return "Error formatting as JSON: " + e.getMessage();
        }
    }
    
    private static String formatSelectedAsJSON(JTable table, int[] selectedRows) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode arrayNode = mapper.createArrayNode();
            
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
            
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
        } catch (Exception e) {
            return "Error formatting as JSON: " + e.getMessage();
        }
    }
    
    private static String formatAsHTML(TableModel tableModel) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border=\"1\">\n");
        
        // Headers
        sb.append("  <thead>\n    <tr>\n");
        for (int j = 0; j < tableModel.getColumnCount(); j++) {
            sb.append("      <th>").append(escapeHTML(tableModel.getColumnName(j))).append("</th>\n");
        }
        sb.append("    </tr>\n  </thead>\n");
        
        // Data
        sb.append("  <tbody>\n");
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            sb.append("    <tr>\n");
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object value = tableModel.getValueAt(i, j);
                String valueStr = value != null ? value.toString() : "";
                sb.append("      <td>").append(escapeHTML(valueStr)).append("</td>\n");
            }
            sb.append("    </tr>\n");
        }
        sb.append("  </tbody>\n");
        sb.append("</table>");
        
        return sb.toString();
    }
    
    private static String formatSelectedAsHTML(JTable table, int[] selectedRows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border=\"1\">\n");
        
        // Headers
        sb.append("  <thead>\n    <tr>\n");
        for (int j = 0; j < table.getColumnCount(); j++) {
            sb.append("      <th>").append(escapeHTML(table.getColumnName(j))).append("</th>\n");
        }
        sb.append("    </tr>\n  </thead>\n");
        
        // Selected data
        sb.append("  <tbody>\n");
        for (int rowIndex : selectedRows) {
            sb.append("    <tr>\n");
            for (int j = 0; j < table.getColumnCount(); j++) {
                Object value = table.getValueAt(rowIndex, j);
                String valueStr = value != null ? value.toString() : "";
                sb.append("      <td>").append(escapeHTML(valueStr)).append("</td>\n");
            }
            sb.append("    </tr>\n");
        }
        sb.append("  </tbody>\n");
        sb.append("</table>");
        
        return sb.toString();
    }
    
    private static void copyStringToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }
    
    private static String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
    
    private static String escapeHTML(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
