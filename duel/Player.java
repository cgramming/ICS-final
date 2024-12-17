/*
* Swapnil Kabir and Syed Bazif Shah
* Date: December 13, 2024
* Description: Player class representing a player in Top-Down Duel.
* Extends Rectangle for position and dimension tracking.
* Supports directional movement and shooting interactions.
* Now includes player-specific image rendering.
*/
package duel;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
public class Player extends Rectangle {
   // Movement and screen-related constants
   private final int SPEED = 5; // Base movement speed
   private final int SCREEN_HEIGHT; // Height of game screen
  
   // Movement and state variables
   private int yVelocity; // Current vertical velocity
   private boolean isMoving; // Current movement state
   private int movementDirection; // Current movement direction (1 for down, -1 for up)
   private boolean hasGun = true; // Whether player currently possesses a gun
  
   // Image-related variables
   private BufferedImage playerImage; // Player's image
   private boolean isLeftPlayer; // Indicates which side of the screen the player is on
  
   // Shooting-related variables
   private static final long SHOOT_PAUSE_DURATION = 250; // Milliseconds to pause when shooting
   private long lastShootTime; // Timestamp of last shoot action
   
   // Constructor for Player class
   public Player(int x, int y, int playerWidth, int playerHeight,
                 int screenHeight, boolean hasGun) {
       super(x, y, playerWidth, playerHeight);
       this.SCREEN_HEIGHT = screenHeight;
       this.hasGun = hasGun;
       this.movementDirection = 1; // Default to downward movement
       this.isMoving = true; // Start in moving state
       this.yVelocity = SPEED; // Initial downward velocity
       
       // Determine if this is the left or right player
       this.isLeftPlayer = x < screenHeight / 2;
       
       // Load player-specific image
       loadPlayerImage();
   }
   
   // Loads the appropriate player image based on player position/
   private void loadPlayerImage() {
       try {
           // Load image based on player's position
           String imageName = isLeftPlayer ? "playerLeft.png" : "playerRight.png";
           playerImage = ImageIO.read(getClass().getResourceAsStream(imageName));
       } catch (IOException | IllegalArgumentException e) {
           System.err.println("Error loading player image: " + e.getMessage());
           // Fallback to null if image fails to load
           playerImage = null;
       }
   }
   
   /*
    * Sets the vertical direction of player movement
    * Positive values move down, negative values move up
    */
   public void setYDirection(int direction) {
       // Normalize direction to -1, 0, or 1
       if (direction > 0) direction = 1;
       if (direction < 0) direction = -1;
       // Update movement direction and velocity
       movementDirection = direction;
       yVelocity = direction * SPEED;
   }
   
   /*
    * Moves the player vertically within screen boundaries
    * Respects movement state, direction, and screen limits
    */
   public void move() {
       if (!isMoving){
        
        return;
       } // Stop movement if paused
       y += yVelocity;
       // Boundary checks with direction reversal
       if (y < 0) {
           y = 0;
           movementDirection *= -1; // Reverse direction at top
           yVelocity = movementDirection * SPEED;

       } else if (y > SCREEN_HEIGHT - height) {
           y = SCREEN_HEIGHT - height;
           movementDirection = movementDirection* -1; // Reverse direction at bottom
           yVelocity = movementDirection * SPEED;
           
       }
   }
   
   // Handles shoot action based on gun possession
   public boolean shoot(long currentTime) {
    
       // Check if enough time has passed since last shoot
       if (currentTime - lastShootTime < SHOOT_PAUSE_DURATION) {
        
           return false;
       }
       if (hasGun) {
           // Pause movement momentarily when shooting with a gun
           isMoving = false;
           lastShootTime = currentTime;
           return true;
       } else {
        
           // Without a gun, change movement direction
           movementDirection *= -1;
           yVelocity = movementDirection * SPEED;
           return false;
       }
   }
   
   // Resumes movement after shoot pause
   public void resumeMovement(long currentTime) {
       if (!hasGun || currentTime - lastShootTime >= SHOOT_PAUSE_DURATION) {
           isMoving = true;
           yVelocity = movementDirection * SPEED;
       }
   }
   
   // Draws the player on the screen
   public void draw(Graphics g) {
       if (playerImage != null) {
           // Draw the loaded image with custom width scaling
           // Increase width to 1.5 times the original height
           int scaledWidth = (int)(height);
           int xOffset = (width - scaledWidth) / 2; // Center the image
           g.drawImage(playerImage, x + xOffset, y, scaledWidth, height, null);
       } else {
           // Fallback to drawing a black rectangle if image fails
           g.setColor(Color.BLACK);
           g.fillRect(x, y, width, height);
       }
   }
   
   // Getters and setters
   public void setHasGun(boolean hasGun) {
       this.hasGun = hasGun;
   }
}
