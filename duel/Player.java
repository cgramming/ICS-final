/*
* Swapnil Kabir and Syed Bazif Shah
* Date: December 13, 2024
* Description: Player class representing a player in Top-Down Duel.
* Extends Rectangle for position and dimension tracking.
* Supports directional movement and shooting interactions.
* Includes player-specific image rendering.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Player extends Rectangle {
   // Movement and screen-related constants
   private final int SPEED = 5;
   private final int SCREEN_HEIGHT;
   private final int TOP_MARGIN; // Top margin constant
  
   // Movement and state variables
   private int yVelocity;
   private boolean isMoving;
   private int movementDirection;
   private boolean hasGun = true;
   private BufferedImage playerImageWithGun;
   private BufferedImage playerImageNoGun;
   private boolean isLeftPlayer;
   private static final long SHOOT_PAUSE_DURATION = 250;
   private long lastShootTime;
   
   public Player(int x, int y, int playerWidth, int playerHeight,
                 int screenHeight, boolean hasGun) {
       super(x, y, playerWidth, playerHeight);
       this.SCREEN_HEIGHT = screenHeight;
       this.TOP_MARGIN = (int)(screenHeight * 0.1); // 10% from top
       this.hasGun = hasGun;
       this.movementDirection = 1;
       this.isMoving = true;
       this.yVelocity = SPEED;
       
       // Determine if this is the left or right player
       this.isLeftPlayer = x < screenHeight / 2;
       
       // Ensure initial position respects top margin
       if (y < TOP_MARGIN) {
           y = TOP_MARGIN;
       }
       
       loadPlayerImages();
   }
   
   // Loads the appropriate player images based on player position
   private void loadPlayerImages() {
       try {
           // Load images based on player's position
           String imageNameWithGun = isLeftPlayer ? "playerLeft.png" : "playerRight.png";
           String imageNameNoGun = isLeftPlayer ? "playerLeftNoGun.png" : "playerRightNoGun.png";
           playerImageWithGun = ImageIO.read(getClass().getResourceAsStream(imageNameWithGun));
           playerImageNoGun = ImageIO.read(getClass().getResourceAsStream(imageNameNoGun));
       } catch (IOException | IllegalArgumentException e) {
           System.err.println("Error loading player images: " + e.getMessage());
           // Fallback to null if images fail to load
           playerImageWithGun = null;
           playerImageNoGun = null;
       }
   }
   
   /*
    * Sets the vertical direction of player movement
    * Positive values move down, negative values move up
    */
   public void setYDirection(int direction) {
       // Normalize direction to -1, 0, or 1
       if (direction > 0) movementDirection = 1;
       if (direction < 0) movementDirection = -1;
       if (direction == 0) movementDirection = movementDirection *-1;
       // Update movement direction and velocity
       yVelocity = direction * SPEED;
   }
   public int getYDirection() {
        // Return the current Y direction
        return movementDirection;
    }
   public void reverseDirection() {
        // Reverse the current Y direction
        setYDirection(-getYDirection());
    }
   
   /*
    * Moves the player vertically within screen boundaries
    * Respects movement state, direction, and screen limits
    */
   public void move() {
       if (!isMoving) {
           return;
       }
       
       y += yVelocity;
       
       // Boundary checks with direction reversal
       if (y < TOP_MARGIN) { // Top boundary at top 10% of screen height
           y = TOP_MARGIN;
           movementDirection *= -1;
           yVelocity = movementDirection * SPEED;
       } else if (y > SCREEN_HEIGHT - height) {  // Bottom boundary at screen edge
           y = SCREEN_HEIGHT - height;
           movementDirection *= -1;
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
           hasGun = false;
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
    if (currentTime - lastShootTime >= SHOOT_PAUSE_DURATION) {
        isMoving = true;
        yVelocity = movementDirection * SPEED;
        }
    }
   
   // Draws the player on the screen
   public void draw(Graphics g) {
       BufferedImage currentImage = hasGun ? playerImageWithGun : playerImageNoGun;
       
       if (currentImage != null) {
           // Draw the loaded image with custom width scaling
           // Increase width to 1.5 times the original height
           int scaledWidth = (int)(height);
           int xOffset = (width - scaledWidth) / 2; // Center the image
           g.drawImage(currentImage, x + xOffset, y, scaledWidth, height, null);
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
   
   public boolean hasGun() {
       return hasGun;
   }
}