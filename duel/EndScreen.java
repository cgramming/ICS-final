/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 17, 2025
* Description: Manages the end game screen and functionality
*/

import java.awt.*;
import javax.swing.*;

public class EndScreen extends JPanel {
	
    private GamePanel gamePanel;
    private JButton resetButton;
    private JButton mainMenuButton;
    private JButton quitButton;
    private String winnerText;
    
    public EndScreen(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.setLayout(null);
        this.setOpaque(false);
        
        initializeButtons();
        setVisible(false);
    }
    
    private void initializeButtons() {
        // Common button settings
        int startY = 300;
        int spacing = 70;
        
        // Initialize buttons
        resetButton = createButton("Reset", startY);
        mainMenuButton = createButton("Main Menu", startY + spacing);
        quitButton = createButton("Quit", startY + spacing * 2);
        
        // Add action listeners
        resetButton.addActionListener(e -> {
            gamePanel.resetGame();
            setVisible(false);
        });
        mainMenuButton.addActionListener(e -> {
            gamePanel.returnToMainMenu();
            setVisible(false);
        });
        quitButton.addActionListener(e -> System.exit(0));
        
        // Add buttons to panel
        add(resetButton);
        add(mainMenuButton);
        add(quitButton);
    }
    
    private JButton createButton(String text, int y) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBounds((GamePanel.GAME_WIDTH - 200) / 2, y, 200, 50);
        return button;
    }
    
    public void showEndScreen(String winner) {
        this.winnerText = winner + " Wins!";
        setVisible(true);
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw translucent black background
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw winner text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(winnerText);
        g.drawString(winnerText, (getWidth() - textWidth) / 2, 200);
    }
}
