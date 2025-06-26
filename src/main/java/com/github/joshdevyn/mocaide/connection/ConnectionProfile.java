package com.github.joshdevyn.mocaide.connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a MOCA server connection profile with all necessary connection details
 */
public class ConnectionProfile {
    
    private String id;
    private String name;
    private String url;
    private String userId;
    private String password; // This should be encrypted in production
    private boolean approveUnsafeScripts;
    private LocalDateTime lastUsed;
    private LocalDateTime created;
    private boolean isFavorite;
    private String description;
    private String environment; // DEV, TEST, PROD, etc.
    private int timeoutSeconds;
    
    // Default constructor
    public ConnectionProfile() {
        this.id = UUID.randomUUID().toString();
        this.created = LocalDateTime.now();
        this.timeoutSeconds = 30;
        this.approveUnsafeScripts = false;
        this.isFavorite = false;
    }
    
    // Constructor with basic details
    public ConnectionProfile(String name, String url, String userId, String password) {
        this();
        this.name = name;
        this.url = url;
        this.userId = userId;
        this.password = password;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public boolean isApproveUnsafeScripts() { return approveUnsafeScripts; }
    public void setApproveUnsafeScripts(boolean approveUnsafeScripts) { this.approveUnsafeScripts = approveUnsafeScripts; }
    
    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
    
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    
    /**
     * Update the last used timestamp to current time
     */
    public void updateLastUsed() {
        this.lastUsed = LocalDateTime.now();
    }
    
    /**
     * Get display name for UI purposes
     */
    public String getDisplayName() {
        StringBuilder display = new StringBuilder(name);
        if (environment != null && !environment.isEmpty()) {
            display.append(" (").append(environment).append(")");
        }
        return display.toString();
    }
    
    /**
     * Create a copy of this profile (useful for editing)
     */
    public ConnectionProfile copy() {
        ConnectionProfile copy = new ConnectionProfile();
        copy.id = this.id;
        copy.name = this.name;
        copy.url = this.url;
        copy.userId = this.userId;
        copy.password = this.password;
        copy.approveUnsafeScripts = this.approveUnsafeScripts;
        copy.lastUsed = this.lastUsed;
        copy.created = this.created;
        copy.isFavorite = this.isFavorite;
        copy.description = this.description;
        copy.environment = this.environment;
        copy.timeoutSeconds = this.timeoutSeconds;
        return copy;
    }
    
    /**
     * Convert to JSON string
     */
    public String toJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(this);
    }
    
    /**
     * Create from JSON string
     */
    public static ConnectionProfile fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ConnectionProfile.class);
    }
    
    /**
     * Validate the connection profile
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               url != null && !url.trim().isEmpty() &&
               userId != null && !userId.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionProfile that = (ConnectionProfile) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}
