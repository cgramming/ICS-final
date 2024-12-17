/*
 * Swapnil Kabir and Syed Bazif Shah
 * Date: December 16, 2024
 * Description: Menu class for Top-Down Duel game, managing start screen, 
 * tutorial, and game initialization.
 */
package duel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Menu extends JPanel {
    // Menu components
    private String title;
    private JButton start;
    private JButton tutorial;
    private JButton back;
    
    // Reference to GamePanel for game control
    private GamePanel gamePanel;
    
    // Tutorial text
    private static final String TUTORIAL_TEXT = "GAME CONTROLS:\n\n" +
        "Left Player:\n" +
        "W - Shoot/Change Direction\n\n" +
        "Right Player:\n" +
        "Up Arrow - Shoot/Change Direction\n\n" +
        "Avoid enemy bullets and shoot your opponent!";
    
    // Main menu panel
    private JPanel mainMenuPanel;
    
    // Tutorial panel
    private JPanel tutorialPanel;
    
    // Constructor for Menu
    public Menu(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        
        // Set layout and style
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Initialize main menu panel
        mainMenuPanel = createMainMenuPanel();
        
        // Initialize tutorial panel
        tutorialPanel = createTutorialPanel();
        
        // Add main menu as default
        add(mainMenuPanel, BorderLayout.CENTER);
    }
    
    // Creates the main menu panel with start and tutorial buttons
    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title label
        JLabel titleLabel = new JLabel("TOP-DOWN DUEL");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Start button
        start = new JButton("START");
        start.setFont(new Font("Arial", Font.BOLD, 24));
        start.addActionListener(e -> buttonPressed(ButtonType.START));
        gbc.gridy = 1;
        panel.add(start, gbc);
        
        // Tutorial button
        tutorial = new JButton("TUTORIAL");
        tutorial.setFont(new Font("Arial", Font.BOLD, 24));
        tutorial.addActionListener(e -> buttonPressed(ButtonType.TUTORIAL));
        gbc.gridy = 2;
        panel.add(tutorial, gbc);
        
        return panel;
    }
    
    // Creates the tutorial panel with back button and instructions
    private JPanel createTutorialPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Tutorial text area
        JTextArea tutorialTextArea = new JTextArea(TUTORIAL_TEXT);
        tutorialTextArea.setEditable(false);
        tutorialTextArea.setFont(new Font("Arial", Font.PLAIN, 18));
        tutorialTextArea.setBackground(Color.WHITE);
        tutorialTextArea.setLineWrap(true);
        tutorialTextArea.setWrapStyleWord(true);
        
        // Back button
        back = new JButton("BACK");
        back.setFont(new Font("Arial", Font.BOLD, 24));
        back.addActionListener(e -> buttonPressed(ButtonType.BACK));
        
        // Add components to panel
        panel.add(tutorialTextArea, BorderLayout.CENTER);
        panel.add(back, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Enum to define button types for easier handling
    private enum ButtonType {
        START, TUTORIAL, BACK
    }
    
    // Handles button press events
    private void buttonPressed(ButtonType buttonType) {
        switch (buttonType) {
            case START:
                // Start the game
                gamePanel.startGame();
                break;
            case TUTORIAL:
                // Switch to tutorial panel
                removeAll();
                add(tutorialPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
                break;
            case BACK:
                // Return to main menu
                removeAll();
                add(mainMenuPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
                break;
        }
    }
}
