package com.github.joshdevyn.mocaide.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Security and audit system for MOCA GUI
 * Provides secure credential storage, audit logging, and session management
 */
public class SecurityManager {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final String KEY_PREF = "encryption_key";
    private static final String AUDIT_LOG_FILE = "moca_gui_audit.log";
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    
    private static SecurityManager instance;
    private Preferences prefs;
    private SecretKey encryptionKey;
    private LocalDateTime lastActivity;
    private List<AuditListener> auditListeners;
    private boolean auditEnabled = true;
    
    /**
     * Audit event listener interface
     */
    public interface AuditListener {
        void onAuditEvent(AuditEvent event);
    }
    
    /**
     * Audit event types
     */
    public enum AuditEventType {
        LOGIN, LOGOUT, CONNECT, DISCONNECT, COMMAND_EXECUTE, 
        FILE_OPEN, FILE_SAVE, EXPORT_DATA, SECURITY_VIOLATION
    }
    
    /**
     * Audit event data
     */
    public static class AuditEvent {
        private final AuditEventType type;
        private final String description;
        private final LocalDateTime timestamp;
        private final String user;
        private final String details;
        
        public AuditEvent(AuditEventType type, String description, String user, String details) {
            this.type = type;
            this.description = description;
            this.user = user;
            this.details = details;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters
        public AuditEventType getType() { return type; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getUser() { return user; }
        public String getDetails() { return details; }
        
        @Override
        public String toString() {
            return String.format("[%s] %s - %s: %s (%s)",
                timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                type, user, description, details);
        }
    }
    
    /**
     * Encrypted credential storage
     */
    public static class CredentialEntry {
        private String name;
        private String username;
        private String encryptedPassword;
        private String server;
        private int port;
        private String database;
        
        public CredentialEntry(String name, String username, String password, 
                              String server, int port, String database) {
            this.name = name;
            this.username = username;
            this.server = server;
            this.port = port;
            this.database = database;
            try {
                this.encryptedPassword = SecurityManager.getInstance().encryptPassword(password);
            } catch (Exception e) {
                throw new RuntimeException("Failed to encrypt password", e);
            }
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() {
            try {
                return SecurityManager.getInstance().decryptPassword(encryptedPassword);
            } catch (Exception e) {
                throw new RuntimeException("Failed to decrypt password", e);
            }
        }
        
        public void setPassword(String password) {
            try {
                this.encryptedPassword = SecurityManager.getInstance().encryptPassword(password);
            } catch (Exception e) {
                throw new RuntimeException("Failed to encrypt password", e);
            }
        }
        
        public String getServer() { return server; }
        public void setServer(String server) { this.server = server; }
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }
        
        public String getEncryptedPassword() { return encryptedPassword; }
        public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }
    }
    
    private SecurityManager() {
        prefs = Preferences.userNodeForPackage(SecurityManager.class);
        auditListeners = new ArrayList<>();
        initializeEncryption();
        updateActivity();
    }
    
    public static SecurityManager getInstance() {
        if (instance == null) {
            instance = new SecurityManager();
        }
        return instance;
    }
    
    private void initializeEncryption() {
        try {
            String keyString = prefs.get(KEY_PREF, null);
            if (keyString == null) {
                // Generate new key
                KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
                keyGen.init(256);
                encryptionKey = keyGen.generateKey();
                
                // Store key
                String encodedKey = Base64.getEncoder().encodeToString(encryptionKey.getEncoded());
                prefs.put(KEY_PREF, encodedKey);
                prefs.flush();
            } else {
                // Load existing key
                byte[] decodedKey = Base64.getDecoder().decode(keyString);
                encryptionKey = new SecretKeySpec(decodedKey, ALGORITHM);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption", e);
        }
    }
    
    public String encryptPassword(String password) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        byte[] encryptedBytes = cipher.doFinal(password.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    public String decryptPassword(String encryptedPassword) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
        return new String(decryptedBytes);
    }
    
    public void updateActivity() {
        lastActivity = LocalDateTime.now();
    }
    
    public boolean isSessionExpired() {
        if (lastActivity == null) return true;
        return LocalDateTime.now().isAfter(lastActivity.plusMinutes(SESSION_TIMEOUT_MINUTES));
    }
    
    public void logAuditEvent(AuditEventType type, String description, String user, String details) {
        if (!auditEnabled) return;
        
        AuditEvent event = new AuditEvent(type, description, user, details);
        
        // Write to audit log file
        try {
            writeAuditLogEntry(event);
        } catch (IOException e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
        
        // Notify listeners
        for (AuditListener listener : auditListeners) {
            try {
                listener.onAuditEvent(event);
            } catch (Exception e) {
                System.err.println("Error notifying audit listener: " + e.getMessage());
            }
        }
        
        updateActivity();
    }
    
    private void writeAuditLogEntry(AuditEvent event) throws IOException {
        File logFile = new File(AUDIT_LOG_FILE);
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(event.toString() + System.lineSeparator());
            writer.flush();
        }
    }
    
    public List<AuditEvent> getRecentAuditEvents(int maxEvents) {
        List<AuditEvent> events = new ArrayList<>();
        
        try {
            File logFile = new File(AUDIT_LOG_FILE);
            if (!logFile.exists()) return events;
            
            List<String> lines = Files.readAllLines(Paths.get(AUDIT_LOG_FILE));
            
            // Get last N lines
            int startIndex = Math.max(0, lines.size() - maxEvents);
            for (int i = startIndex; i < lines.size(); i++) {
                String line = lines.get(i);
                AuditEvent event = parseAuditLogLine(line);
                if (event != null) {
                    events.add(event);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read audit log: " + e.getMessage());
        }
        
        return events;
    }
    
    private AuditEvent parseAuditLogLine(String line) {
        try {
            // Simple parsing - in production, use more robust parsing
            if (line.contains("] ") && line.contains(" - ") && line.contains(": ")) {
                String[] parts = line.split(" - ", 2);
                if (parts.length >= 2) {
                    String userAndDetails = parts[1];
                    
                    String[] userParts = userAndDetails.split(": ", 2);
                    if (userParts.length >= 2) {
                        String user = userParts[0];
                        String description = userParts[1];
                        
                        // Extract details if present
                        String details = "";
                        if (description.contains(" (") && description.endsWith(")")) {
                            int lastParen = description.lastIndexOf(" (");
                            details = description.substring(lastParen + 2, description.length() - 1);
                            description = description.substring(0, lastParen);
                        }
                        
                        // For simplicity, use COMMAND_EXECUTE as default type
                        return new AuditEvent(AuditEventType.COMMAND_EXECUTE, description, user, details);
                    }
                }
            }
        } catch (Exception e) {
            // Skip malformed lines
        }
        return null;
    }
    
    public void addAuditListener(AuditListener listener) {
        auditListeners.add(listener);
    }
    
    public void removeAuditListener(AuditListener listener) {
        auditListeners.remove(listener);
    }
    
    public boolean isAuditEnabled() {
        return auditEnabled;
    }
    
    public void setAuditEnabled(boolean enabled) {
        this.auditEnabled = enabled;
        if (enabled) {
            logAuditEvent(AuditEventType.SECURITY_VIOLATION, "Audit logging enabled", 
                System.getProperty("user.name"), "Manual configuration");
        }
    }
    
    public void clearAuditLog() {
        try {
            File logFile = new File(AUDIT_LOG_FILE);
            if (logFile.exists()) {
                logFile.delete();
                logAuditEvent(AuditEventType.SECURITY_VIOLATION, "Audit log cleared", 
                    System.getProperty("user.name"), "Manual action");
            }
        } catch (Exception e) {
            System.err.println("Failed to clear audit log: " + e.getMessage());
        }
    }
    
    public void exportAuditLog(String filename) throws IOException {
        File sourceFile = new File(AUDIT_LOG_FILE);
        File targetFile = new File(filename);
        
        if (sourceFile.exists()) {
            Files.copy(sourceFile.toPath(), targetFile.toPath());
            logAuditEvent(AuditEventType.EXPORT_DATA, "Audit log exported", 
                System.getProperty("user.name"), "Export to: " + filename);
        }
    }
    
    /**
     * Validate session and refresh activity
     */
    public boolean validateSession() {
        if (isSessionExpired()) {
            logAuditEvent(AuditEventType.SECURITY_VIOLATION, "Session expired", 
                System.getProperty("user.name"), "Automatic logout");
            return false;
        }
        updateActivity();
        return true;
    }
    
    /**
     * Generate secure random password
     */
    public static String generateSecurePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Validate password strength
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) return false;
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        logAuditEvent(AuditEventType.LOGOUT, "Application shutdown", 
            System.getProperty("user.name"), "Normal exit");
        auditListeners.clear();
    }
}
