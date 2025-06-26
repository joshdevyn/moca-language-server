package com.github.joshdevyn.mocaide.connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages MOCA connection profiles with persistence, CRUD operations, and import/export
 */
public class ConnectionManager {
    
    private static final String CONNECTIONS_FILE = "moca-connections.json";
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String CONFIG_DIR = ".moca-gui";
    
    private List<ConnectionProfile> connections;
    private List<ConnectionManagerListener> listeners;
    private ConnectionProfile activeConnection;
    
    public ConnectionManager() {
        this.connections = new ArrayList<>();
        this.listeners = new ArrayList<>();
        loadConnections();
    }
    
    /**
     * Get all connection profiles
     */
    public List<ConnectionProfile> getAllConnections() {
        return new ArrayList<>(connections);
    }
    
    /**
     * Get connections sorted by last used (most recent first)
     */
    public List<ConnectionProfile> getRecentConnections(int limit) {
        return connections.stream()
                .filter(conn -> conn.getLastUsed() != null)
                .sorted((a, b) -> b.getLastUsed().compareTo(a.getLastUsed()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get favorite connections
     */
    public List<ConnectionProfile> getFavoriteConnections() {
        return connections.stream()
                .filter(ConnectionProfile::isFavorite)
                .sorted(Comparator.comparing(ConnectionProfile::getName))
                .collect(Collectors.toList());
    }
    
    /**
     * Add a new connection profile
     */
    public void addConnection(ConnectionProfile profile) {
        if (profile != null && profile.isValid()) {
            connections.add(profile);
            saveConnections();
            notifyListeners();
        }
    }
    
    /**
     * Update an existing connection profile
     */
    public void updateConnection(ConnectionProfile profile) {
        if (profile != null && profile.isValid()) {
            for (int i = 0; i < connections.size(); i++) {
                if (connections.get(i).getId().equals(profile.getId())) {
                    connections.set(i, profile);
                    saveConnections();
                    notifyListeners();
                    return;
                }
            }
        }
    }
    
    /**
     * Delete a connection profile
     */
    public void deleteConnection(String connectionId) {
        connections.removeIf(conn -> conn.getId().equals(connectionId));
        saveConnections();
        notifyListeners();
    }
    
    /**
     * Get connection by ID
     */
    public ConnectionProfile getConnection(String connectionId) {
        return connections.stream()
                .filter(conn -> conn.getId().equals(connectionId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Search connections by name or URL
     */
    public List<ConnectionProfile> searchConnections(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllConnections();
        }
        
        String lowerQuery = query.toLowerCase();
        return connections.stream()
                .filter(conn -> 
                    conn.getName().toLowerCase().contains(lowerQuery) ||
                    conn.getUrl().toLowerCase().contains(lowerQuery) ||
                    (conn.getDescription() != null && conn.getDescription().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
    }
    
    /**
     * Set active connection and update last used timestamp
     */
    public void setActiveConnection(ConnectionProfile connection) {
        this.activeConnection = connection;
        if (connection != null) {
            connection.updateLastUsed();
            updateConnection(connection);
        }
        notifyListeners();
    }
    
    /**
     * Get currently active connection
     */
    public ConnectionProfile getActiveConnection() {
        return activeConnection;
    }
    
    /**
     * Export connections to JSON file
     */
    public void exportConnections(File file) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(connections, writer);
        }
    }
    
    /**
     * Import connections from JSON file
     */
    public void importConnections(File file, boolean merge) throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ConnectionProfile>>(){}.getType();
        
        try (FileReader reader = new FileReader(file)) {
            List<ConnectionProfile> importedConnections = gson.fromJson(reader, listType);
            
            if (!merge) {
                connections.clear();
            }
            
            for (ConnectionProfile profile : importedConnections) {
                if (profile.isValid()) {
                    // Generate new ID to avoid conflicts
                    profile.setId(UUID.randomUUID().toString());
                    connections.add(profile);
                }
            }
            
            saveConnections();
            notifyListeners();
        }
    }
    
    /**
     * Load connections from persistent storage
     */
    private void loadConnections() {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try {
                String json = new String(Files.readAllBytes(configPath));
                Gson gson = new Gson();
                Type listType = new TypeToken<List<ConnectionProfile>>(){}.getType();
                List<ConnectionProfile> loaded = gson.fromJson(json, listType);
                if (loaded != null) {
                    connections = loaded;
                }
            } catch (Exception e) {
                System.err.println("Error loading connections: " + e.getMessage());
                connections = new ArrayList<>();
            }
        }
    }
    
    /**
     * Save connections to persistent storage
     */
    private void saveConnections() {
        try {
            Path configDir = Paths.get(USER_HOME, CONFIG_DIR);
            Files.createDirectories(configDir);
            
            Path configPath = getConfigPath();
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            
            String json = gson.toJson(connections);
            Files.write(configPath, json.getBytes());
        } catch (Exception e) {
            System.err.println("Error saving connections: " + e.getMessage());
        }
    }
    
    /**
     * Get the path to the connections configuration file
     */
    private Path getConfigPath() {
        return Paths.get(USER_HOME, CONFIG_DIR, CONNECTIONS_FILE);
    }
    
    /**
     * Add a listener for connection changes
     */
    public void addListener(ConnectionManagerListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a listener
     */
    public void removeListener(ConnectionManagerListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners of changes
     */
    private void notifyListeners() {
        for (ConnectionManagerListener listener : listeners) {
            listener.onConnectionsChanged();
        }
    }
    
    /**
     * Interface for listening to connection changes
     */
    public interface ConnectionManagerListener {
        void onConnectionsChanged();
    }
}
