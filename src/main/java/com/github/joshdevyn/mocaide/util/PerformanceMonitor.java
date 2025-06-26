package com.github.joshdevyn.mocaide.util;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance monitoring utility for tracking execution times,
 * memory usage, and system performance metrics
 */
public class PerformanceMonitor {
    
    private static final ConcurrentHashMap<String, ExecutionMetrics> metrics = new ConcurrentHashMap<>();
    private static final List<PerformanceListener> listeners = new ArrayList<>();
    private static final AtomicLong totalCommands = new AtomicLong(0);
    private static final AtomicLong totalTime = new AtomicLong(0);
    
    /**
     * Interface for performance event listeners
     */
    public interface PerformanceListener {
        void onExecutionComplete(String command, ExecutionMetrics metrics);
        void onMemoryUpdate(MemoryMetrics memory);
    }
    
    /**
     * Execution metrics for a command
     */
    public static class ExecutionMetrics {
        private final String command;
        private final long startTime;
        private final long endTime;
        private final long memoryBefore;
        private final long memoryAfter;
        private final boolean success;
        private final String error;
        private final int resultCount;
        
        public ExecutionMetrics(String command, long startTime, long endTime, 
                               long memoryBefore, long memoryAfter, boolean success, 
                               String error, int resultCount) {
            this.command = command;
            this.startTime = startTime;
            this.endTime = endTime;
            this.memoryBefore = memoryBefore;
            this.memoryAfter = memoryAfter;
            this.success = success;
            this.error = error;
            this.resultCount = resultCount;
        }
        
        public double getExecutionTimeSeconds() {
            return (endTime - startTime) / 1000.0;
        }
        
        public long getMemoryUsed() {
            return memoryAfter - memoryBefore;
        }
        
        public String getFormattedTime() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
        
        // Getters
        public String getCommand() { return command; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public long getMemoryBefore() { return memoryBefore; }
        public long getMemoryAfter() { return memoryAfter; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public int getResultCount() { return resultCount; }
    }
    
    /**
     * Memory metrics
     */
    public static class MemoryMetrics {
        private final long totalMemory;
        private final long freeMemory;
        private final long usedMemory;
        private final long maxMemory;
        
        public MemoryMetrics() {
            Runtime runtime = Runtime.getRuntime();
            this.totalMemory = runtime.totalMemory();
            this.freeMemory = runtime.freeMemory();
            this.usedMemory = totalMemory - freeMemory;
            this.maxMemory = runtime.maxMemory();
        }
        
        public double getUsedMemoryMB() {
            return usedMemory / (1024.0 * 1024.0);
        }
        
        public double getTotalMemoryMB() {
            return totalMemory / (1024.0 * 1024.0);
        }
        
        public double getMaxMemoryMB() {
            return maxMemory / (1024.0 * 1024.0);
        }
        
        public double getMemoryUsagePercent() {
            return (usedMemory * 100.0) / maxMemory;
        }
        
        public String getFormattedUsage() {
            return String.format("%.1f MB / %.1f MB (%.1f%%)", 
                getUsedMemoryMB(), getMaxMemoryMB(), getMemoryUsagePercent());
        }
        
        // Getters
        public long getTotalMemory() { return totalMemory; }
        public long getFreeMemory() { return freeMemory; }
        public long getUsedMemory() { return usedMemory; }
        public long getMaxMemory() { return maxMemory; }
    }
    
    /**
     * Performance tracker for timing operations
     */
    public static class PerformanceTracker {
        private final String operation;
        private final long startTime;
        private final long startMemory;
        
        public PerformanceTracker(String operation) {
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
            this.startMemory = getCurrentMemoryUsage();
        }
        
        public ExecutionMetrics finish(boolean success, String error, int resultCount) {
            long endTime = System.currentTimeMillis();
            long endMemory = getCurrentMemoryUsage();
            
            ExecutionMetrics metrics = new ExecutionMetrics(
                operation, startTime, endTime, startMemory, endMemory, 
                success, error, resultCount);
            
            // Record metrics
            recordExecution(metrics);
            
            return metrics;
        }
        
        public ExecutionMetrics finish(boolean success, int resultCount) {
            return finish(success, null, resultCount);
        }
    }
    
    /**
     * Start tracking a new operation
     */
    public static PerformanceTracker startTracking(String operation) {
        return new PerformanceTracker(operation);
    }
    
    /**
     * Record execution metrics
     */
    private static void recordExecution(ExecutionMetrics metrics) {
        totalCommands.incrementAndGet();
        totalTime.addAndGet((long) (metrics.getExecutionTimeSeconds() * 1000));
        
        // Store metrics for this command type
        String commandType = extractCommandType(metrics.getCommand());
        PerformanceMonitor.metrics.merge(commandType, metrics, (existing, newMetrics) -> {
            // Keep the latest metrics for simplicity
            return newMetrics;
        });
        
        // Notify listeners
        notifyListeners(metrics);
    }
    
    /**
     * Extract command type from full command text
     */
    private static String extractCommandType(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "Unknown";
        }
        
        String trimmed = command.trim().toLowerCase();
        String[] words = trimmed.split("\\s+");
        
        if (words.length > 0) {
            String firstWord = words[0];
            // Common MOCA command patterns
            if (firstWord.equals("list") || firstWord.equals("select")) {
                return firstWord.toUpperCase();
            } else if (firstWord.equals("publish")) {
                return "PUBLISH";
            } else if (firstWord.equals("execute")) {
                return "EXECUTE";
            } else if (firstWord.equals("get")) {
                return "GET";
            }
            return firstWord.toUpperCase();
        }
        
        return "Unknown";
    }
    
    /**
     * Get current memory usage
     */
    private static long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    /**
     * Get current memory metrics
     */
    public static MemoryMetrics getMemoryMetrics() {
        return new MemoryMetrics();
    }
    
    /**
     * Get performance statistics
     */
    public static String getPerformanceStats() {
        long commands = totalCommands.get();
        long time = totalTime.get();
        
        if (commands == 0) {
            return "No commands executed yet";
        }
        
        double avgTime = time / (double) commands / 1000.0;
        MemoryMetrics memory = getMemoryMetrics();
        
        return String.format(
            "Commands: %d | Avg Time: %.2fs | Memory: %s",
            commands, avgTime, memory.getFormattedUsage()
        );
    }
    
    /**
     * Get detailed statistics for all command types
     */
    public static String getDetailedStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Performance Statistics ===\n");
        sb.append(String.format("Total Commands: %d\n", totalCommands.get()));
        sb.append(String.format("Total Time: %.2fs\n", totalTime.get() / 1000.0));
        
        if (totalCommands.get() > 0) {
            sb.append(String.format("Average Time: %.2fs\n", 
                (totalTime.get() / (double) totalCommands.get()) / 1000.0));
        }
        
        sb.append("\n=== Memory Usage ===\n");
        MemoryMetrics memory = getMemoryMetrics();
        sb.append(String.format("Used: %.1f MB\n", memory.getUsedMemoryMB()));
        sb.append(String.format("Total: %.1f MB\n", memory.getTotalMemoryMB()));
        sb.append(String.format("Max: %.1f MB\n", memory.getMaxMemoryMB()));
        sb.append(String.format("Usage: %.1f%%\n", memory.getMemoryUsagePercent()));
        
        if (!metrics.isEmpty()) {
            sb.append("\n=== Command Types ===\n");
            metrics.forEach((type, metric) -> {
                sb.append(String.format("%s: %.2fs (%s)\n", 
                    type, metric.getExecutionTimeSeconds(),
                    metric.isSuccess() ? "Success" : "Failed"));
            });
        }
        
        return sb.toString();
    }
    
    /**
     * Add a performance listener
     */
    public static void addListener(PerformanceListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a performance listener
     */
    public static void removeListener(PerformanceListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners of execution completion
     */
    private static void notifyListeners(ExecutionMetrics metrics) {
        SwingUtilities.invokeLater(() -> {
            for (PerformanceListener listener : listeners) {
                try {
                    listener.onExecutionComplete(metrics.getCommand(), metrics);
                } catch (Exception e) {
                    System.err.println("Error notifying performance listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Notify listeners of memory updates
     */
    public static void notifyMemoryUpdate() {
        MemoryMetrics memory = getMemoryMetrics();
        SwingUtilities.invokeLater(() -> {
            for (PerformanceListener listener : listeners) {
                try {
                    listener.onMemoryUpdate(memory);
                } catch (Exception e) {
                    System.err.println("Error notifying memory listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Clear all performance data
     */
    public static void clearStats() {
        metrics.clear();
        totalCommands.set(0);
        totalTime.set(0);
    }
    
    /**
     * Force garbage collection and update memory metrics
     */
    public static void forceGarbageCollection() {
        System.gc();
        notifyMemoryUpdate();
    }
    
    /**
     * Create a performance monitoring panel
     */
    public static JPanel createMonitoringPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Performance Monitor"));
        
        JTextArea statsArea = new JTextArea(10, 40);
        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(statsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear Stats");
        JButton gcButton = new JButton("Force GC");
        
        refreshButton.addActionListener(e -> {
            statsArea.setText(getDetailedStats());
        });
        
        clearButton.addActionListener(e -> {
            clearStats();
            statsArea.setText("Statistics cleared.");
        });
        
        gcButton.addActionListener(e -> {
            forceGarbageCollection();
            statsArea.setText(getDetailedStats());
        });
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(gcButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Initial content
        statsArea.setText(getDetailedStats());
        
        return panel;
    }
}
