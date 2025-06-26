package com.github.joshdevyn.mocaide.gui.connection;

import com.github.joshdevyn.mocaide.connection.ConnectionManager;
import com.github.joshdevyn.mocaide.connection.ConnectionProfile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialog for creating new connections or editing existing ones
 */
public class ConnectionEditDialog extends JDialog {
    
    private ConnectionProfile originalProfile;
    private ConnectionManager connectionManager;
    private boolean saved = false;
    
    // Form fields
    private JTextField nameField;
    private JTextField urlField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JTextField environmentField;
    private JTextArea descriptionArea;
    private JCheckBox approveUnsafeScriptsCheckBox;
    private JCheckBox favoriteCheckBox;
    private JSpinner timeoutSpinner;
    
    // Buttons
    private JButton saveButton;
    private JButton cancelButton;
    private JButton testConnectionButton;
    
    public ConnectionEditDialog(Window parent, ConnectionProfile profile, ConnectionManager manager) {
        super(parent, profile == null ? "New Connection" : "Edit Connection", ModalityType.APPLICATION_MODAL);
        this.originalProfile = profile;
        this.connectionManager = manager;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        if (profile != null) {
            populateFields(profile);
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        nameField = new JTextField(20);
        urlField = new JTextField(30);
        userField = new JTextField(15);
        passwordField = new JPasswordField(15);
        environmentField = new JTextField(10);
        
        descriptionArea = new JTextArea(3, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        approveUnsafeScriptsCheckBox = new JCheckBox("Approve Unsafe Scripts");
        favoriteCheckBox = new JCheckBox("Favorite Connection");
        
        timeoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 300, 5));
        
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        testConnectionButton = new JButton("Test Connection");
        
        // Set default button
        getRootPane().setDefaultButton(saveButton);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(nameField, gbc);
        
        // URL
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("URL:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(urlField, gbc);
        
        // User
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(userField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(passwordField, gbc);
        
        // Environment
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Environment:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(environmentField, gbc);
        
        // Timeout
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Timeout (seconds):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(timeoutSpinner, gbc);
        
        // Options
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        formPanel.add(approveUnsafeScriptsCheckBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(favoriteCheckBox, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        buttonPanel.add(testConnectionButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(e -> saveConnection());
        cancelButton.addActionListener(e -> cancelDialog());
        testConnectionButton.addActionListener(e -> testConnection());
        
        // Close dialog on window close
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelDialog();
            }
        });
    }
    
    private void populateFields(ConnectionProfile profile) {
        nameField.setText(profile.getName());
        urlField.setText(profile.getUrl());
        userField.setText(profile.getUserId());
        passwordField.setText(profile.getPassword());
        environmentField.setText(profile.getEnvironment());
        descriptionArea.setText(profile.getDescription());
        approveUnsafeScriptsCheckBox.setSelected(profile.isApproveUnsafeScripts());
        favoriteCheckBox.setSelected(profile.isFavorite());
        timeoutSpinner.setValue(profile.getTimeoutSeconds());
    }
    
    private void saveConnection() {
        // Validate fields
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a connection name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        if (urlField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a URL.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            urlField.requestFocus();
            return;
        }
        
        if (userField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a user ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            userField.requestFocus();
            return;
        }
        
        // Create or update connection profile
        ConnectionProfile profile;
        if (originalProfile == null) {
            // New connection
            profile = new ConnectionProfile();
        } else {
            // Edit existing connection
            profile = originalProfile;
        }
        
        // Update profile with form data
        profile.setName(nameField.getText().trim());
        profile.setUrl(urlField.getText().trim());
        profile.setUserId(userField.getText().trim());
        profile.setPassword(new String(passwordField.getPassword()));
        profile.setEnvironment(environmentField.getText().trim());
        profile.setDescription(descriptionArea.getText().trim());
        profile.setApproveUnsafeScripts(approveUnsafeScriptsCheckBox.isSelected());
        profile.setFavorite(favoriteCheckBox.isSelected());
        profile.setTimeoutSeconds((Integer) timeoutSpinner.getValue());
        
        // Save to manager
        if (originalProfile == null) {
            connectionManager.addConnection(profile);
        } else {
            connectionManager.updateConnection(profile);
        }
        
        saved = true;
        dispose();
    }
    
    private void cancelDialog() {
        saved = false;
        dispose();
    }
    
    private void testConnection() {
        String url = urlField.getText().trim();
        String userId = userField.getText().trim();
        String password = new String(passwordField.getPassword());
        boolean approveUnsafe = approveUnsafeScriptsCheckBox.isSelected();
        
        if (url.isEmpty() || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in URL and User ID before testing.", "Test Connection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        testConnectionButton.setEnabled(false);
        testConnectionButton.setText("Testing...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private String errorMessage;
            
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // Create a temporary connection to test
                    com.github.mrglassdanny.mocalanguageserver.moca.connection.MocaConnection testConnection = 
                        new com.github.mrglassdanny.mocalanguageserver.moca.connection.MocaConnection();
                    testConnection.connect(url, userId, password, approveUnsafe);
                    
                    // Test with a simple command
                    testConnection.executeCommand("list warehouses");
                    
                    return true;
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                testConnectionButton.setEnabled(true);
                testConnectionButton.setText("Test Connection");
                
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(ConnectionEditDialog.this,
                            "Connection test successful!",
                            "Test Connection",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ConnectionEditDialog.this,
                            "Connection test failed:\n" + errorMessage,
                            "Test Connection",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ConnectionEditDialog.this,
                        "Connection test failed:\n" + e.getMessage(),
                        "Test Connection",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    public boolean isSaved() {
        return saved;
    }
}
