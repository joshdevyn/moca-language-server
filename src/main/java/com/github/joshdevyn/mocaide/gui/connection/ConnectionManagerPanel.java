package com.github.joshdevyn.mocaide.gui.connection;

import com.github.joshdevyn.mocaide.connection.ConnectionManager;
import com.github.joshdevyn.mocaide.connection.ConnectionProfile;
import com.github.mrglassdanny.mocalanguageserver.moca.connection.MocaConnection;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Enterprise-grade connection management panel with profiles, import/export, and favorites
 */
public class ConnectionManagerPanel extends JPanel {
    
    private ConnectionManager connectionManager;
    private JTable connectionsTable;
    private DefaultTableModel tableModel;
    private JButton connectButton, disconnectButton, newButton, editButton, deleteButton, duplicateButton;
    private JButton importButton, exportButton, favoriteButton;
    private JLabel statusLabel;
    private JTextField searchField;
    private JLabel connectionStatusIndicator;
    
    // Connection details panel
    private ConnectionDetailsPanel detailsPanel;
    
    // Recent connections quick access
    private JList<ConnectionProfile> recentConnectionsList;
    private DefaultListModel<ConnectionProfile> recentListModel;
    
    // Active connection state
    private MocaConnection activeConnection;
    private ConnectionProfile activeProfile;
    
    // Connection listeners
    private java.util.List<ConnectionChangeListener> connectionListeners = new java.util.ArrayList<>();
    
    public ConnectionManagerPanel() {
        this.connectionManager = new ConnectionManager();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refreshConnectionsList();
        refreshRecentConnections();
    }
    
    private void initializeComponents() {
        // Connections table
        String[] columnNames = {"Name", "URL", "User", "Environment", "Last Used", "Favorite"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Boolean.class; // Favorite column
                return String.class;
            }
        };
        
        connectionsTable = new JTable(tableModel);
        connectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connectionsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        connectionsTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Name
        connectionsTable.getColumnModel().getColumn(1).setPreferredWidth(250); // URL
        connectionsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // User
        connectionsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Environment
        connectionsTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Last Used
        connectionsTable.getColumnModel().getColumn(5).setPreferredWidth(60);  // Favorite
        
        // Buttons
        connectButton = new JButton("Connect");
        connectButton.setFont(connectButton.getFont().deriveFont(Font.BOLD));
        connectButton.setEnabled(false);
        
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        
        newButton = new JButton("New");
        editButton = new JButton("Edit");
        editButton.setEnabled(false);
        
        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);
        
        duplicateButton = new JButton("Duplicate");
        duplicateButton.setEnabled(false);
        
        importButton = new JButton("Import...");
        exportButton = new JButton("Export...");
        
        favoriteButton = new JButton("Toggle Favorite");
        favoriteButton.setEnabled(false);
        
        // Search field
        searchField = new JTextField(20);
        searchField.setToolTipText("Search connections by name, URL, or user");
        
        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
        
        // Connection details panel
        detailsPanel = new ConnectionDetailsPanel();
        
        // Recent connections list
        recentListModel = new DefaultListModel<>();
        recentConnectionsList = new JList<>(recentListModel);
        recentConnectionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentConnectionsList.setCellRenderer(new ConnectionListCellRenderer());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Connection Manager"));
        
        // Left panel - connections list and controls
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(600, 0));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Connections table
        JScrollPane tableScrollPane = new JScrollPane(connectionsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Saved Connections"));
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.add(connectButton);
        buttonsPanel.add(disconnectButton);
        buttonsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonsPanel.add(newButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(duplicateButton);
        buttonsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonsPanel.add(favoriteButton);
        buttonsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonsPanel.add(importButton);
        buttonsPanel.add(exportButton);
        
        leftPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Right panel - recent connections and details
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 0));
        
        // Recent connections
        JScrollPane recentScrollPane = new JScrollPane(recentConnectionsList);
        recentScrollPane.setBorder(BorderFactory.createTitledBorder("Recent Connections"));
        recentScrollPane.setPreferredSize(new Dimension(0, 150));
        rightPanel.add(recentScrollPane, BorderLayout.NORTH);
        
        // Connection details
        rightPanel.add(detailsPanel, BorderLayout.CENTER);
        
        // Main layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.65);
        add(splitPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        
        // Add connection status indicator
        connectionStatusIndicator = new JLabel("●");
        connectionStatusIndicator.setForeground(Color.RED);
        connectionStatusIndicator.setToolTipText("Connection Status: Disconnected");
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(connectionStatusIndicator);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Table selection
        connectionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
                updateConnectionDetails();
            }
        });
        
        // Double-click to connect
        connectionsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    connectToSelected();
                }
            }
        });
        
        // Recent list selection
        recentConnectionsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ConnectionProfile selected = recentConnectionsList.getSelectedValue();
                if (selected != null) {
                    selectConnectionInTable(selected);
                }
            }
        });
        
        // Double-click recent to connect
        recentConnectionsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ConnectionProfile selected = recentConnectionsList.getSelectedValue();
                    if (selected != null) {
                        connectToConnection(selected);
                    }
                }
            }
        });
        
        // Button actions
        connectButton.addActionListener(e -> connectToSelected());
        disconnectButton.addActionListener(e -> disconnectFromServer());
        newButton.addActionListener(e -> showNewConnectionDialog());
        editButton.addActionListener(e -> showEditConnectionDialog());
        deleteButton.addActionListener(e -> deleteSelectedConnection());
        duplicateButton.addActionListener(e -> duplicateSelectedConnection());
        favoriteButton.addActionListener(e -> toggleFavorite());
        importButton.addActionListener(e -> importConnections());
        exportButton.addActionListener(e -> exportConnections());
        
        // Search
        searchField.addActionListener(e -> filterConnections());
    }
    
    private void refreshConnectionsList() {
        tableModel.setRowCount(0);
        List<ConnectionProfile> connections = connectionManager.getAllConnections();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (ConnectionProfile conn : connections) {
            Object[] row = {
                conn.getName(),
                conn.getUrl(),
                conn.getUserId(),
                conn.getEnvironment() != null ? conn.getEnvironment() : "",
                conn.getLastUsed() != null ? conn.getLastUsed().format(formatter) : "Never",
                conn.isFavorite()
            };
            tableModel.addRow(row);
        }
        
        updateStatus();
    }
    
    private void refreshRecentConnections() {
        recentListModel.clear();
        List<ConnectionProfile> recent = connectionManager.getRecentConnections(5);
        for (ConnectionProfile conn : recent) {
            recentListModel.addElement(conn);
        }
    }
    
    private void updateButtonStates() {
        boolean hasSelection = connectionsTable.getSelectedRow() >= 0;
        boolean hasConnection = activeConnection != null;
        
        connectButton.setEnabled(hasSelection && !hasConnection);
        disconnectButton.setEnabled(hasConnection);
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        duplicateButton.setEnabled(hasSelection);
        favoriteButton.setEnabled(hasSelection);
    }
    
    private void updateConnectionDetails() {
        int selectedRow = connectionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<ConnectionProfile> connections = connectionManager.getAllConnections();
            if (selectedRow < connections.size()) {
                ConnectionProfile conn = connections.get(selectedRow);
                detailsPanel.displayConnection(conn);
            }
        } else {
            detailsPanel.clearDisplay();
        }
    }
    
    private void selectConnectionInTable(ConnectionProfile connection) {
        List<ConnectionProfile> connections = connectionManager.getAllConnections();
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i).getId().equals(connection.getId())) {
                connectionsTable.setRowSelectionInterval(i, i);
                break;
            }
        }
    }
    
    private void connectToSelected() {
        int selectedRow = connectionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<ConnectionProfile> connections = connectionManager.getAllConnections();
            ConnectionProfile conn = connections.get(selectedRow);
            connectToConnection(conn);
        }
    }
    
    private void connectToConnection(ConnectionProfile profile) {
        statusLabel.setText("Connecting to " + profile.getName() + "...");
        
        SwingWorker<MocaConnection, Void> worker = new SwingWorker<MocaConnection, Void>() {
            @Override
            protected MocaConnection doInBackground() throws Exception {
                try {
                    MocaConnection mocaConnection = new MocaConnection();
                    mocaConnection.connect(profile.getUrl(), profile.getUserId(), 
                                         profile.getPassword(), profile.isApproveUnsafeScripts());
                    
                    // Update last used time
                    connectionManager.setActiveConnection(profile);
                    
                    return mocaConnection;
                } catch (Exception e) {
                    throw e;
                }
            }
            
            @Override
            protected void done() {
                try {
                    MocaConnection connection = get();
                    if (connection != null) {
                        // Store active connection
                        activeConnection = connection;
                        activeProfile = profile;
                        
                        statusLabel.setText("Connected to " + profile.getName());
                        refreshConnectionsList();
                        refreshRecentConnections();
                        
                        // Update status indicator
                        updateConnectionStatus(true, profile.getName());
                        
                        // Enable disconnect button, disable connect button when connected
                        connectButton.setEnabled(false);
                        disconnectButton.setEnabled(true);
                        
                        // Notify listeners
                        fireConnectionChanged(profile, connection);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Connection error: " + e.getMessage());
                    updateConnectionStatus(false, null);
                    JOptionPane.showMessageDialog(ConnectionManagerPanel.this,
                        "Error connecting: " + e.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void disconnectFromServer() {
        if (activeConnection != null) {
            // Clear active connection state
            activeConnection = null;
            activeProfile = null;
            
            // Update UI
            statusLabel.setText("Disconnected");
            updateConnectionStatus(false, null);
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            
            // Notify listeners
            fireConnectionDisconnected();
        }
    }
    
    private void showNewConnectionDialog() {
        ConnectionEditDialog dialog = new ConnectionEditDialog(
            SwingUtilities.getWindowAncestor(this), null, connectionManager);
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            refreshConnectionsList();
            refreshRecentConnections();
        }
    }
    
    private void showEditConnectionDialog() {
        int selectedRow = connectionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<ConnectionProfile> connections = connectionManager.getAllConnections();
            ConnectionProfile conn = connections.get(selectedRow);
            
            ConnectionEditDialog dialog = new ConnectionEditDialog(
                SwingUtilities.getWindowAncestor(this), conn, connectionManager);
            dialog.setVisible(true);
            
            if (dialog.isSaved()) {
                refreshConnectionsList();
                refreshRecentConnections();
                updateConnectionDetails();
            }
        }
    }
    
    private void deleteSelectedConnection() {
        int selectedRow = connectionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<ConnectionProfile> connections = connectionManager.getAllConnections();
            ConnectionProfile conn = connections.get(selectedRow);
            
            int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the connection '" + conn.getName() + "'?",
                "Delete Connection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                connectionManager.deleteConnection(conn.getId());
                refreshConnectionsList();
                refreshRecentConnections();
                detailsPanel.clearDisplay();
            }
        }
    }
    
    private void duplicateSelectedConnection() {
        int selectedRow = connectionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<ConnectionProfile> connections = connectionManager.getAllConnections();
            ConnectionProfile original = connections.get(selectedRow);
            
            ConnectionProfile duplicate = new ConnectionProfile(
                original.getName() + " (Copy)",
                original.getUrl(),
                original.getUserId(),
                original.getPassword()
            );
            duplicate.setApproveUnsafeScripts(original.isApproveUnsafeScripts());
            duplicate.setEnvironment(original.getEnvironment());
            duplicate.setDescription(original.getDescription());
            duplicate.setTimeoutSeconds(original.getTimeoutSeconds());
            
            connectionManager.addConnection(duplicate);
            refreshConnectionsList();
        }
    }
    
    private void toggleFavorite() {
        int selectedRow = connectionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<ConnectionProfile> connections = connectionManager.getAllConnections();
            ConnectionProfile conn = connections.get(selectedRow);
            
            conn.setFavorite(!conn.isFavorite());
            connectionManager.updateConnection(conn);
            refreshConnectionsList();
            updateConnectionDetails();
        }
    }
    
    private void importConnections() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                connectionManager.importConnections(file, true);
                refreshConnectionsList();
                refreshRecentConnections();
                
                JOptionPane.showMessageDialog(this,
                    "Import completed successfully",
                    "Import Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error importing connections: " + e.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportConnections() {
        if (connectionManager.getAllConnections().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No connections to export",
                "Export",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        fileChooser.setSelectedFile(new File("moca-connections-export.json"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                connectionManager.exportConnections(file);
                
                JOptionPane.showMessageDialog(this,
                    "Successfully exported " + connectionManager.getAllConnections().size() + " connections",
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting connections: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void filterConnections() {
        // Simple filtering - would be enhanced with more sophisticated search
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            refreshConnectionsList();
        } else {
            tableModel.setRowCount(0);
            List<ConnectionProfile> connections = connectionManager.getAllConnections();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            for (ConnectionProfile conn : connections) {
                if (conn.getName().toLowerCase().contains(searchText) ||
                    conn.getUrl().toLowerCase().contains(searchText) ||
                    conn.getUserId().toLowerCase().contains(searchText)) {
                    
                    Object[] row = {
                        conn.getName(),
                        conn.getUrl(),
                        conn.getUserId(),
                        conn.getEnvironment() != null ? conn.getEnvironment() : "",
                        conn.getLastUsed() != null ? conn.getLastUsed().format(formatter) : "Never",
                        conn.isFavorite()
                    };
                    tableModel.addRow(row);
                }
            }
        }
        updateStatus();
    }
    
    private void updateStatus() {
        int totalConnections = connectionManager.getAllConnections().size();
        int displayedConnections = tableModel.getRowCount();
        
        if (displayedConnections == totalConnections) {
            statusLabel.setText(totalConnections + " connections");
        } else {
            statusLabel.setText(displayedConnections + " of " + totalConnections + " connections shown");
        }
    }
    
    // Connection event handling
    private void fireConnectionChanged(ConnectionProfile profile, MocaConnection connection) {
        for (ConnectionChangeListener listener : connectionListeners) {
            listener.onConnectionChanged(profile, connection);
        }
    }
    
    private void fireConnectionDisconnected() {
        for (ConnectionChangeListener listener : connectionListeners) {
            listener.onConnectionDisconnected();
        }
    }
    
    public void addConnectionChangeListener(ConnectionChangeListener listener) {
        connectionListeners.add(listener);
    }
    
    public void removeConnectionChangeListener(ConnectionChangeListener listener) {
        connectionListeners.remove(listener);
    }
    
    public MocaConnection getActiveConnection() {
        return activeConnection;
    }
    
    public ConnectionProfile getActiveProfile() {
        return activeProfile;
    }
    
    /**
     * Update the connection status indicator
     */
    public void updateConnectionStatus(boolean connected, String connectionName) {
        if (connected) {
            connectionStatusIndicator.setForeground(new Color(0, 153, 51));
            connectionStatusIndicator.setToolTipText("Connection Status: Connected to " + connectionName);
        } else {
            connectionStatusIndicator.setForeground(Color.RED);
            connectionStatusIndicator.setToolTipText("Connection Status: Disconnected");
        }
    }
    
    /**
     * Custom cell renderer for connection list
     */
    private static class ConnectionListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof ConnectionProfile) {
                ConnectionProfile conn = (ConnectionProfile) value;
                setText(conn.getName());
                setToolTipText(conn.getUrl() + " (" + conn.getUserId() + ")");
                
                if (conn.isFavorite()) {
                    setText("★ " + conn.getName());
                }
            }
            
            return this;
        }
    }
    
    /**
     * Interface for listening to connection changes
     */
    public interface ConnectionChangeListener {
        void onConnectionChanged(ConnectionProfile profile, MocaConnection connection);
        void onConnectionDisconnected();
    }
}
