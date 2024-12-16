/*
* Swapnil Kabir and Syed Bazif Shah
* Date: December 12, 2024
* Description: Menu class manages the game's main menu and interface screens.
*/
package duel;
import javax.swing.*;
import java.awt.*;
public class Menu extends JPanel {
   // Menu components (as specified in UML)
   public String title;
   public JButton start;
   public JButton tutorial;
   public JButton back;
   // Reference to GamePanel for state management
   private GamePanel gamePanel;
   // Constructor initializes menu components
   public Menu(GamePanel gamePanel) {
       this.gamePanel = gamePanel;
       initializeMenu();
   }
   // Initializes and sets up the menu components
   private void initializeMenu() {
       this.setLayout(new BorderLayout());
       this.setBackground(Color.WHITE);
       // Title setup
       title = "DUEL";
       JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
       titleLabel.setFont(new Font("Arial", Font.BOLD, 72));
       titleLabel.setForeground(Color.BLACK);
       // Button panel setup
       JPanel buttonPanel = new JPanel();
       buttonPanel.setBackground(Color.WHITE);
       buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));
       // Start button
       start = new JButton("START");
       start.setFont(new Font("Arial", Font.BOLD, 24));
       start.addActionListener(e -> buttonPressed());
       // Tutorial button
       tutorial = new JButton("TUTORIAL");
       tutorial.setFont(new Font("Arial", Font.BOLD, 24));
       tutorial.addActionListener(e -> tutorial());
       // Back button
       back = new JButton("BACK");
       back.setFont(new Font("Arial", Font.BOLD, 24));
       back.addActionListener(e -> back());
       back.setVisible(false);
       // Add components to panel
       buttonPanel.add(start);
       buttonPanel.add(tutorial);
       buttonPanel.add(back);
       // Add title and buttons to menu
       this.add(titleLabel, BorderLayout.NORTH);
       this.add(buttonPanel, BorderLayout.CENTER);
       // Ensure components are properly rendered
       revalidate();
       repaint();
   }
   // Handles start button press to begin game
   public void buttonPressed() {
       // Hide menu and start the game
       this.setVisible(false);
       gamePanel.setVisible(true);
       gamePanel.startGame();
   }
   // Displays the tutorial screen
   public void tutorial() {
       // Clear existing components
       removeAll();
       setLayout(new BorderLayout());
       // Create tutorial text
       JTextArea tutorialText = new JTextArea(
           "GAME CONTROLS:\n\n" +
           "Left Player:\n" +
           "W - Shoot/Change Direction\n\n" +
           "Right Player:\n" +
           "Up Arrow - Shoot/Change Direction\n\n" +
           "Avoid enemy bullets and shoot your opponent!"
       );
       tutorialText.setBackground(Color.WHITE);
       tutorialText.setForeground(Color.WHITE);
       tutorialText.setFont(new Font("Arial", Font.PLAIN, 24));
       tutorialText.setEditable(false);
       tutorialText.setLineWrap(true);
       tutorialText.setWrapStyleWord(true);
       // Add tutorial text and back button
       add(tutorialText, BorderLayout.CENTER);
       back.setVisible(true);
       add(back, BorderLayout.SOUTH);
       // Revalidate and repaint after changes
       revalidate();
       repaint();
   }
   // Returns to the main menu from tutorial or other screens
   public void back() {
       // Reinitialize menu to return to default state
       removeAll();
       initializeMenu();
       // Revalidate and repaint to ensure changes are visible
       revalidate();
       repaint();
   }
}
