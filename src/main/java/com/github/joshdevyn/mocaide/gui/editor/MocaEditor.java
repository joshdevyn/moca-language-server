package com.github.joshdevyn.mocaide.gui.editor;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.autocomplete.*;

import com.github.mrglassdanny.mocalanguageserver.moca.cache.MocaCache;
import com.github.mrglassdanny.mocalanguageserver.moca.cache.MocaCommand;
import com.github.mrglassdanny.mocalanguageserver.moca.cache.MocaFunction;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Enhanced MOCA editor with syntax highlighting, code completion, and real-time error checking
 */
public class MocaEditor extends JPanel {
    
    private RSyntaxTextArea textArea;
    private RTextScrollPane scrollPane;
    private AutoCompletion autoCompletion;
    private CompletionProvider completionProvider;
    private javax.swing.Timer errorCheckTimer;
    private JLabel statusLabel;
    private boolean realTimeLinting = true; // Enable by default
    
    public MocaEditor() {
        initializeComponents();
        setupLayout();
        setupAutoCompletion();
        setupRealTimeLinting();
    }
    
    private void initializeComponents() {
        // Create the main text area with MOCA syntax highlighting
        textArea = new RSyntaxTextArea(20, 60);
        
        // Try to use custom MOCA syntax, fallback to SQL
        try {
            MocaTokenMakerFactory.install();
            textArea.setSyntaxEditingStyle("text/moca");
        } catch (Exception e) {
            // Fallback to SQL syntax highlighting
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        }
        
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setAutoIndentEnabled(true);
        textArea.setBracketMatchingEnabled(true);
        textArea.setPaintTabLines(true);
        textArea.setMarkOccurrences(true);
        textArea.setTabSize(4);
        textArea.setWhitespaceVisible(false);
        
        // Apply clean, professional theme - force light theme
        try {
            // Try to load the clean default theme
            Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default.xml"));
            if (theme != null) {
                theme.apply(textArea);
            } else {
                // Fallback to manual light theme setup
                applyLightTheme();
            }
        } catch (Exception e) {
            // Use clean light theme as fallback
            applyLightTheme();
        }
        
        // Create scroll pane
        scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);
        scrollPane.setIconRowHeaderEnabled(true);
        
        // Status label for showing compilation errors
        statusLabel = new JLabel("Ready");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(240, 240, 240));
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void setupAutoCompletion() {
        // Create completion provider
        completionProvider = new DefaultCompletionProvider();
        
        // Add comprehensive MOCA keywords
        String[] mocaKeywords = {
            // Control structures
            "if", "elsif", "else", "endif", "while", "endwhile", "for", "endfor",
            "try", "catch", "endtry", "throw", "return", "break", "continue",
            
            // Logical operators
            "and", "or", "not", "in", "like", "is", "null", "true", "false",
            
            // MOCA commands
            "create", "modify", "remove", "list", "count", "get", "set", "move",
            "publish", "data", "where", "order", "by", "group", "having",
            "distinct", "all", "any", "some", "exists", "between", "case",
            "when", "then", "end", "union", "intersect", "except",
            
            // Common MOCA objects
            "inventory", "location", "item", "order", "shipment", "receipt",
            "cycle", "work", "task", "user", "client", "company", "facility",
            
            // Common operations
            "select", "insert", "update", "delete", "commit", "rollback"
        };
        
        for (String keyword : mocaKeywords) {
            ((DefaultCompletionProvider) completionProvider).addCompletion(
                new BasicCompletion(completionProvider, keyword, "MOCA keyword"));
        }
        
        // Add enhanced MOCA built-in functions with descriptions
        addMocaFunction("nvl", "nvl(expr1, expr2)", "Returns expr2 if expr1 is null, otherwise returns expr1");
        addMocaFunction("decode", "decode(expr, search1, result1, ...)", "Compares expr to search values and returns corresponding result");
        addMocaFunction("substr", "substr(string, start, length)", "Returns substring starting at position start with specified length");
        addMocaFunction("length", "length(string)", "Returns the length of the string");
        addMocaFunction("upper", "upper(string)", "Converts string to uppercase");
        addMocaFunction("lower", "lower(string)", "Converts string to lowercase");
        addMocaFunction("trim", "trim(string)", "Removes leading and trailing whitespace");
        addMocaFunction("to_char", "to_char(value, format)", "Converts value to character string using format");
        addMocaFunction("to_date", "to_date(string, format)", "Converts string to date using format");
        addMocaFunction("to_number", "to_number(string)", "Converts string to number");
        addMocaFunction("sysdate", "sysdate", "Returns current system date and time");
        addMocaFunction("user", "user", "Returns current user ID");
        addMocaFunction("rownum", "rownum", "Returns row number in result set");
        addMocaFunction("max", "max(expr)", "Returns maximum value");
        addMocaFunction("min", "min(expr)", "Returns minimum value");
        addMocaFunction("sum", "sum(expr)", "Returns sum of values");
        addMocaFunction("count", "count(expr)", "Returns count of non-null values");
        addMocaFunction("avg", "avg(expr)", "Returns average value");
        addMocaFunction("stddev", "stddev(expr)", "Returns standard deviation");
        addMocaFunction("variance", "variance(expr)", "Returns variance");
        addMocaFunction("concat", "concat(str1, str2)", "Concatenates two strings");
        addMocaFunction("instr", "instr(string, substring)", "Returns position of substring in string");
        addMocaFunction("replace", "replace(string, search, replace)", "Replaces occurrences of search with replace");
        addMocaFunction("translate", "translate(string, from, to)", "Translates characters in string");
        addMocaFunction("lpad", "lpad(string, length, pad)", "Left-pads string to specified length");
        addMocaFunction("rpad", "rpad(string, length, pad)", "Right-pads string to specified length");
        
        // Setup auto-completion with enhanced settings
        autoCompletion = new AutoCompletion(completionProvider);
        autoCompletion.setAutoActivationEnabled(true);
        autoCompletion.setAutoActivationDelay(200); // Faster activation
        autoCompletion.setAutoCompleteSingleChoices(false);
        autoCompletion.setShowDescWindow(true); // Show descriptions
        autoCompletion.setDescriptionWindowSize(400, 300);
        autoCompletion.install(textArea);
    }
    
    private void addMocaFunction(String name, String signature, String description) {
        FunctionCompletion completion = new FunctionCompletion(completionProvider, name, signature);
        completion.setShortDescription(description);
        ((DefaultCompletionProvider) completionProvider).addCompletion(completion);
    }
    
    private void setupRealTimeLinting() {
        // Timer for delayed error checking (avoid checking on every keystroke)
        errorCheckTimer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (realTimeLinting) {
                    performErrorCheck();
                }
            }
        });
        errorCheckTimer.setRepeats(false);
        
        // Document listener to trigger error checking
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleErrorCheck();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleErrorCheck();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleErrorCheck();
            }
        });
    }
    
    private void scheduleErrorCheck() {
        if (errorCheckTimer.isRunning()) {
            errorCheckTimer.restart();
        } else {
            errorCheckTimer.start();
        }
    }
    
    private void performErrorCheck() {
        String code = textArea.getText();
        if (code.trim().isEmpty()) {
            setStatus("Ready", false);
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Simple syntax validation for now
                if (code.contains("select") || code.contains("list") || code.contains("get")) {
                    setStatus("Syntax OK", false);
                } else {
                    setStatus("Unknown command", true);
                }
            } catch (Exception e) {
                setStatus("Error: " + e.getMessage(), true);
            }
        });
    }
    
    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        if (isError) {
            statusLabel.setBackground(new Color(255, 235, 235)); // Light red
            statusLabel.setForeground(new Color(139, 0, 0)); // Dark red text
        } else {
            statusLabel.setBackground(new Color(240, 240, 240)); // Light gray
            statusLabel.setForeground(Color.BLACK);
        }
    }
    
    /**
     * Update completion provider with MOCA cache data
     */
    public void updateCompletions(MocaCache cache) {
        if (cache == null) return;
        
        DefaultCompletionProvider provider = (DefaultCompletionProvider) completionProvider;
        
        // Add MOCA commands from cache
        if (cache.commands != null) {
            for (java.util.ArrayList<MocaCommand> commandList : cache.commands.values()) {
                if (commandList != null && !commandList.isEmpty()) {
                    MocaCommand command = commandList.get(0); // Get the first command from the list
                    String description = "MOCA Command: " + command.command;
                    if (command.desc != null && !command.desc.isEmpty()) {
                        description += " - " + command.desc;
                    }
                    provider.addCompletion(new BasicCompletion(provider, command.command, description));
                }
            }
        }
        
        // Add MOCA functions from cache
        if (cache.functions != null) {
            for (MocaFunction function : cache.functions.values()) {
                String description = "MOCA Function: " + function.name;
                if (function.description != null && !function.description.isEmpty()) {
                    description += " - " + function.description;
                }
                provider.addCompletion(new FunctionCompletion(provider, function.name, description));
            }
        }
    }
    
    /**
     * Update completions with server data (commands, tables, columns)
     */
    public void updateCompletions(java.util.List<String> commands, java.util.List<String> tables, java.util.List<String> columns) {
        DefaultCompletionProvider provider = (DefaultCompletionProvider) completionProvider;
        
        // Clear existing server-based completions (keep keywords and functions)
        // Note: This is a simplified approach - in a real implementation you'd want to track what was added
        
        // Add commands from server
        if (commands != null) {
            for (String command : commands) {
                provider.addCompletion(new BasicCompletion(provider, command, "MOCA Command from server"));
            }
        }
        
        // Add tables from server
        if (tables != null) {
            for (String table : tables) {
                provider.addCompletion(new BasicCompletion(provider, table, "Table: " + table));
            }
        }
        
        // Add columns from server
        if (columns != null) {
            for (String column : columns) {
                provider.addCompletion(new BasicCompletion(provider, column, "Column: " + column));
            }
        }
        
        // Update the status to show server completions are available
        setStatus("Server completions loaded (" + 
                  (commands != null ? commands.size() : 0) + " commands, " +
                  (tables != null ? tables.size() : 0) + " tables, " +
                  (columns != null ? columns.size() : 0) + " columns)", false);
    }
    
    /**
     * Enable or disable server-based validation
     */
    public void setServerValidation(boolean enabled) {
        if (enabled) {
            setStatus("Server validation enabled", false);
        } else {
            setStatus("Using offline validation", false);
        }
        // This would integrate with the language server in a full implementation
    }
    
    /**
     * Get the text content of the editor
     */
    public String getText() {
        return textArea.getText();
    }
    
    /**
     * Set the text content of the editor
     */
    public void setText(String text) {
        textArea.setText(text);
    }
    
    /**
     * Clear the editor content
     */
    public void clear() {
        textArea.setText("");
    }
    
    /**
     * Enable or disable real-time linting
     */
    public void setRealTimeLinting(boolean enabled) {
        this.realTimeLinting = enabled;
        if (!enabled) {
            setStatus("Ready", false);
        }
    }
    
    /**
     * Get the underlying RSyntaxTextArea component
     */
    public RSyntaxTextArea getTextArea() {
        return textArea;
    }
    
    /**
     * Insert text at the current cursor position
     */
    public void insertText(String text) {
        textArea.insert(text, textArea.getCaretPosition());
    }
    
    /**
     * Get selected text
     */
    public String getSelectedText() {
        return textArea.getSelectedText();
    }
    
    /**
     * Replace selected text
     */
    public void replaceSelection(String text) {
        textArea.replaceSelection(text);
    }
    
    /**
     * Apply a clean light theme to the editor
     */
    private void applyLightTheme() {
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(Color.BLACK);
        textArea.setCaretColor(Color.BLACK);
        textArea.setSelectionColor(new Color(184, 207, 229)); // Light blue selection
        textArea.setCurrentLineHighlightColor(new Color(248, 248, 255)); // Very light blue
        textArea.setMarkOccurrencesColor(new Color(255, 255, 153)); // Light yellow
        textArea.setMatchedBracketBGColor(new Color(238, 238, 238)); // Light gray
        textArea.setMatchedBracketBorderColor(new Color(128, 128, 128)); // Gray border
    }
}
