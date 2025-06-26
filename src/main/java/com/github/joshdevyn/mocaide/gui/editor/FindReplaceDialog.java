package com.github.joshdevyn.mocaide.gui.editor;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Advanced Find/Replace dialog with regex support, case sensitivity,
 * whole word matching, and search scope options
 */
public class FindReplaceDialog extends JDialog {
    
    private final RSyntaxTextArea textArea;
    
    // UI Components
    private JTextField findField;
    private JTextField replaceField;
    private JCheckBox caseSensitiveCheck;
    private JCheckBox wholeWordCheck;
    private JCheckBox regexCheck;
    private JCheckBox wrapAroundCheck;
    private JRadioButton forwardRadio;
    private JRadioButton backwardRadio;
    private JRadioButton allRadio;
    private JRadioButton selectionRadio;
    
    private JButton findNextButton;
    private JButton findPreviousButton;
    private JButton replaceButton;
    private JButton replaceAllButton;
    private JButton closeButton;
    
    private JLabel statusLabel;
    private JLabel matchCountLabel;
    
    // Search state
    private SearchContext searchContext;
    private boolean isReplaceMode;
    
    public FindReplaceDialog(Frame parent, RSyntaxTextArea textArea, boolean replaceMode) {
        super(parent, replaceMode ? "Find & Replace" : "Find", false);
        this.textArea = textArea;
        this.isReplaceMode = replaceMode;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupKeyBindings();
        finalizeDialog();
    }
    
    private void initializeComponents() {
        // Text fields
        findField = new JTextField(20);
        replaceField = new JTextField(20);
        
        // Checkboxes
        caseSensitiveCheck = new JCheckBox("Case sensitive");
        wholeWordCheck = new JCheckBox("Whole word");
        regexCheck = new JCheckBox("Regular expression");
        wrapAroundCheck = new JCheckBox("Wrap around", true);
        
        // Radio buttons for direction
        forwardRadio = new JRadioButton("Forward", true);
        backwardRadio = new JRadioButton("Backward");
        ButtonGroup directionGroup = new ButtonGroup();
        directionGroup.add(forwardRadio);
        directionGroup.add(backwardRadio);
        
        // Radio buttons for scope
        allRadio = new JRadioButton("All", true);
        selectionRadio = new JRadioButton("Selection only");
        ButtonGroup scopeGroup = new ButtonGroup();
        scopeGroup.add(allRadio);
        scopeGroup.add(selectionRadio);
        
        // Buttons
        findNextButton = new JButton("Find Next");
        findPreviousButton = new JButton("Find Previous");
        replaceButton = new JButton("Replace");
        replaceAllButton = new JButton("Replace All");
        closeButton = new JButton("Close");
        
        // Status labels
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.BLUE);
        matchCountLabel = new JLabel(" ");
        matchCountLabel.setForeground(Color.GRAY);
        
        // Initialize search context
        searchContext = new SearchContext();
        updateSearchContext();
        
        // Set initial state
        if (!isReplaceMode) {
            replaceField.setVisible(false);
            replaceButton.setVisible(false);
            replaceAllButton.setVisible(false);
        }
        
        // Pre-fill with selected text if any
        String selectedText = textArea.getSelectedText();
        if (selectedText != null && !selectedText.contains("\n")) {
            findField.setText(selectedText);
            selectionRadio.setEnabled(true);
        } else {
            selectionRadio.setEnabled(false);
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Find section
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        mainPanel.add(new JLabel("Find:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(findField, gbc);
        
        // Replace section (if in replace mode)
        if (isReplaceMode) {
            gbc.gridx = 0; gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            mainPanel.add(new JLabel("Replace:"), gbc);
            
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            mainPanel.add(replaceField, gbc);
        }
        
        // Options panel
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        optionsPanel.setBorder(new TitledBorder("Options"));
        optionsPanel.add(caseSensitiveCheck);
        optionsPanel.add(wholeWordCheck);
        optionsPanel.add(regexCheck);
        optionsPanel.add(wrapAroundCheck);
        
        gbc.gridx = 0; gbc.gridy = isReplaceMode ? 2 : 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(optionsPanel, gbc);
        
        // Direction panel
        JPanel directionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        directionPanel.setBorder(new TitledBorder("Direction"));
        directionPanel.add(forwardRadio);
        directionPanel.add(backwardRadio);
        
        // Scope panel
        JPanel scopePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scopePanel.setBorder(new TitledBorder("Scope"));
        scopePanel.add(allRadio);
        scopePanel.add(selectionRadio);
        
        JPanel directionScopePanel = new JPanel(new GridLayout(1, 2));
        directionScopePanel.add(directionPanel);
        directionScopePanel.add(scopePanel);
        
        gbc.gridy++;
        mainPanel.add(directionScopePanel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(findNextButton);
        buttonPanel.add(findPreviousButton);
        if (isReplaceMode) {
            buttonPanel.add(replaceButton);
            buttonPanel.add(replaceAllButton);
        }
        buttonPanel.add(closeButton);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(matchCountLabel, BorderLayout.EAST);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(statusPanel, BorderLayout.NORTH);
    }
    
    private void setupEventHandlers() {
        // Find buttons
        findNextButton.addActionListener(e -> findNext());
        findPreviousButton.addActionListener(e -> findPrevious());
        
        // Replace buttons
        replaceButton.addActionListener(e -> replace());
        replaceAllButton.addActionListener(e -> replaceAll());
        
        // Close button
        closeButton.addActionListener(e -> setVisible(false));
        
        // Option change listeners
        ActionListener optionListener = e -> updateSearchContext();
        caseSensitiveCheck.addActionListener(optionListener);
        wholeWordCheck.addActionListener(optionListener);
        regexCheck.addActionListener(optionListener);
        wrapAroundCheck.addActionListener(optionListener);
        forwardRadio.addActionListener(optionListener);
        backwardRadio.addActionListener(optionListener);
        
        // Text field listeners
        findField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSearchContext(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSearchContext(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSearchContext(); }
        });
        
        // Enter key in find field
        findField.addActionListener(e -> findNext());
        
        // Window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
    }
    
    private void setupKeyBindings() {
        // Escape key to close
        KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        
        // F3 for find next
        KeyStroke f3Stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f3Stroke, "FIND_NEXT");
        getRootPane().getActionMap().put("FIND_NEXT", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findNext();
            }
        });
        
        // Shift+F3 for find previous
        KeyStroke shiftF3Stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(shiftF3Stroke, "FIND_PREVIOUS");
        getRootPane().getActionMap().put("FIND_PREVIOUS", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findPrevious();
            }
        });
    }
    
    private void finalizeDialog() {
        pack();
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        
        // Focus on find field
        SwingUtilities.invokeLater(() -> findField.requestFocusInWindow());
    }
    
    private void updateSearchContext() {
        String searchText = findField.getText();
        
        if (searchContext == null) {
            searchContext = new SearchContext();
        }
        
        searchContext.setSearchFor(searchText);
        searchContext.setMatchCase(caseSensitiveCheck.isSelected());
        searchContext.setWholeWord(wholeWordCheck.isSelected());
        searchContext.setRegularExpression(regexCheck.isSelected());
        searchContext.setSearchForward(forwardRadio.isSelected());
        
        // Validate regex if enabled
        if (regexCheck.isSelected() && !searchText.isEmpty()) {
            try {
                Pattern.compile(searchText);
                statusLabel.setText(" ");
                statusLabel.setForeground(Color.BLUE);
            } catch (PatternSyntaxException e) {
                statusLabel.setText("Invalid regex: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
                return;
            }
        }
        
        // Update match count if search text is not empty
        if (!searchText.isEmpty()) {
            updateMatchCount();
        } else {
            matchCountLabel.setText(" ");
        }
    }
    
    private void updateMatchCount() {
        // This is a simplified implementation
        // A more sophisticated version would count all matches
        matchCountLabel.setText(""); // For now, leave empty
    }
    
    private void findNext() {
        if (findField.getText().isEmpty()) {
            statusLabel.setText("Search field is empty");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        updateSearchContext();
        searchContext.setSearchForward(true);
        
        SearchResult result = SearchEngine.find(textArea, searchContext);
        boolean found = result.wasFound();
        if (found) {
            statusLabel.setText("Found");
            statusLabel.setForeground(Color.BLUE);
        } else {
            statusLabel.setText("Not found");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void findPrevious() {
        if (findField.getText().isEmpty()) {
            statusLabel.setText("Search field is empty");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        updateSearchContext();
        searchContext.setSearchForward(false);
        
        SearchResult result = SearchEngine.find(textArea, searchContext);
        boolean found = result.wasFound();
        if (found) {
            statusLabel.setText("Found");
            statusLabel.setForeground(Color.BLUE);
        } else {
            statusLabel.setText("Not found");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void replace() {
        if (!isReplaceMode) return;
        
        String selectedText = textArea.getSelectedText();
        if (selectedText != null && selectedText.equals(findField.getText())) {
            textArea.replaceSelection(replaceField.getText());
            findNext(); // Find next occurrence
            statusLabel.setText("Replaced and found next");
            statusLabel.setForeground(Color.BLUE);
        } else {
            findNext(); // Find first occurrence
        }
    }
    
    private void replaceAll() {
        if (!isReplaceMode) return;
        
        updateSearchContext();
        searchContext.setReplaceWith(replaceField.getText());
        
        SearchResult result = SearchEngine.replaceAll(textArea, searchContext);
        int count = result.getCount();
        statusLabel.setText("Replaced " + count + " occurrences");
        statusLabel.setForeground(Color.BLUE);
    }
    
    /**
     * Show the dialog with the given search text
     */
    public void showDialog(String searchText) {
        if (searchText != null && !searchText.isEmpty()) {
            findField.setText(searchText);
        }
        
        // Update selection radio state
        selectionRadio.setEnabled(textArea.getSelectedText() != null);
        
        setVisible(true);
        findField.requestFocusInWindow();
        findField.selectAll();
    }
    
    /**
     * Create a Find dialog
     */
    public static FindReplaceDialog createFindDialog(Frame parent, RSyntaxTextArea textArea) {
        return new FindReplaceDialog(parent, textArea, false);
    }
    
    /**
     * Create a Find & Replace dialog
     */
    public static FindReplaceDialog createReplaceDialog(Frame parent, RSyntaxTextArea textArea) {
        return new FindReplaceDialog(parent, textArea, true);
    }
}
