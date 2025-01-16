/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 17, 2025
* Description: This class sets up the main game window for Top-Down Duel.
* It initializes and embeds the GamePanel constructor, configures the frame's properties,
* and ensures the window is displayed at the center of the screen.
*/

import java.awt.*;
import javax.swing.*;
public class GameFrame extends JFrame {
   // Constructor to initialize the game frame
   public GameFrame() {
       // Create an instance of GamePanel to handle the game logic and visuals
       GamePanel panel = new GamePanel();
       // Add the GamePanel to the frame
       add(panel);
       // Set the title of the game window
       setTitle("Top-Down Duel");
       // Prevent the user from resizing the game window
       setResizable(false);
       // Set the background colour of the frame
       setBackground(Color.white);
       // Ensure the application closes when the window is closed
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       // Automatically adjust the frame size based on its contents (GamePanel)
       pack();
       // Make the frame visible on the screen
       setVisible(true);
       // Position the frame at the center of the screen
       setLocationRelativeTo(null);
   }
}