package com.github.joshdevyn.mocaide.util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced panel management utility for dynamic UI with intelligent snapping,
 * dockable panels, and floating windows
 */
public class PanelManager {
    
    private static final int SNAP_THRESHOLD = 10;
    private static final int MIN_PANEL_SIZE = 100;
    private static final List<FloatingPanel> floatingPanels = new ArrayList<>();
    
    /**
     * Enhanced JSplitPane with intelligent snapping
     */
    public static class SmartSplitPane extends JSplitPane {
        private final int[] snapPositions;
        private boolean snapEnabled = true;
        
        public SmartSplitPane(int orientation, Component left, Component right, int... snapPositions) {
            super(orientation, true, left, right);
            this.snapPositions = snapPositions;
            setupSnapping();
        }
        
        private void setupSnapping() {
            // Add property change listener for divider location
            addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
                if (!snapEnabled) return;
                
                int location = getDividerLocation();
                int snapPosition = findNearestSnapPosition(location);
                
                if (snapPosition != -1 && Math.abs(location - snapPosition) <= SNAP_THRESHOLD) {
                    SwingUtilities.invokeLater(() -> {
                        snapEnabled = false;
                        setDividerLocation(snapPosition);
                        snapEnabled = true;
                    });
                }
            });
            
            // Set minimum sizes to prevent collapsing
            if (getLeftComponent() != null) {
                getLeftComponent().setMinimumSize(new Dimension(MIN_PANEL_SIZE, MIN_PANEL_SIZE));
            }
            if (getRightComponent() != null) {
                getRightComponent().setMinimumSize(new Dimension(MIN_PANEL_SIZE, MIN_PANEL_SIZE));
            }
        }
        
        private int findNearestSnapPosition(int location) {
            for (int snapPos : snapPositions) {
                if (Math.abs(location - snapPos) <= SNAP_THRESHOLD) {
                    return snapPos;
                }
            }
            return -1;
        }
        
        public void addSnapPosition(int position) {
            int[] newPositions = new int[snapPositions.length + 1];
            System.arraycopy(snapPositions, 0, newPositions, 0, snapPositions.length);
            newPositions[snapPositions.length] = position;
        }
    }
    
    /**
     * Dockable panel that can be detached into a floating window
     */
    public static class DockablePanel extends JPanel {
        private final String title;
        private final JComponent content;
        private FloatingPanel floatingWindow;
        private Container originalParent;
        private Object originalConstraints;
        private boolean isDocked = true;
        private JButton floatButton;
        
        public DockablePanel(String title, JComponent content) {
            this.title = title;
            this.content = content;
            setupPanel();
        }
        
        private void setupPanel() {
            setLayout(new BorderLayout());
            
            // Create title bar with float/dock button
            JPanel titleBar = new JPanel(new BorderLayout());
            titleBar.setBorder(BorderFactory.createRaisedBevelBorder());
            titleBar.setBackground(new Color(240, 240, 240));
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 11f));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            
            floatButton = new JButton("◳");
            floatButton.setPreferredSize(new Dimension(20, 20));
            floatButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            floatButton.setToolTipText("Float/Dock Panel");
            floatButton.addActionListener(e -> toggleFloat());
            
            titleBar.add(titleLabel, BorderLayout.CENTER);
            titleBar.add(floatButton, BorderLayout.EAST);
            
            add(titleBar, BorderLayout.NORTH);
            add(content, BorderLayout.CENTER);
        }
        
        public void toggleFloat() {
            if (isDocked) {
                undock();
            } else {
                dock();
            }
        }
        
        private void undock() {
            // Store original parent information
            originalParent = getParent();
            if (originalParent instanceof JComponent) {
                java.awt.LayoutManager layout = originalParent.getLayout();
                if (layout instanceof BorderLayout) {
                    BorderLayout bl = (BorderLayout) layout;
                    originalConstraints = bl.getConstraints(this);
                }
            }
            
            // Remove from parent
            originalParent.remove(this);
            originalParent.revalidate();
            originalParent.repaint();
            
            // Create floating window
            Window parentWindow = SwingUtilities.getWindowAncestor(originalParent);
            floatingWindow = new FloatingPanel(parentWindow, title, this);
            floatingPanels.add(floatingWindow);
            
            floatButton.setText("◱");
            floatButton.setToolTipText("Dock Panel");
            isDocked = false;
            
            floatingWindow.setVisible(true);
        }
        
        private void dock() {
            if (floatingWindow != null) {
                floatingWindow.setVisible(false);
                floatingWindow.dispose();
                floatingPanels.remove(floatingWindow);
                floatingWindow = null;
            }
            
            // Return to original parent
            if (originalParent != null) {
                originalParent.add(this, originalConstraints);
                originalParent.revalidate();
                originalParent.repaint();
            }
            
            floatButton.setText("◳");
            floatButton.setToolTipText("Float Panel");
            isDocked = true;
        }
        
        public boolean isDocked() {
            return isDocked;
        }
        
        public String getTitle() {
            return title;
        }
    }
    
    /**
     * Floating window for undocked panels
     */
    public static class FloatingPanel extends JDialog {
        private final DockablePanel dockablePanel;
        
        public FloatingPanel(Window parent, String title, DockablePanel panel) {
            super(parent, title, ModalityType.MODELESS);
            this.dockablePanel = panel;
            setupFloatingWindow();
        }
        
        private void setupFloatingWindow() {
            setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            setLayout(new BorderLayout());
            
            // Remove the panel's title bar when floating (we use the window title)
            Component titleBar = dockablePanel.getComponent(0);
            dockablePanel.remove(titleBar);
            
            add(dockablePanel, BorderLayout.CENTER);
            
            // Handle window closing
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    dockablePanel.dock();
                }
            });
            
            // Set default size and position
            setSize(400, 300);
            setLocationRelativeTo(getParent());
        }
    }
    
    /**
     * Panel maximization utility
     */
    public static class MaximizablePanel extends JPanel {
        private boolean isMaximized = false;
        private Container originalParent;
        private Component[] originalSiblings;
        private java.awt.LayoutManager originalLayout;
        private JButton maximizeButton;
        
        public MaximizablePanel(JComponent content, String title) {
            setLayout(new BorderLayout());
            
            // Create title bar
            JPanel titleBar = new JPanel(new BorderLayout());
            titleBar.setBorder(BorderFactory.createRaisedBevelBorder());
            titleBar.setBackground(new Color(245, 245, 245));
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 11f));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            
            maximizeButton = new JButton("□");
            maximizeButton.setPreferredSize(new Dimension(20, 20));
            maximizeButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            maximizeButton.setToolTipText("Maximize/Restore Panel");
            maximizeButton.addActionListener(e -> toggleMaximize());
            
            titleBar.add(titleLabel, BorderLayout.CENTER);
            titleBar.add(maximizeButton, BorderLayout.EAST);
            
            add(titleBar, BorderLayout.NORTH);
            add(content, BorderLayout.CENTER);
        }
        
        public void toggleMaximize() {
            if (isMaximized) {
                restore();
            } else {
                maximize();
            }
        }
        
        private void maximize() {
            originalParent = getParent();
            originalLayout = originalParent.getLayout();
            originalSiblings = originalParent.getComponents();
            
            // Hide all other components
            for (Component comp : originalSiblings) {
                if (comp != this) {
                    comp.setVisible(false);
                }
            }
            
            // Set this panel to fill the parent
            originalParent.setLayout(new BorderLayout());
            originalParent.removeAll();
            originalParent.add(this, BorderLayout.CENTER);
            
            maximizeButton.setText("▣");
            maximizeButton.setToolTipText("Restore Panel");
            isMaximized = true;
            
            originalParent.revalidate();
            originalParent.repaint();
        }
        
        private void restore() {
            // Restore original layout
            originalParent.removeAll();
            originalParent.setLayout(originalLayout);
            
            // Restore all components
            for (Component comp : originalSiblings) {
                originalParent.add(comp);
                comp.setVisible(true);
            }
            
            maximizeButton.setText("□");
            maximizeButton.setToolTipText("Maximize Panel");
            isMaximized = false;
            
            originalParent.revalidate();
            originalParent.repaint();
        }
    }
    
    /**
     * Create a smart split pane with snapping
     */
    public static SmartSplitPane createSmartSplitPane(int orientation, Component left, Component right, double resizeWeight) {
        SmartSplitPane splitPane = new SmartSplitPane(orientation, left, right, 200, 400, 600, 800);
        splitPane.setResizeWeight(resizeWeight);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        return splitPane;
    }
    
    /**
     * Create a dockable panel
     */
    public static DockablePanel createDockablePanel(String title, JComponent content) {
        return new DockablePanel(title, content);
    }
    
    /**
     * Create a maximizable panel
     */
    public static MaximizablePanel createMaximizablePanel(String title, JComponent content) {
        return new MaximizablePanel(content, title);
    }
    
    /**
     * Close all floating panels
     */
    public static void closeAllFloatingPanels() {
        for (FloatingPanel panel : new ArrayList<>(floatingPanels)) {
            panel.dockablePanel.dock();
        }
    }
    
    /**
     * Get list of floating panels
     */
    public static List<FloatingPanel> getFloatingPanels() {
        return new ArrayList<>(floatingPanels);
    }
}
