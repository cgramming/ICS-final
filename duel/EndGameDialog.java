/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 9, 2025
* Description: This class creates buttons and text to be shown to players at the end of a game.
*/

import java.awt.*;
import javax.swing.*;

public class EndGameDialog extends JDialog {
    public EndGameDialog(JFrame parent, String message, GamePanel gamePanel) {
        super(parent, "Game Over", true);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Winner message
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(messageLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JButton playAgainButton = new JButton("Play Again");
        playAgainButton.addActionListener(e -> {
            gamePanel.resetGame();
            dispose();
        });

        JButton exitButton = new JButton("Exit Game");
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(playAgainButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(exitButton);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(buttonPanel);

        add(panel);
        pack();
        setLocationRelativeTo(parent);
    }
}