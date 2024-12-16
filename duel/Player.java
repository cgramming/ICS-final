/*
* Swapnil Kabir and Syed Bazif Shah
* Date: December 13, 2024
* Description: Player class representing a player in Top-Down Duel.
* Extends Rectangle for position and dimension tracking.
* Supports directional movement and shooting interactions.
test
*/
package duel;
import java.awt.*;
public class Player extends Rectangle {
   // Movement and screen-related constants
   private final int SPEED = 5; // Base movement speed
   private final int SCREEN_HEIGHT; // Height of game screen
  
   // Movement and state variables
   private int yVelocity; // Current vertical velocity
   private boolean isMoving; // Current movement state
   private int movementDirection; // Current movement direction (1 for down, -1 for up)
   private boolean hasGun; // Whether player currently possesses a gun
  
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
       if (!isMoving) return; // Stop movement if paused
       y += yVelocity;
       // Boundary checks with direction reversal
       if (y < 0) {
           y = 0;
           movementDirection *= -1; // Reverse direction at top
           yVelocity = movementDirection * SPEED;
       } else if (y > SCREEN_HEIGHT - height) {
           y = SCREEN_HEIGHT - height;
           movementDirection *= -1; // Reverse direction at bottom
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
       g.setColor(Color.BLACK);
       g.fillRect(x, y, width, height);
   }
   // Getters and setters
   public void setHasGun(boolean hasGun) {
       this.hasGun = hasGun;
   }
   public boolean hasGun() {
       return hasGun;
   }
}
