/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 17, 2025
* Description: Manages the pause menu overlay and functionality
*/

import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class PauseMenu extends JPanel {
    private GamePanel gamePanel;
    private Image pauseButtonImage;
    private Rectangle pauseButtonBounds;
    private JButton resumeButton;
    private JButton resetButton;
    private JButton mainMenuButton;
    private JButton quitButton;
    private boolean isPaused;
    
    public PauseMenu(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.setLayout(null);
        this.setOpaque(false);
        isPaused = false;
        
        // Load pause button image
        try {
            pauseButtonImage = ImageIO.read(getClass().getResourceAsStream("Pause.png"));
            pauseButtonBounds = new Rectangle(GamePanel.GAME_WIDTH - 60, 10, 50, 50);
        } catch (Exception e) {
            System.err.println("Error loading pause button image: " + e.getMessage());
        }
        
        initializeButtons();
        setButtonsVisible(false);
        
        // Add mouse listener for pause button
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (pauseButtonBounds.contains(e.getPoint())) {
                    togglePause();
                }
            }
        });
    }
    
    private void initializeButtons() {
        // Common button settings
        int startY = 200;
        int spacing = 70;
        
        // Initialize buttons
        resumeButton = createButton("Resume", startY);
        resetButton = createButton("Reset", startY + spacing);
        mainMenuButton = createButton("Main Menu", startY + spacing * 2);
        quitButton = createButton("Quit", startY + spacing * 3);
        
        // Add action listeners
        resumeButton.addActionListener(e -> togglePause());
        resetButton.addActionListener(e -> {
            gamePanel.resetGame();
            togglePause();
        });
        mainMenuButton.addActionListener(e -> {
            gamePanel.returnToMainMenu();
            togglePause();
        });
        quitButton.addActionListener(e -> System.exit(0));
        
        // Add buttons to panel
        add(resumeButton);
        add(resetButton);
        add(mainMenuButton);
        add(quitButton);
    }
    //Creates a button template
    
    private JButton createButton(String text, int y) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBounds((GamePanel.GAME_WIDTH - 200) / 2, y, 200, 50);
        return button;
    }
    //toggles pause on or off
    public void togglePause() {
        isPaused = !isPaused;
        setButtonsVisible(isPaused);
        gamePanel.setPaused(isPaused);
        repaint();
    }
    //makes everything visable
    
    private void setButtonsVisible(boolean visible) {
        resumeButton.setVisible(visible);
        resetButton.setVisible(visible);
        mainMenuButton.setVisible(visible);
        quitButton.setVisible(visible);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw pause button
        if (pauseButtonImage != null) {
            g.drawImage(pauseButtonImage, pauseButtonBounds.x, pauseButtonBounds.y, 
                       pauseButtonBounds.width, pauseButtonBounds.height, null);
        }
        
        // Draw translucent black background when paused
        if (isPaused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    
    public boolean isPaused() {
        return isPaused;
    }
}