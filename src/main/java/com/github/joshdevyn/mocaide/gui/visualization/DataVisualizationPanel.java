package com.github.joshdevyn.mocaide.gui.visualization;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Data visualization panel with statistics and simple charting capabilities
 */
public class DataVisualizationPanel extends JPanel {
    
    private JTabbedPane tabbedPane;
    private JPanel statisticsPanel;
    private JPanel chartPanel;
    private JPanel validationPanel;
    private TableModel currentData;
    
    // Statistics components
    private JTextArea statisticsText;
    private JTable dataPreview;
    
    // Chart components
    private JComboBox<String> chartTypeCombo;
    private JComboBox<String> xAxisCombo;
    private JComboBox<String> yAxisCombo;
    private JPanel chartDisplayPanel;
    
    // Validation components
    private JTextArea validationResults;
    private JButton validateButton;
    
    public DataVisualizationPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        
        // Statistics Panel
        statisticsPanel = createStatisticsPanel();
        
        // Chart Panel
        chartPanel = createChartPanel();
        
        // Validation Panel
        validationPanel = createValidationPanel();
        
        tabbedPane.addTab("Statistics", createIcon("📊"), statisticsPanel, "View data statistics and summary");
        tabbedPane.addTab("Charts", createIcon("📈"), chartPanel, "Create charts and visualizations");
        tabbedPane.addTab("Validation", createIcon("✓"), validationPanel, "Validate data quality");
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Statistics text area
        statisticsText = new JTextArea(10, 40);
        statisticsText.setEditable(false);
        statisticsText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statisticsText.setBorder(BorderFactory.createTitledBorder("Data Statistics"));
        
        JScrollPane statsScrollPane = new JScrollPane(statisticsText);
        
        // Data preview table
        dataPreview = new JTable();
        dataPreview.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        dataPreview.setBorder(BorderFactory.createTitledBorder("Data Preview"));
        
        JScrollPane previewScrollPane = new JScrollPane(dataPreview);
        previewScrollPane.setPreferredSize(new Dimension(400, 200));
        
        // Split pane for statistics and preview
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statsScrollPane, previewScrollPane);
        splitPane.setResizeWeight(0.6);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton refreshStatsButton = new JButton("Refresh Statistics");
        refreshStatsButton.addActionListener(e -> updateStatistics());
        controlPanel.add(refreshStatsButton);
        
        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Chart configuration panel
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("Chart Configuration"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Chart type selection
        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(new JLabel("Chart Type:"), gbc);
        
        chartTypeCombo = new JComboBox<>(new String[]{"Bar Chart", "Line Chart", "Pie Chart", "Histogram"});
        gbc.gridx = 1; gbc.gridy = 0;
        configPanel.add(chartTypeCombo, gbc);
        
        // X-axis selection
        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(new JLabel("X-Axis:"), gbc);
        
        xAxisCombo = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 1;
        configPanel.add(xAxisCombo, gbc);
        
        // Y-axis selection
        gbc.gridx = 0; gbc.gridy = 2;
        configPanel.add(new JLabel("Y-Axis:"), gbc);
        
        yAxisCombo = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 2;
        configPanel.add(yAxisCombo, gbc);
        
        // Create chart button
        JButton createChartButton = new JButton("Create Chart");
        createChartButton.addActionListener(e -> createChart());
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        configPanel.add(createChartButton, gbc);
        
        // Chart display panel
        chartDisplayPanel = new JPanel(new BorderLayout());
        chartDisplayPanel.setBorder(BorderFactory.createTitledBorder("Chart Display"));
        chartDisplayPanel.setPreferredSize(new Dimension(400, 300));
        
        JLabel noChartLabel = new JLabel("No chart created yet", JLabel.CENTER);
        noChartLabel.setForeground(Color.GRAY);
        chartDisplayPanel.add(noChartLabel, BorderLayout.CENTER);
        
        panel.add(configPanel, BorderLayout.NORTH);
        panel.add(chartDisplayPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createValidationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Validation controls
        JPanel controlPanel = new JPanel(new FlowLayout());
        validateButton = new JButton("Validate Data");
        validateButton.addActionListener(e -> validateData());
        controlPanel.add(validateButton);
        
        JButton checkDuplicatesButton = new JButton("Check Duplicates");
        checkDuplicatesButton.addActionListener(e -> checkDuplicates());
        controlPanel.add(checkDuplicatesButton);
        
        JButton checkNullsButton = new JButton("Check Nulls");
        checkNullsButton.addActionListener(e -> checkNulls());
        controlPanel.add(checkNullsButton);
        
        // Validation results
        validationResults = new JTextArea(15, 50);
        validationResults.setEditable(false);
        validationResults.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(validationResults);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Validation Results"));
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Update axis options when data changes
        chartTypeCombo.addActionListener(e -> updateChartOptions());
    }
    
    public void setData(TableModel data) {
        this.currentData = data;
        updateDataPreview();
        updateAxisOptions();
        updateStatistics();
    }
    
    private void updateDataPreview() {
        if (currentData != null) {
            // Show first 10 rows of data
            int rowCount = Math.min(10, currentData.getRowCount());
            int colCount = currentData.getColumnCount();
            
            Object[][] previewData = new Object[rowCount][colCount];
            String[] columnNames = new String[colCount];
            
            // Get column names
            for (int i = 0; i < colCount; i++) {
                columnNames[i] = currentData.getColumnName(i);
            }
            
            // Get preview data
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < colCount; j++) {
                    previewData[i][j] = currentData.getValueAt(i, j);
                }
            }
            
            dataPreview.setModel(new DefaultTableModel(previewData, columnNames));
        }
    }
    
    private void updateAxisOptions() {
        if (currentData != null) {
            xAxisCombo.removeAllItems();
            yAxisCombo.removeAllItems();
            
            for (int i = 0; i < currentData.getColumnCount(); i++) {
                String columnName = currentData.getColumnName(i);
                xAxisCombo.addItem(columnName);
                yAxisCombo.addItem(columnName);
            }
        }
    }
    
    private void updateChartOptions() {
        // Update available options based on chart type
        String selectedType = (String) chartTypeCombo.getSelectedItem();
        if ("Pie Chart".equals(selectedType)) {
            yAxisCombo.setEnabled(false);
        } else {
            yAxisCombo.setEnabled(true);
        }
    }
    
    private void updateStatistics() {
        if (currentData == null) {
            statisticsText.setText("No data available for analysis.");
            return;
        }
        
        StringBuilder stats = new StringBuilder();
        stats.append("=== DATA STATISTICS ===\n\n");
        
        int rowCount = currentData.getRowCount();
        int colCount = currentData.getColumnCount();
        
        stats.append(String.format("Rows: %d\n", rowCount));
        stats.append(String.format("Columns: %d\n\n", colCount));
        
        // Column statistics
        stats.append("=== COLUMN ANALYSIS ===\n");
        for (int col = 0; col < colCount; col++) {
            String columnName = currentData.getColumnName(col);
            stats.append(String.format("\n%s:\n", columnName));
            
            // Analyze column data
            Map<Object, Integer> valueCount = new HashMap<>();
            int nullCount = 0;
            boolean isNumeric = true;
            List<Double> numericValues = new ArrayList<>();
            
            for (int row = 0; row < rowCount; row++) {
                Object value = currentData.getValueAt(row, col);
                
                if (value == null || value.toString().trim().isEmpty()) {
                    nullCount++;
                } else {
                    valueCount.put(value, valueCount.getOrDefault(value, 0) + 1);
                    
                    // Try to parse as number
                    try {
                        double numValue = Double.parseDouble(value.toString());
                        numericValues.add(numValue);
                    } catch (NumberFormatException e) {
                        isNumeric = false;
                    }
                }
            }
            
            stats.append(String.format("  Non-null values: %d\n", rowCount - nullCount));
            stats.append(String.format("  Null/empty values: %d\n", nullCount));
            stats.append(String.format("  Unique values: %d\n", valueCount.size()));
            
            if (isNumeric && !numericValues.isEmpty()) {
                // Numeric statistics
                double sum = numericValues.stream().mapToDouble(Double::doubleValue).sum();
                double avg = sum / numericValues.size();
                double min = numericValues.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                double max = numericValues.stream().mapToDouble(Double::doubleValue).max().orElse(0);
                
                stats.append(String.format("  Min: %.2f\n", min));
                stats.append(String.format("  Max: %.2f\n", max));
                stats.append(String.format("  Average: %.2f\n", avg));
                stats.append(String.format("  Sum: %.2f\n", sum));
            }
            
            // Show most common values
            if (!valueCount.isEmpty()) {
                stats.append("  Most common values:\n");
                valueCount.entrySet().stream()
                    .sorted(Map.Entry.<Object, Integer>comparingByValue().reversed())
                    .limit(3)
                    .forEach(entry -> stats.append(String.format("    %s: %d times\n", 
                        entry.getKey(), entry.getValue())));
            }
        }
        
        statisticsText.setText(stats.toString());
    }
    
    private void createChart() {
        if (currentData == null) {
            JOptionPane.showMessageDialog(this, "No data available for charting.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String chartType = (String) chartTypeCombo.getSelectedItem();
        String xAxis = (String) xAxisCombo.getSelectedItem();
        String yAxis = (String) yAxisCombo.getSelectedItem();
        
        // Create simple chart visualization
        chartDisplayPanel.removeAll();
        
        JPanel chartContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSimpleChart(g, chartType, xAxis, yAxis);
            }
        };
        
        chartContent.setPreferredSize(new Dimension(400, 300));
        chartDisplayPanel.add(chartContent, BorderLayout.CENTER);
        chartDisplayPanel.revalidate();
        chartDisplayPanel.repaint();
    }
    
    private void drawSimpleChart(Graphics g, String chartType, String xAxis, String yAxis) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth() - 60;
        int height = getHeight() - 60;
        int startX = 40;
        int startY = 40;
        
        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(startX, startY + height, startX + width, startY + height); // X-axis
        g2d.drawLine(startX, startY, startX, startY + height); // Y-axis
        
        // Draw title
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        String title = String.format("%s: %s vs %s", chartType, xAxis, yAxis != null ? yAxis : "Count");
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (getWidth() - titleWidth) / 2, 20);
        
        // Simple bar chart representation
        if ("Bar Chart".equals(chartType) && currentData != null) {
            drawBarChart(g2d, startX, startY, width, height, xAxis, yAxis);
        } else {
            // Show placeholder
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            String message = "Chart visualization: " + chartType;
            FontMetrics msgFm = g2d.getFontMetrics();
            int msgWidth = msgFm.stringWidth(message);
            g2d.drawString(message, (getWidth() - msgWidth) / 2, getHeight() / 2);
        }
        
        g2d.dispose();
    }
    
    private void drawBarChart(Graphics2D g2d, int startX, int startY, int width, int height, String xColumn, String yColumn) {
        // Simple implementation - group data by X column and show counts or sums
        Map<String, Double> dataMap = new HashMap<>();
        
        int xColIndex = -1, yColIndex = -1;
        for (int i = 0; i < currentData.getColumnCount(); i++) {
            if (currentData.getColumnName(i).equals(xColumn)) {
                xColIndex = i;
            }
            if (yColumn != null && currentData.getColumnName(i).equals(yColumn)) {
                yColIndex = i;
            }
        }
        
        if (xColIndex == -1) return;
        
        // Aggregate data
        for (int row = 0; row < currentData.getRowCount(); row++) {
            Object xValue = currentData.getValueAt(row, xColIndex);
            if (xValue == null) continue;
            
            String key = xValue.toString();
            double value = 1; // Default to count
            
            if (yColIndex != -1) {
                Object yValue = currentData.getValueAt(row, yColIndex);
                if (yValue != null) {
                    try {
                        value = Double.parseDouble(yValue.toString());
                    } catch (NumberFormatException e) {
                        value = 1; // Fall back to count
                    }
                }
            }
            
            dataMap.put(key, dataMap.getOrDefault(key, 0.0) + value);
        }
        
        if (dataMap.isEmpty()) return;
        
        // Draw bars
        int barCount = Math.min(dataMap.size(), 10); // Limit to 10 bars
        int barWidth = width / barCount - 10;
        double maxValue = dataMap.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
        
        g2d.setColor(new Color(70, 130, 180));
        int x = startX + 5;
        
        for (Map.Entry<String, Double> entry : dataMap.entrySet()) {
            if (x > startX + width) break;
            
            double value = entry.getValue();
            int barHeight = (int) ((value / maxValue) * height * 0.8);
            
            // Draw bar
            g2d.fillRect(x, startY + height - barHeight, barWidth, barHeight);
            
            // Draw value label
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            String valueLabel = String.format("%.1f", value);
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(valueLabel);
            g2d.drawString(valueLabel, x + (barWidth - labelWidth) / 2, startY + height - barHeight - 5);
            
            // Draw key label
            String keyLabel = entry.getKey();
            if (keyLabel.length() > 8) {
                keyLabel = keyLabel.substring(0, 8) + "...";
            }
            int keyWidth = fm.stringWidth(keyLabel);
            g2d.drawString(keyLabel, x + (barWidth - keyWidth) / 2, startY + height + 15);
            
            g2d.setColor(new Color(70, 130, 180));
            x += barWidth + 10;
        }
    }
    
    private void validateData() {
        if (currentData == null) {
            validationResults.setText("No data available for validation.");
            return;
        }
        
        StringBuilder results = new StringBuilder();
        results.append("=== DATA VALIDATION RESULTS ===\n\n");
        
        int totalRows = currentData.getRowCount();
        int totalCols = currentData.getColumnCount();
        
        results.append(String.format("Dataset: %d rows × %d columns\n\n", totalRows, totalCols));
        
        // Check for completeness
        int totalCells = totalRows * totalCols;
        int emptyCells = 0;
        
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < totalCols; col++) {
                Object value = currentData.getValueAt(row, col);
                if (value == null || value.toString().trim().isEmpty()) {
                    emptyCells++;
                }
            }
        }
        
        double completeness = ((totalCells - emptyCells) * 100.0) / totalCells;
        results.append(String.format("Data Completeness: %.1f%%\n", completeness));
        results.append(String.format("Empty/Null cells: %d out of %d\n\n", emptyCells, totalCells));
        
        // Data quality assessment
        if (completeness >= 95) {
            results.append("✓ EXCELLENT: Data completeness is excellent (≥95%)\n");
        } else if (completeness >= 80) {
            results.append("⚠ GOOD: Data completeness is good (≥80%)\n");
        } else if (completeness >= 60) {
            results.append("⚠ FAIR: Data completeness needs attention (≥60%)\n");
        } else {
            results.append("✗ POOR: Data completeness is poor (<60%)\n");
        }
        
        results.append("\n=== COLUMN-LEVEL ISSUES ===\n");
        
        for (int col = 0; col < totalCols; col++) {
            String columnName = currentData.getColumnName(col);
            int columnNulls = 0;
            
            for (int row = 0; row < totalRows; row++) {
                Object value = currentData.getValueAt(row, col);
                if (value == null || value.toString().trim().isEmpty()) {
                    columnNulls++;
                }
            }
            
            double columnCompleteness = ((totalRows - columnNulls) * 100.0) / totalRows;
            
            if (columnCompleteness < 90) {
                results.append(String.format("⚠ %s: %.1f%% complete (%d nulls)\n", 
                    columnName, columnCompleteness, columnNulls));
            }
        }
        
        validationResults.setText(results.toString());
    }
    
    private void checkDuplicates() {
        if (currentData == null) return;
        
        StringBuilder results = new StringBuilder();
        results.append("=== DUPLICATE ROW ANALYSIS ===\n\n");
        
        Set<String> seenRows = new HashSet<>();
        Set<String> duplicateRows = new HashSet<>();
        
        for (int row = 0; row < currentData.getRowCount(); row++) {
            StringBuilder rowData = new StringBuilder();
            for (int col = 0; col < currentData.getColumnCount(); col++) {
                Object value = currentData.getValueAt(row, col);
                rowData.append(value != null ? value.toString() : "NULL").append("|");
            }
            
            String rowString = rowData.toString();
            if (seenRows.contains(rowString)) {
                duplicateRows.add(rowString);
            } else {
                seenRows.add(rowString);
            }
        }
        
        results.append(String.format("Total rows: %d\n", currentData.getRowCount()));
        results.append(String.format("Unique rows: %d\n", seenRows.size()));
        results.append(String.format("Duplicate rows: %d\n\n", duplicateRows.size()));
        
        if (duplicateRows.isEmpty()) {
            results.append("✓ No duplicate rows found.\n");
        } else {
            results.append("⚠ Duplicate rows detected. Consider data deduplication.\n");
        }
        
        validationResults.setText(results.toString());
    }
    
    private void checkNulls() {
        if (currentData == null) return;
        
        StringBuilder results = new StringBuilder();
        results.append("=== NULL VALUE ANALYSIS ===\n\n");
        
        for (int col = 0; col < currentData.getColumnCount(); col++) {
            String columnName = currentData.getColumnName(col);
            int nullCount = 0;
            
            for (int row = 0; row < currentData.getRowCount(); row++) {
                Object value = currentData.getValueAt(row, col);
                if (value == null || value.toString().trim().isEmpty()) {
                    nullCount++;
                }
            }
            
            double nullPercent = (nullCount * 100.0) / currentData.getRowCount();
            results.append(String.format("%s: %d nulls (%.1f%%)", columnName, nullCount, nullPercent));
            
            if (nullPercent > 50) {
                results.append(" ⚠ HIGH");
            } else if (nullPercent > 10) {
                results.append(" ⚠ MEDIUM");
            } else if (nullPercent > 0) {
                results.append(" ✓ LOW");
            } else {
                results.append(" ✓ NONE");
            }
            
            results.append("\n");
        }
        
        validationResults.setText(results.toString());
    }
    
    private Icon createIcon(String emoji) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(emoji, x, y + 12);
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 16; }
            
            @Override
            public int getIconHeight() { return 16; }
        };
    }
}
