/*
* Swapnil Kabir and Syed Bazif Shah
* Date: December 13, 2024
* Description: Bullet class representing projectiles shot by players,
* with directional movement and player-specific image rendering.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
public class Bullet extends Rectangle {
   private int xVelocity; // Bullet's speed in x direction
   private final int BASE_SPEED = 10; // Base speed of the bullet
   private BufferedImage bulletImage; // Image for the bullet
   private boolean isFromLeftPlayer; // Indicates which player shot a bullet, used to determine which bullet image to display
   /*Constructor for Bullet class*/
   public Bullet(int x, int y, int width, int height, boolean isFromLeftPlayer) {
       super(x, y, width, height);
       this.isFromLeftPlayer = isFromLeftPlayer;
       loadBulletImage();
      
       // Set initial velocity based on player
       xVelocity = isFromLeftPlayer ? BASE_SPEED : -BASE_SPEED;
   }
   // Loads the appropriate bullet image based on player
   private void loadBulletImage() {
       try {
           // Load image based on which player shot the bullet
           String imageName = isFromLeftPlayer ? "bulletRight.png" : "bulletLeft.png";
           bulletImage = ImageIO.read(getClass().getResourceAsStream(imageName));
       } catch (IOException | IllegalArgumentException e) {
           System.err.println("Error loading bullet image: " + e.getMessage());
           // Fallback to default color rendering if image fails to load
           bulletImage = null;
       }
   }
   // Moves the bullet based on its velocity
   public void move() {
       x += xVelocity;
   }
   // Draws the bullet on the game panel
   public void draw(Graphics g) {
       if (bulletImage != null) {
           // Draw the loaded image
           g.drawImage(bulletImage, x, y, width, height, null);
       } else {
           // Fallback to drawing a white rectangle if image fails
           g.setColor(Color.WHITE);
           g.fillRect(x, y, width, height);
       }
   }
   // Checks if the bullet has moved off the screen
   public boolean isOutOfBounds(int screenWidth) {
       return x < 0 || x > screenWidth;
   }
   // Checks if the bullet collides with a player
   public boolean collidesWith(Player player) {
       return this.intersects(player);
   }
}
