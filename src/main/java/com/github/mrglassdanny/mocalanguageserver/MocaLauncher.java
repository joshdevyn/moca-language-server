package com.github.mrglassdanny.mocalanguageserver;

import com.github.joshdevyn.mocaide.gui.MocaGuiWindow;

/**
 * Main launcher class that can start either the GUI application or the language server
 */
public class MocaLauncher {
    
    public static void main(String[] args) {
        // Check command line arguments
        boolean useGui = true;
        
        for (String arg : args) {
            if ("--server".equals(arg) || "--lsp".equals(arg)) {
                useGui = false;
                break;
            } else if ("--gui".equals(arg)) {
                useGui = true;
                break;
            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                printHelp();
                return;
            }
        }
        
        if (useGui) {
            // Check if GUI is available (not running in headless mode)
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                System.err.println("GUI not available in headless environment. Starting language server instead.");
                startLanguageServer(args);
            } else {
                startGui(args);
            }
        } else {
            startLanguageServer(args);
        }
    }
    
    private static void startGui(String[] args) {
        try {
            // Launch the GUI window
            javax.swing.SwingUtilities.invokeLater(() -> {
                new MocaGuiWindow().setVisible(true);
            });
        } catch (Exception e) {
            System.err.println("Failed to start GUI application: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Falling back to language server mode...");
            startLanguageServer(args);
        }
    }
    
    private static void startLanguageServer(String[] args) {
        try {
            com.github.mrglassdanny.mocalanguageserver.MocaLanguageServer.main(args);
        } catch (Exception e) {
            System.err.println("Failed to start language server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void printHelp() {
        System.out.println("MOCA Language Server");
        System.out.println("Usage: java -jar moca-language-server.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --gui         Start GUI application (default)");
        System.out.println("  --server      Start language server only");
        System.out.println("  --lsp         Alias for --server");
        System.out.println("  --help, -h    Show this help message");
        System.out.println();
        System.out.println("GUI Mode:");
        System.out.println("  Provides a graphical interface for connecting to MOCA servers,");
        System.out.println("  executing commands, and managing the language server.");
        System.out.println();
        System.out.println("Server Mode:");
        System.out.println("  Runs as a Language Server Protocol server for IDE integration.");
        System.out.println("  Communicates via stdin/stdout for LSP clients.");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar moca-language-server.jar");
        System.out.println("  java -jar moca-language-server.jar --gui");
        System.out.println("  java -jar moca-language-server.jar --server");
    }
}
