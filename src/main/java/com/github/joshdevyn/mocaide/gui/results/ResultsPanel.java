package com.github.joshdevyn.mocaide.gui.results;

import com.github.joshdevyn.mocaide.util.ClipboardUtil;
import com.github.joshdevyn.mocaide.util.ExportUtil;
import com.github.mrglassdanny.mocalanguageserver.moca.connection.MocaResults;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced results panel supporting multiple result tables, cell/table selection,
 * copy/export operations, and advanced data grid features
 */
public class ResultsPanel extends JPanel {
    
    private JTabbedPane resultsTabbedPane;
    private List<TabContent> resultTabs;
    private JLabel statusLabel;
    private JButton clearAllButton, exportAllButton, compareResultsButton;
    
    // Context menu for table operations
    private JPopupMenu tableContextMenu;
    private JMenuItem copySelectedMenuItem, copyAllMenuItem, exportSelectedMenuItem, exportAllMenuItem;
    
    // Interface for tab content
    public interface TabContent {
        int getRowCount();
        JPanel getPanel();
    }
    
    public ResultsPanel() {
        this.resultTabs = new ArrayList<>();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        resultsTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        resultsTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        
        // Control buttons
        clearAllButton = new JButton("Clear All");
        clearAllButton.setToolTipText("Clear all result tabs");
        
        exportAllButton = new JButton("Export All");
        exportAllButton.setToolTipText("Export all results to files");
        
        compareResultsButton = new JButton("Compare Results");
        compareResultsButton.setToolTipText("Compare two result sets");
        compareResultsButton.setEnabled(false); // Enabled when 2+ tabs exist
        
        statusLabel = new JLabel("No results");
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
        statusLabel.setForeground(Color.GRAY);
        
        // Create context menu
        createContextMenu();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Results", 
            TitledBorder.LEFT, 
            TitledBorder.TOP
        ));
        
        // Top panel with controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(clearAllButton);
        topPanel.add(exportAllButton);
        topPanel.add(compareResultsButton);
        add(topPanel, BorderLayout.NORTH);
        
        // Main results area
        add(resultsTabbedPane, BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        clearAllButton.addActionListener(e -> clearAllResults());
        exportAllButton.addActionListener(e -> exportAllResults());
        compareResultsButton.addActionListener(e -> showResultComparison());
    }
    
    private void createContextMenu() {
        tableContextMenu = new JPopupMenu();
        
        copySelectedMenuItem = new JMenuItem("Copy Selected");
        copySelectedMenuItem.addActionListener(e -> copySelectedData());
        
        copyAllMenuItem = new JMenuItem("Copy All");
        copyAllMenuItem.addActionListener(e -> copyAllData());
        
        exportSelectedMenuItem = new JMenuItem("Export Selected...");
        exportSelectedMenuItem.addActionListener(e -> exportSelectedData());
        
        exportAllMenuItem = new JMenuItem("Export All...");
        exportAllMenuItem.addActionListener(e -> exportCurrentTableData());
        
        tableContextMenu.add(copySelectedMenuItem);
        tableContextMenu.add(copyAllMenuItem);
        tableContextMenu.addSeparator();
        tableContextMenu.add(exportSelectedMenuItem);
        tableContextMenu.add(exportAllMenuItem);
    }
    
    /**
     * Add a new result set to the panel
     */
    public void addResult(String commandDescription, MocaResults results, double executionTime) {
        String tabTitle = String.format("Result %d", resultTabs.size() + 1);
        if (commandDescription != null && !commandDescription.trim().isEmpty()) {
            // Truncate long commands for tab title
            String shortDesc = commandDescription.length() > 30 
                ? commandDescription.substring(0, 30) + "..." 
                : commandDescription;
            tabTitle = shortDesc;
        }
        
        ResultTab resultTab = new ResultTab(tabTitle, commandDescription, results, executionTime);
        resultTabs.add(resultTab);
        
        // Add close button to tab
        JPanel tabPanel = createTabPanel(tabTitle, resultTab);
        resultsTabbedPane.addTab("", resultTab.getPanel());
        resultsTabbedPane.setTabComponentAt(resultsTabbedPane.getTabCount() - 1, tabPanel);
        
        // Select the new tab
        resultsTabbedPane.setSelectedIndex(resultsTabbedPane.getTabCount() - 1);
        
        updateStatusLabel();
    }
    
    private JPanel createTabPanel(String title, TabContent tabContent) {
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        
        JButton closeButton = new JButton("×");
        closeButton.setPreferredSize(new Dimension(16, 16));
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setFocusable(false);
        closeButton.setToolTipText("Close tab");
        
        closeButton.addActionListener(e -> {
            int index = resultsTabbedPane.indexOfTabComponent(tabPanel);
            if (index != -1) {
                ResultsPanel.this.resultTabs.remove(index);
                resultsTabbedPane.removeTabAt(index);
                updateStatusLabel();
            }
        });
        
        tabPanel.add(titleLabel);
        tabPanel.add(closeButton);
        
        return tabPanel;
    }
    
    private void updateStatusLabel() {
        if (resultTabs.isEmpty()) {
            statusLabel.setText("No results");
            compareResultsButton.setEnabled(false);
        } else {
            int totalRows = resultTabs.stream().mapToInt(tab -> tab.getRowCount()).sum();
            statusLabel.setText(String.format("%d result sets, %d total rows", resultTabs.size(), totalRows));
            
            // Enable compare button only if we have 2+ result tabs that are actual results (not errors)
            long resultTabCount = resultTabs.stream()
                .filter(tab -> tab instanceof ResultTab)
                .count();
            compareResultsButton.setEnabled(resultTabCount >= 2);
        }
    }
    
    private void clearAllResults() {
        resultTabs.clear();
        resultsTabbedPane.removeAll();
        updateStatusLabel();
    }
    
    private void exportAllResults() {
        if (resultTabs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results to export", "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Show dialog to select export format and directory
        String[] options = {"CSV", "Excel", "JSON", "XML"};
        int choice = JOptionPane.showOptionDialog(this,
            "Select export format for all results:",
            "Export All Results",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
            
        if (choice == -1) return; // User cancelled
        
        String extension = options[choice].toLowerCase();
        if (extension.equals("excel")) extension = "xlsx";
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Export Directory");
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File directory = fileChooser.getSelectedFile();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            int successCount = 0;
            
            for (int i = 0; i < resultTabs.size(); i++) {
                TabContent tab = resultTabs.get(i);
                String fileName = String.format("result_%d_%s.%s", i + 1, timestamp, extension);
                File file = new File(directory, fileName);
                
                try {
                    if (tab instanceof ResultTab) {
                        ResultTab resultTab = (ResultTab) tab;
                        if (choice == 0) { // CSV
                            ExportUtil.exportToCSV(resultTab.table.getModel(), file);
                        } else if (choice == 1) { // Excel
                            ExportUtil.exportToExcel(resultTab.table.getModel(), file);
                        } else if (choice == 2) { // JSON
                            ExportUtil.exportToJSON(resultTab.table.getModel(), file);
                        } else if (choice == 3) { // XML
                            ExportUtil.exportToXML(resultTab.table.getModel(), file);
                        }
                        successCount++;
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, 
                        "Error exporting " + fileName + ": " + e.getMessage(), 
                        "Export Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            
            JOptionPane.showMessageDialog(this, 
                String.format("Successfully exported %d result sets to %s", successCount, directory.getAbsolutePath()), 
                "Export Complete", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void copySelectedData() {
        TabContent currentTab = getCurrentResultTab();
        if (currentTab instanceof ResultTab) {
            ((ResultTab) currentTab).copySelectedToClipboard();
        }
    }
    
    private void copyAllData() {
        TabContent currentTab = getCurrentResultTab();
        if (currentTab instanceof ResultTab) {
            ((ResultTab) currentTab).copyAllToClipboard();
        }
    }
    
    private void exportSelectedData() {
        TabContent currentTab = getCurrentResultTab();
        if (currentTab instanceof ResultTab) {
            ((ResultTab) currentTab).exportSelected();
        }
    }
    
    private void exportCurrentTableData() {
        TabContent currentTab = getCurrentResultTab();
        if (currentTab instanceof ResultTab) {
            ((ResultTab) currentTab).exportAll();
        }
    }
    
    private TabContent getCurrentResultTab() {
        int selectedIndex = resultsTabbedPane.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < resultTabs.size()) {
            return resultTabs.get(selectedIndex);
        }
        return null;
    }
    
    /**
     * Add an error result to the panel
     */
    public void addError(String commandDescription, String command, String errorMessage) {
        String tabTitle = String.format("Error %d", resultTabs.size() + 1);
        if (commandDescription != null && !commandDescription.trim().isEmpty()) {
            String shortDesc = commandDescription.length() > 30 
                ? commandDescription.substring(0, 30) + "..." 
                : commandDescription;
            tabTitle = shortDesc + " (Error)";
        }
        
        ErrorTab errorTab = new ErrorTab(tabTitle, commandDescription, command, errorMessage);
        resultTabs.add(errorTab);
        
        // Add close button to tab
        JPanel tabPanel = createTabPanel(tabTitle, errorTab);
        resultsTabbedPane.addTab("", errorTab.getPanel());
        resultsTabbedPane.setTabComponentAt(resultsTabbedPane.getTabCount() - 1, tabPanel);
        
        // Select the new tab
        resultsTabbedPane.setSelectedIndex(resultsTabbedPane.getTabCount() - 1);
        
        updateStatusLabel();
    }
    
    /**
     * Clear all results from the panel
     */
    public void clearResults() {
        clearAllResults();
    }

    /**
     * Inner class representing a single result tab
     */
    private class ResultTab extends JPanel implements TabContent {
        private String title;
        private String commandDescription;
        private MocaResults results;
        private double executionTime;
        private JTable table;
        private DefaultTableModel tableModel;
        private TableRowSorter<DefaultTableModel> sorter;
        private JTextField searchField;
        
        public ResultTab(String title, String commandDescription, MocaResults results, double executionTime) {
            this.title = title;
            this.commandDescription = commandDescription;
            this.results = results;
            this.executionTime = executionTime;
            
            initializeTab();
            populateTable();
        }
        
        private void initializeTab() {
            setLayout(new BorderLayout());
            
            // Create table model and table
            tableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make table read-only
                }
            };
            
            table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.setCellSelectionEnabled(true);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            
            // Add sorting
            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);
            
            // Add context menu
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        tableContextMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
            
            // Top panel with search and info
            JPanel topPanel = new JPanel(new BorderLayout());
            
            // Search panel
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.add(new JLabel("Search:"));
            searchField = new JTextField(20);
            searchField.addActionListener(e -> filterTable());
            searchPanel.add(searchField);
            
            JButton searchButton = new JButton("Filter");
            searchButton.addActionListener(e -> filterTable());
            searchPanel.add(searchButton);
            
            JButton clearFilterButton = new JButton("Clear");
            clearFilterButton.addActionListener(e -> clearFilter());
            searchPanel.add(clearFilterButton);
            
            topPanel.add(searchPanel, BorderLayout.WEST);
            
            // Info panel
            JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            if (commandDescription != null && !commandDescription.trim().isEmpty()) {
                JLabel commandLabel = new JLabel("Command: " + commandDescription);
                commandLabel.setFont(commandLabel.getFont().deriveFont(Font.ITALIC));
                infoPanel.add(commandLabel);
            }
            
            JLabel timeLabel = new JLabel(String.format("Time: %.2fs", executionTime));
            timeLabel.setFont(timeLabel.getFont().deriveFont(11f));
            infoPanel.add(timeLabel);
            
            topPanel.add(infoPanel, BorderLayout.EAST);
            
            add(topPanel, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
        }
        
        private void populateTable() {
            if (results == null) {
                return;
            }
            
            // Add columns
            String[] columns = results.getColumnNames();
            for (String column : columns) {
                tableModel.addColumn(column);
            }
            
            // Add rows
            for (int i = 0; i < results.getRowCount(); i++) {
                Object[] row = new Object[columns.length];
                for (int j = 0; j < columns.length; j++) {
                    row[j] = results.getString(i, columns[j]);
                }
                tableModel.addRow(row);
            }
            
            // Auto-resize columns
            for (int i = 0; i < table.getColumnCount(); i++) {
                TableColumn column = table.getColumnModel().getColumn(i);
                int width = Math.max(100, Math.min(200, column.getPreferredWidth()));
                column.setPreferredWidth(width);
            }
        }
        
        private void filterTable() {
            String searchText = searchField.getText().trim();
            if (searchText.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
            }
        }
        
        private void clearFilter() {
            searchField.setText("");
            sorter.setRowFilter(null);
        }
        
        public int getRowCount() {
            return results != null ? results.getRowCount() : 0;
        }
        
        @Override
        public JPanel getPanel() {
            return this;
        }
        
        public void copySelectedToClipboard() {
            ClipboardUtil.showCopyDialog(this, table);
        }
        
        public void copyAllToClipboard() {
            ClipboardUtil.showCopyDialog(this, table.getModel());
        }
        
        public void exportSelected() {
            ExportUtil.showExportDialog(this, table, title + "_selected");
        }
        
        public void exportAll() {
            ExportUtil.showExportDialog(this, table.getModel(), title);
        }
    }
    
    /**
     * Inner class representing an error tab
     */
    private class ErrorTab extends JPanel implements TabContent {
        private String commandDescription;
        private String command;
        private String errorMessage;
        
        public ErrorTab(String title, String commandDescription, String command, String errorMessage) {
            this.commandDescription = commandDescription;
            this.command = command;
            this.errorMessage = errorMessage;
            
            initializeErrorTab();
        }
        
        private void initializeErrorTab() {
            setLayout(new BorderLayout());
            
            // Top panel with command info
            JPanel topPanel = new JPanel(new BorderLayout());
            if (commandDescription != null && !commandDescription.trim().isEmpty()) {
                JLabel commandLabel = new JLabel("Command: " + commandDescription);
                commandLabel.setFont(commandLabel.getFont().deriveFont(Font.BOLD));
                topPanel.add(commandLabel, BorderLayout.NORTH);
            }
            
            if (command != null && !command.trim().isEmpty()) {
                JTextArea commandArea = new JTextArea(3, 50);
                commandArea.setText(command);
                commandArea.setEditable(false);
                commandArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                commandArea.setBorder(BorderFactory.createTitledBorder("Command"));
                JScrollPane commandScroll = new JScrollPane(commandArea);
                topPanel.add(commandScroll, BorderLayout.CENTER);
            }
            
            add(topPanel, BorderLayout.NORTH);
            
            // Error message area
            JTextArea errorArea = new JTextArea(10, 50);
            errorArea.setText(errorMessage);
            errorArea.setEditable(false);
            errorArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            errorArea.setForeground(Color.RED);
            errorArea.setBorder(BorderFactory.createTitledBorder("Error Details"));
            
            JScrollPane errorScroll = new JScrollPane(errorArea);
            add(errorScroll, BorderLayout.CENTER);
        }
        
        public int getRowCount() {
            return 0; // Error tabs don't have rows
        }
        
        @Override
        public JPanel getPanel() {
            return this;
        }
    }
    
    private void showResultComparison() {
        // Get all result tables for comparison
        List<TableModel> tables = new ArrayList<>();
        List<String> tableNames = new ArrayList<>();
        
        for (int i = 0; i < resultTabs.size(); i++) {
            TabContent tab = resultTabs.get(i);
            if (tab instanceof ResultTab) {
                ResultTab resultTab = (ResultTab) tab;
                tables.add(resultTab.table.getModel());
                tableNames.add(resultsTabbedPane.getTitleAt(i));
            }
        }
        
        if (tables.size() < 2) {
            JOptionPane.showMessageDialog(this, 
                "At least 2 result sets are needed for comparison", 
                "Insufficient Data", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Show comparison dialog
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        ResultComparisonDialog dialog = new ResultComparisonDialog(parentFrame, tables, tableNames);
        dialog.setVisible(true);
    }
}
