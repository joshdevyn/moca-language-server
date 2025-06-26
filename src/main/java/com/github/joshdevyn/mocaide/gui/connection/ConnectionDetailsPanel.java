package com.github.joshdevyn.mocaide.gui.connection;

import com.github.joshdevyn.mocaide.connection.ConnectionProfile;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Panel that displays detailed information about a selected connection profile
 */
public class ConnectionDetailsPanel extends JPanel {
    
    private JLabel nameLabel, urlLabel, userLabel, environmentLabel;
    private JLabel createdLabel, lastUsedLabel, favoriteLabel;
    private JTextArea descriptionArea;
    
    public ConnectionDetailsPanel() {
        initializeComponents();
        setupLayout();
        clearDisplay();
    }
    
    private void initializeComponents() {
        setBorder(BorderFactory.createTitledBorder("Connection Details"));
        
        nameLabel = new JLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));
        
        urlLabel = new JLabel();
        userLabel = new JLabel();
        environmentLabel = new JLabel();
        createdLabel = new JLabel();
        lastUsedLabel = new JLabel();
        favoriteLabel = new JLabel();
        
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setEditable(false);
        descriptionArea.setBackground(getBackground());
        descriptionArea.setBorder(BorderFactory.createEtchedBorder());
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(nameLabel, gbc);
        
        // URL
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("URL:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(urlLabel, gbc);
        
        // User
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("User:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(userLabel, gbc);
        
        // Environment
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Environment:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(environmentLabel, gbc);
        
        // Created
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Created:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(createdLabel, gbc);
        
        // Last Used
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(new JLabel("Last Used:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(lastUsedLabel, gbc);
        
        // Favorite
        gbc.gridx = 0; gbc.gridy = 6;
        mainPanel.add(new JLabel("Favorite:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(favoriteLabel, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBorder(BorderFactory.createTitledBorder("Description"));
        descPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        mainPanel.add(descPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    public void displayConnection(ConnectionProfile connection) {
        if (connection == null) {
            clearDisplay();
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        nameLabel.setText(connection.getName());
        urlLabel.setText(connection.getUrl());
        userLabel.setText(connection.getUserId());
        environmentLabel.setText(connection.getEnvironment() != null ? connection.getEnvironment() : "Not specified");
        createdLabel.setText(connection.getCreated() != null ? connection.getCreated().format(formatter) : "Unknown");
        lastUsedLabel.setText(connection.getLastUsed() != null ? connection.getLastUsed().format(formatter) : "Never");
        favoriteLabel.setText(connection.isFavorite() ? "★ Yes" : "No");
        descriptionArea.setText(connection.getDescription() != null ? connection.getDescription() : "");
    }
    
    public void clearDisplay() {
        nameLabel.setText("No connection selected");
        urlLabel.setText("");
        userLabel.setText("");
        environmentLabel.setText("");
        createdLabel.setText("");
        lastUsedLabel.setText("");
        favoriteLabel.setText("");
        descriptionArea.setText("");
    }
}
