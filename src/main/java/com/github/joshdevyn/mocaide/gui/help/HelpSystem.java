package com.github.joshdevyn.mocaide.gui.help;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive help system with context-sensitive help, MOCA documentation,
 * and keyboard shortcuts reference
 */
public class HelpSystem extends JDialog {
    
    private static HelpSystem instance;
    private JTabbedPane tabbedPane;
    private JTextArea helpContent;
    private JTree commandTree;
    private JTable shortcutTable;
    
    // Help content database
    private static final Map<String, String> HELP_TOPICS = new HashMap<>();
    private static final Map<String, String> MOCA_COMMANDS = new HashMap<>();
    private static final String[][] KEYBOARD_SHORTCUTS = {
        {"File Operations", "", ""},
        {"Ctrl+N", "New Script", "Create a new MOCA script file"},
        {"Ctrl+O", "Open Script", "Open an existing script file"},
        {"Ctrl+S", "Save Script", "Save the current script"},
        {"Ctrl+Shift+S", "Save As", "Save script with a new name"},
        {"", "", ""},
        {"Edit Operations", "", ""},
        {"Ctrl+Z", "Undo", "Undo the last action"},
        {"Ctrl+Y", "Redo", "Redo the last undone action"},
        {"Ctrl+X", "Cut", "Cut selected text"},
        {"Ctrl+C", "Copy", "Copy selected text"},
        {"Ctrl+V", "Paste", "Paste from clipboard"},
        {"Ctrl+A", "Select All", "Select all text"},
        {"", "", ""},
        {"Search & Navigation", "", ""},
        {"Ctrl+F", "Find", "Open find dialog"},
        {"Ctrl+H", "Replace", "Open find and replace dialog"},
        {"Ctrl+G", "Go to Line", "Go to specific line number"},
        {"F2", "Toggle Bookmark", "Toggle bookmark on current line"},
        {"Ctrl+F2", "Next Bookmark", "Go to next bookmark"},
        {"Shift+F2", "Previous Bookmark", "Go to previous bookmark"},
        {"", "", ""},
        {"MOCA Operations", "", ""},
        {"F5", "Execute Command", "Execute the current command or selection"},
        {"F6", "Connect to Server", "Connect to MOCA server"},
        {"F7", "Check Syntax", "Check syntax of current script"},
        {"Ctrl+Alt+L", "Format Code", "Format the current script"},
        {"", "", ""},
        {"Help & Documentation", "", ""},
        {"F1", "Help", "Open help documentation"},
        {"Ctrl+?", "Keyboard Shortcuts", "Show keyboard shortcuts"}
    };
    
    static {
        initializeHelpContent();
        initializeMocaCommands();
    }
    
    private HelpSystem(Window parent) {
        super(parent, "MOCA GUI Help", ModalityType.MODELESS);
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }
    
    public static HelpSystem getInstance(Window parent) {
        if (instance == null) {
            instance = new HelpSystem(parent);
        }
        return instance;
    }
    
    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        
        // General Help tab
        helpContent = new JTextArea();
        helpContent.setEditable(false);
        helpContent.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        helpContent.setLineWrap(true);
        helpContent.setWrapStyleWord(true);
        
        JScrollPane helpScrollPane = new JScrollPane(helpContent);
        helpScrollPane.setBorder(BorderFactory.createTitledBorder("Help Content"));
        
        // MOCA Commands tab
        commandTree = createCommandTree();
        JScrollPane commandScrollPane = new JScrollPane(commandTree);
        commandScrollPane.setBorder(BorderFactory.createTitledBorder("MOCA Commands"));
        
        // Keyboard Shortcuts tab
        shortcutTable = createShortcutTable();
        JScrollPane shortcutScrollPane = new JScrollPane(shortcutTable);
        shortcutScrollPane.setBorder(BorderFactory.createTitledBorder("Keyboard Shortcuts"));
        
        tabbedPane.addTab("General Help", createIcon("?"), helpScrollPane, "General help and usage information");
        tabbedPane.addTab("MOCA Commands", createIcon("⌘"), commandScrollPane, "MOCA command reference");
        tabbedPane.addTab("Keyboard Shortcuts", createIcon("⌨"), shortcutScrollPane, "Keyboard shortcuts reference");
        
        // Show general help by default
        showHelp("general");
    }
    
    private JTree createCommandTree() {
        // Create tree structure for MOCA commands
        javax.swing.tree.DefaultMutableTreeNode root = new javax.swing.tree.DefaultMutableTreeNode("MOCA Commands");
        
        javax.swing.tree.DefaultMutableTreeNode dataCommands = new javax.swing.tree.DefaultMutableTreeNode("Data Commands");
        dataCommands.add(new javax.swing.tree.DefaultMutableTreeNode("list"));
        dataCommands.add(new javax.swing.tree.DefaultMutableTreeNode("select"));
        dataCommands.add(new javax.swing.tree.DefaultMutableTreeNode("insert"));
        dataCommands.add(new javax.swing.tree.DefaultMutableTreeNode("update"));
        dataCommands.add(new javax.swing.tree.DefaultMutableTreeNode("delete"));
        
        javax.swing.tree.DefaultMutableTreeNode controlCommands = new javax.swing.tree.DefaultMutableTreeNode("Control Commands");
        controlCommands.add(new javax.swing.tree.DefaultMutableTreeNode("if"));
        controlCommands.add(new javax.swing.tree.DefaultMutableTreeNode("while"));
        controlCommands.add(new javax.swing.tree.DefaultMutableTreeNode("try"));
        controlCommands.add(new javax.swing.tree.DefaultMutableTreeNode("catch"));
        
        javax.swing.tree.DefaultMutableTreeNode systemCommands = new javax.swing.tree.DefaultMutableTreeNode("System Commands");
        systemCommands.add(new javax.swing.tree.DefaultMutableTreeNode("publish"));
        systemCommands.add(new javax.swing.tree.DefaultMutableTreeNode("execute"));
        systemCommands.add(new javax.swing.tree.DefaultMutableTreeNode("get"));
        systemCommands.add(new javax.swing.tree.DefaultMutableTreeNode("set"));
        
        root.add(dataCommands);
        root.add(controlCommands);
        root.add(systemCommands);
        
        JTree tree = new JTree(root);
        tree.setRootVisible(true);
        tree.expandRow(0);
        
        // Add selection listener
        tree.addTreeSelectionListener(e -> {
            javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) 
                tree.getLastSelectedPathComponent();
            if (node != null && node.isLeaf()) {
                String command = node.toString();
                showMocaCommandHelp(command);
            }
        });
        
        return tree;
    }
    
    private JTable createShortcutTable() {
        String[] columnNames = {"Shortcut", "Action", "Description"};
        
        JTable table = new JTable(KEYBOARD_SHORTCUTS, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                
                // Highlight category rows
                String shortcut = (String) getValueAt(row, 0);
                String action = (String) getValueAt(row, 1);
                
                if (shortcut.isEmpty() && action.isEmpty()) {
                    // Separator row
                    c.setBackground(Color.LIGHT_GRAY);
                } else if (action.isEmpty()) {
                    // Category row
                    c.setBackground(new Color(230, 230, 250));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    // Regular row
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
                
                return c;
            }
        };
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(20);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        
        return table;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> setVisible(false));
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Keyboard shortcuts for help dialog
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        actionMap.put("close", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setVisible(false);
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "help");
        actionMap.put("help", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showHelp("general");
            }
        });
    }
    
    public void showHelp(String topic) {
        tabbedPane.setSelectedIndex(0);
        String content = HELP_TOPICS.getOrDefault(topic, HELP_TOPICS.get("general"));
        helpContent.setText(content);
        helpContent.setCaretPosition(0);
        setVisible(true);
    }
    
    public void showMocaCommandHelp(String command) {
        tabbedPane.setSelectedIndex(1);
        String content = MOCA_COMMANDS.getOrDefault(command.toLowerCase(), 
            "No documentation available for command: " + command);
        
        // Create detailed help content
        StringBuilder sb = new StringBuilder();
        sb.append("MOCA Command: ").append(command.toUpperCase()).append("\n");
        sb.append("=".repeat(20 + command.length())).append("\n\n");
        sb.append(content);
        
        helpContent.setText(sb.toString());
        helpContent.setCaretPosition(0);
        setVisible(true);
    }
    
    public void showKeyboardShortcuts() {
        tabbedPane.setSelectedIndex(2);
        setVisible(true);
    }
    
    private static void initializeHelpContent() {
        HELP_TOPICS.put("general", 
            "MOCA GUI - Enterprise MOCA Development Environment\n" +
            "==================================================\n\n" +
            "Welcome to MOCA GUI, a professional development environment for MOCA scripts and commands.\n\n" +
            "MAIN FEATURES:\n" +
            "• Advanced script editor with syntax highlighting\n" +
            "• Multi-command execution with result tabbing\n" +
            "• Connection management with profiles\n" +
            "• Data export and visualization tools\n" +
            "• Performance monitoring and analysis\n" +
            "• Find/Replace with regex support\n" +
            "• Code bookmarks and navigation\n\n" +
            "GETTING STARTED:\n" +
            "1. Configure your MOCA server connection in the Connections tab\n" +
            "2. Write or open a MOCA script in the Command & Script tab\n" +
            "3. Execute commands using F5 or the Execute button\n" +
            "4. View results in the integrated results panel\n" +
            "5. Export data or create visualizations as needed\n\n" +
            "TIPS:\n" +
            "• Use Ctrl+F to find text in scripts\n" +
            "• Press F2 to bookmark important lines\n" +
            "• Right-click on results for export options\n" +
            "• Use the Performance tab to monitor execution times\n" +
            "• Panels can be resized by dragging the dividers\n\n" +
            "For more specific help, use the MOCA Commands or Keyboard Shortcuts tabs.");
            
        HELP_TOPICS.put("connection", 
            "CONNECTION MANAGEMENT\n" +
            "=====================\n\n" +
            "The Connection Manager allows you to manage multiple MOCA server connections.\n\n" +
            "CREATING A CONNECTION:\n" +
            "1. Click 'New Connection' in the Connections tab\n" +
            "2. Enter server details (hostname, port, database)\n" +
            "3. Provide authentication credentials\n" +
            "4. Test the connection before saving\n" +
            "5. Save as a profile for future use\n\n" +
            "CONNECTION PROFILES:\n" +
            "• Save frequently used connections as profiles\n" +
            "• Import/export profile configurations\n" +
            "• Set favorites for quick access\n" +
            "• View connection status and health\n\n" +
            "SECURITY:\n" +
            "• Credentials are encrypted when stored\n" +
            "• SSL/TLS connections are supported\n" +
            "• Session timeouts prevent unauthorized access");
            
        HELP_TOPICS.put("editor", 
            "SCRIPT EDITOR\n" +
            "=============\n\n" +
            "The advanced script editor provides professional IDE features for MOCA development.\n\n" +
            "FEATURES:\n" +
            "• Syntax highlighting for MOCA commands\n" +
            "• Auto-indentation and bracket matching\n" +
            "• Line numbering and goto line (Ctrl+G)\n" +
            "• Code folding for better organization\n" +
            "• Bookmarks for navigation (F2)\n" +
            "• Find/Replace with regex support (Ctrl+F/H)\n" +
            "• Undo/Redo with unlimited history\n\n" +
            "EXECUTION:\n" +
            "• Execute entire script or selected text\n" +
            "• Multiple commands create separate result tabs\n" +
            "• Real-time syntax checking\n" +
            "• Performance timing for each command\n\n" +
            "BOOKMARKS:\n" +
            "• F2: Toggle bookmark on current line\n" +
            "• Ctrl+F2: Go to next bookmark\n" +
            "• Shift+F2: Go to previous bookmark\n" +
            "• Bookmarks persist across sessions");
    }
    
    private static void initializeMocaCommands() {
        MOCA_COMMANDS.put("list", 
            "LIST Command\n\n" +
            "Purpose: Retrieve data from database tables or views\n\n" +
            "Syntax:\n" +
            "  list <table_name> [where <condition>] [order by <column>]\n\n" +
            "Examples:\n" +
            "  list invlod\n" +
            "  list invlod where client_id = 'CLIENT01'\n" +
            "  list invlod where qty > 100 order by create_dt desc\n\n" +
            "Parameters:\n" +
            "• table_name: Name of the table or view to query\n" +
            "• where: Optional filter condition\n" +
            "• order by: Optional sorting specification\n\n" +
            "Returns: Result set with matching records");
            
        MOCA_COMMANDS.put("select", 
            "SELECT Command\n\n" +
            "Purpose: Execute custom SQL SELECT statements\n\n" +
            "Syntax:\n" +
            "  select <columns> from <table> [where <condition>]\n\n" +
            "Examples:\n" +
            "  select * from invlod\n" +
            "  select lodnum, client_id, qty from invlod where qty > 0\n" +
            "  select count(*) as total_records from invlod\n\n" +
            "Features:\n" +
            "• Full SQL SELECT syntax support\n" +
            "• Joins, subqueries, and aggregations\n" +
            "• Parameter substitution with @variable syntax\n\n" +
            "Returns: Result set based on the SELECT statement");
            
        MOCA_COMMANDS.put("insert", 
            "INSERT Command\n\n" +
            "Purpose: Insert new records into database tables\n\n" +
            "Syntax:\n" +
            "  insert into <table> (<columns>) values (<values>)\n\n" +
            "Examples:\n" +
            "  insert into temp_table (id, name) values (1, 'Test')\n" +
            "  insert into invlod (lodnum, client_id) values (@lodnum, @client)\n\n" +
            "Features:\n" +
            "• Parameter substitution with @variable syntax\n" +
            "• Bulk insert operations\n" +
            "• Auto-generated key support\n\n" +
            "Returns: Number of rows inserted");
            
        MOCA_COMMANDS.put("update", 
            "UPDATE Command\n\n" +
            "Purpose: Modify existing records in database tables\n\n" +
            "Syntax:\n" +
            "  update <table> set <column>=<value> [where <condition>]\n\n" +
            "Examples:\n" +
            "  update invlod set qty = 100 where lodnum = 'LOD001'\n" +
            "  update invlod set last_upd_dt = sysdate where client_id = @client\n\n" +
            "Features:\n" +
            "• Parameter substitution with @variable syntax\n" +
            "• Conditional updates with WHERE clause\n" +
            "• Multiple column updates\n\n" +
            "Returns: Number of rows updated");
            
        MOCA_COMMANDS.put("delete", 
            "DELETE Command\n\n" +
            "Purpose: Remove records from database tables\n\n" +
            "Syntax:\n" +
            "  delete from <table> [where <condition>]\n\n" +
            "Examples:\n" +
            "  delete from temp_table where id = 1\n" +
            "  delete from invlod where qty = 0 and client_id = @client\n\n" +
            "Features:\n" +
            "• Parameter substitution with @variable syntax\n" +
            "• Conditional deletion with WHERE clause\n" +
            "• Cascading delete support\n\n" +
            "Returns: Number of rows deleted");
            
        MOCA_COMMANDS.put("publish", 
            "PUBLISH Command\n\n" +
            "Purpose: Execute MOCA commands or call stored procedures\n\n" +
            "Syntax:\n" +
            "  publish <command> [where <parameters>]\n\n" +
            "Examples:\n" +
            "  publish 'list inventory locations'\n" +
            "  publish 'create inventory' where lodnum = @lodnum and prtnum = @part\n\n" +
            "Features:\n" +
            "• Execute any valid MOCA command\n" +
            "• Parameter passing with WHERE clause\n" +
            "• Result set merging and processing\n\n" +
            "Returns: Result set from the published command");
            
        MOCA_COMMANDS.put("execute", 
            "EXECUTE Command\n\n" +
            "Purpose: Execute stored procedures or dynamic SQL\n\n" +
            "Syntax:\n" +
            "  execute <procedure_name> [with <parameters>]\n\n" +
            "Examples:\n" +
            "  execute 'sp_calculate_inventory'\n" +
            "  execute 'sp_process_order' with order_id = @order_num\n\n" +
            "Features:\n" +
            "• Call stored procedures and functions\n" +
            "• Parameter passing and output parameters\n" +
            "• Dynamic SQL execution\n\n" +
            "Returns: Result set or output parameters from procedure");
            
        MOCA_COMMANDS.put("if", 
            "IF Command\n\n" +
            "Purpose: Conditional execution of MOCA commands\n\n" +
            "Syntax:\n" +
            "  if (<condition>) {\n" +
            "    <commands>\n" +
            "  } [else {\n" +
            "    <commands>\n" +
            "  }]\n\n" +
            "Examples:\n" +
            "  if (@qty > 0) {\n" +
            "    list invlod where qty = @qty\n" +
            "  }\n\n" +
            "Features:\n" +
            "• Boolean expressions and comparisons\n" +
            "• Nested IF statements\n" +
            "• ELSE clause support\n\n" +
            "Returns: Results from executed commands within the block");
            
        MOCA_COMMANDS.put("while", 
            "WHILE Command\n\n" +
            "Purpose: Loop execution while condition is true\n\n" +
            "Syntax:\n" +
            "  while (<condition>) {\n" +
            "    <commands>\n" +
            "  }\n\n" +
            "Examples:\n" +
            "  while (@counter < 10) {\n" +
            "    set @counter = @counter + 1\n" +
            "  }\n\n" +
            "Features:\n" +
            "• Boolean condition evaluation\n" +
            "• Nested loops support\n" +
            "• Break and continue statements\n\n" +
            "Returns: Results from commands executed within the loop");
    }
    
    private Icon createIcon(String text) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                FontMetrics fm = g2d.getFontMetrics();
                int stringWidth = fm.stringWidth(text);
                int stringHeight = fm.getAscent();
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(text, x + (getIconWidth() - stringWidth) / 2, 
                              y + (getIconHeight() + stringHeight) / 2 - 2);
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 16; }
            
            @Override
            public int getIconHeight() { return 16; }
        };
    }
}
