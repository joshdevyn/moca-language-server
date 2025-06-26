package com.github.joshdevyn.mocaide.gui.results;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for comparing results from different MOCA command executions
 */
public class ResultComparisonDialog extends JDialog {
    
    private JComboBox<TableEntry> leftTableSelector;
    private JComboBox<TableEntry> rightTableSelector;
    private JTable leftTable;
    private JTable rightTable;
    private JTable differenceTable;
    private JLabel comparisonStatusLabel;
    private JButton compareButton, closeButton;
    
    private List<TableEntry> availableTables;
    
    /**
     * Inner class to hold table data for comparison
     */
    private static class TableEntry {
        private final String name;
        private final TableModel tableModel;
        
        public TableEntry(String name, TableModel tableModel) {
            this.name = name;
            this.tableModel = tableModel;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
        public TableModel getTableModel() {
            return tableModel;
        }
    }
    
    public ResultComparisonDialog(Frame parent, List<TableModel> tables, List<String> tableNames) {
        super(parent, "Result Comparison", true);
        
        availableTables = new ArrayList<>();
        for (int i = 0; i < tables.size() && i < tableNames.size(); i++) {
            availableTables.add(new TableEntry(tableNames.get(i), tables.get(i)));
        }
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        setSize(1000, 700);
        setLocationRelativeTo(parent);
    }
    
    /**
     * Convenience method for comparing two specific tables
     */
    public static void showComparison(Frame parent, TableModel table1, String name1, 
                                     TableModel table2, String name2) {
        List<TableModel> tables = new ArrayList<>();
        List<String> names = new ArrayList<>();
        
        tables.add(table1);
        tables.add(table2);
        names.add(name1);
        names.add(name2);
        
        ResultComparisonDialog dialog = new ResultComparisonDialog(parent, tables, names);
        dialog.setVisible(true);
    }
    
    private void initializeComponents() {
        // Table selectors
        leftTableSelector = new JComboBox<>(availableTables.toArray(new TableEntry[0]));
        rightTableSelector = new JComboBox<>(availableTables.toArray(new TableEntry[0]));
        
        if (availableTables.size() > 1) {
            rightTableSelector.setSelectedIndex(1);
        }
        
        // Result tables
        leftTable = new JTable();
        rightTable = new JTable();
        differenceTable = new JTable();
        
        leftTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        rightTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        differenceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Buttons
        compareButton = new JButton("Compare");
        closeButton = new JButton("Close");
        
        // Status label
        comparisonStatusLabel = new JLabel("Select tables and click Compare");
        comparisonStatusLabel.setForeground(Color.BLUE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with selectors
        JPanel selectorPanel = new JPanel(new FlowLayout());
        selectorPanel.add(new JLabel("Left Table:"));
        selectorPanel.add(leftTableSelector);
        selectorPanel.add(Box.createHorizontalStrut(20));
        selectorPanel.add(new JLabel("Right Table:"));
        selectorPanel.add(rightTableSelector);
        selectorPanel.add(Box.createHorizontalStrut(20));
        selectorPanel.add(compareButton);
        
        add(selectorPanel, BorderLayout.NORTH);
        
        // Main comparison area
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Left table tab
        JScrollPane leftScrollPane = new JScrollPane(leftTable);
        leftScrollPane.setPreferredSize(new Dimension(400, 300));
        tabbedPane.addTab("Left Table", leftScrollPane);
        
        // Right table tab
        JScrollPane rightScrollPane = new JScrollPane(rightTable);
        rightScrollPane.setPreferredSize(new Dimension(400, 300));
        tabbedPane.addTab("Right Table", rightScrollPane);
        
        // Difference table tab
        JScrollPane diffScrollPane = new JScrollPane(differenceTable);
        diffScrollPane.setPreferredSize(new Dimension(400, 300));
        tabbedPane.addTab("Differences", diffScrollPane);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Bottom panel with status and close button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(comparisonStatusLabel, BorderLayout.CENTER);
        bottomPanel.add(closeButton, BorderLayout.EAST);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        compareButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performComparison();
            }
        });
        
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void performComparison() {
        TableEntry leftEntry = (TableEntry) leftTableSelector.getSelectedItem();
        TableEntry rightEntry = (TableEntry) rightTableSelector.getSelectedItem();
        
        if (leftEntry == null || rightEntry == null) {
            comparisonStatusLabel.setText("Please select both tables for comparison");
            comparisonStatusLabel.setForeground(Color.RED);
            return;
        }
        
        if (leftEntry == rightEntry) {
            comparisonStatusLabel.setText("Please select different tables for comparison");
            comparisonStatusLabel.setForeground(Color.RED);
            return;
        }
        
        TableModel leftModel = leftEntry.getTableModel();
        TableModel rightModel = rightEntry.getTableModel();
        
        // Update the display tables
        leftTable.setModel(leftModel);
        rightTable.setModel(rightModel);
        
        // Perform difference analysis
        TableModel diffModel = createDifferenceModel(leftModel, rightModel);
        differenceTable.setModel(diffModel);
        
        // Update status
        int diffCount = diffModel.getRowCount();
        comparisonStatusLabel.setText(String.format("Comparison complete. Found %d differences.", diffCount));
        comparisonStatusLabel.setForeground(diffCount > 0 ? Color.ORANGE : Color.GREEN);
    }
    
    private TableModel createDifferenceModel(TableModel left, TableModel right) {
        String[] columns = {"Type", "Location", "Left Value", "Right Value", "Description"};
        DefaultTableModel diffModel = new DefaultTableModel(columns, 0);
        
        // Check structural differences
        if (left.getRowCount() != right.getRowCount()) {
            diffModel.addRow(new Object[]{
                "Structure", 
                "Row Count", 
                left.getRowCount(), 
                right.getRowCount(),
                "Different number of rows"
            });
        }
        
        if (left.getColumnCount() != right.getColumnCount()) {
            diffModel.addRow(new Object[]{
                "Structure", 
                "Column Count", 
                left.getColumnCount(), 
                right.getColumnCount(),
                "Different number of columns"
            });
        }
        
        // Check column names
        int maxCols = Math.max(left.getColumnCount(), right.getColumnCount());
        for (int j = 0; j < maxCols; j++) {
            String leftColName = j < left.getColumnCount() ? left.getColumnName(j) : "<missing>";
            String rightColName = j < right.getColumnCount() ? right.getColumnName(j) : "<missing>";
            
            if (!leftColName.equals(rightColName)) {
                diffModel.addRow(new Object[]{
                    "Column", 
                    "Column " + j, 
                    leftColName, 
                    rightColName,
                    "Different column names"
                });
            }
        }
        
        // Check data differences (only if both tables have same structure)
        if (left.getRowCount() > 0 && right.getRowCount() > 0 && 
            left.getColumnCount() == right.getColumnCount()) {
            
            int maxRows = Math.max(left.getRowCount(), right.getRowCount());
            for (int i = 0; i < maxRows; i++) {
                for (int j = 0; j < left.getColumnCount(); j++) {
                    Object leftValue = i < left.getRowCount() ? left.getValueAt(i, j) : "<missing>";
                    Object rightValue = i < right.getRowCount() ? right.getValueAt(i, j) : "<missing>";
                    
                    if (!isEqual(leftValue, rightValue)) {
                        diffModel.addRow(new Object[]{
                            "Data", 
                            String.format("Row %d, Col %d (%s)", i, j, left.getColumnName(j)), 
                            leftValue != null ? leftValue.toString() : "<null>", 
                            rightValue != null ? rightValue.toString() : "<null>",
                            "Different values"
                        });
                    }
                }
            }
        }
        
        return diffModel;
    }
    
    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.toString().equals(right.toString());
    }
}
