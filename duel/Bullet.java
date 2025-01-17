/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 17, 2025
* Description: Bullet class representing projectiles shot by players,
* with directional movement and player-specific image rendering.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Bullet extends Rectangle {
    private int xVelocity;
    private int yVelocity;
    private final int BASE_SPEED = 10;
    private final double MIN_HORIZONTAL_RATIO = 0.2; // Minimum horizontal component of velocity, stops bullets getting stuck going up and down
    private BufferedImage bulletImage;
    private boolean isFromLeftPlayer;
    private double rotation;
    private int previousX;
    private int previousY;
    private final int TOP_MARGIN; // Top margin constant only
    private ArrayList<Bullet> splitBullets; // List of split bullets, used when Bomb powerup is activated
    private double scale = 1.0; // Scaler for bullet size, used by Big Bullet powerup
    private boolean hasFreezeEffect = false; // Used for Freeze powerup
    private Player playerToUnfreeze; // Track player to unfreeze

    // Constructor to initialize bullet with position, size, and player origin
    public Bullet(int x, int y, int width, int height, boolean isFromLeftPlayer) {
        super(x, y, width, height);
        this.isFromLeftPlayer = isFromLeftPlayer;
        this.TOP_MARGIN = (int)(GamePanel.GAME_HEIGHT * 0.1); // 10% from top
        loadBulletImage();

        // Set initial velocities
        xVelocity = isFromLeftPlayer ? BASE_SPEED : -BASE_SPEED;
        yVelocity = 0;
        
        // Set initial rotation based on player
        rotation = isFromLeftPlayer ? 0 : Math.PI;
        
        // Initialize previous position
        previousX = x;
        previousY = y;
        
        // Ensure initial position respects top margin
        if (y < TOP_MARGIN) {
            y = TOP_MARGIN;
        }
    }

    // Loads and resizes the bullet image from resources
    private void loadBulletImage() {
        try {
            bulletImage = ImageIO.read(getClass().getResourceAsStream("bullet.png"));
            bulletImage = resizeImage(bulletImage, width, height);
        } catch (IOException e) {
            System.err.println("Error loading bullet image: " + e.getMessage());
            bulletImage = null;
        }
    }

    // Resizes an image to specified dimensions while maintaining aspect ratio
    private BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }

    // Updates bullet position and handles bouncing off screen boundaries
    public void move() {
    // Store current position before moving
    previousX = x;
    previousY = y;
    
    // Update position
    x += xVelocity;
    y += yVelocity;

    // Bounce off top margin and bottom screen edge
    if (y < TOP_MARGIN || y > GamePanel.GAME_HEIGHT - height) {
        // When bouncing, maintain horizontal velocity while reversing vertical
        setDirection(
            (double)xVelocity / BASE_SPEED,
            -(double)yVelocity / BASE_SPEED
        );
        
        // Adjust position to prevent sticking
        if (y < TOP_MARGIN) y = TOP_MARGIN;
        if (y > GamePanel.GAME_HEIGHT - height) y = GamePanel.GAME_HEIGHT - height;
    }
    
    // Move split bullets independently
    if (splitBullets != null) {
        for (Bullet splitBullet : splitBullets) {
            splitBullet.move(); // Each split bullet handles its own movement
        }
    }
}

    // Sets the bullet's direction while ensuring minimum horizontal movement
    public void setDirection(double dx, double dy) {
        // Ensure minimum horizontal velocity component
        if (Math.abs(dx) < MIN_HORIZONTAL_RATIO) {
            // Maintain direction but adjust magnitude
            double currentDirection = Math.signum(dx);
            if (currentDirection == 0) {
                // If dx is 0, use the initial direction based on player
                currentDirection = isFromLeftPlayer ? 1 : -1;
            }
            dx = currentDirection * MIN_HORIZONTAL_RATIO;
            
            // Adjust vertical component to maintain proper speed
            double maxVerticalComponent = Math.sqrt(1 - MIN_HORIZONTAL_RATIO * MIN_HORIZONTAL_RATIO);
            dy = Math.signum(dy) * Math.min(Math.abs(dy), maxVerticalComponent);
        }

        // Normalize the direction vector
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        dx = dx / magnitude;
        dy = dy / magnitude;

        // Calculate new velocity based on normalized direction and base speed
        xVelocity = (int)(dx * BASE_SPEED);
        yVelocity = (int)(dy * BASE_SPEED);
        
        // Update rotation to match new direction
        updateRotation();
    }

    // Handles bullet collision and bouncing off obstacles
    public Boolean bulletBounce(Point obstacleCenter, Point obstaclePosition, Obstacle obstacle, Bullet bullet){
        Point bulletCenter = new Point(
                x + width / 2,
                y + height / 2
            );
            Point bulletPrevCenter = new Point(
                getPreviousX() + width / 2,
                getPreviousY() + height / 2
            );

            if (obstacle.lineIntersectsCircle(obstacleCenter, bulletPrevCenter, bulletCenter)) {
                // Calculate reflection vector
                double dx = bulletCenter.x - obstacleCenter.x;
                double dy = bulletCenter.y - obstacleCenter.y;
                double length = Math.sqrt(dx * dx + dy * dy);
                
                if (length > 0) {
                    // Normalize the vector
                    dx /= length;
                    dy /= length;

                    System.out.println(dx);
                    if(dx <= 0 && dx >= -0.1) dx = -0.1;
                    else if (dx > 0 && dx <= 0.1) dx = 0.1;
                    
                    // Set new bullet direction based on reflection
                    bullet.setDirection(dx, dy);
                }
                
                obstacle.breakObstacle(obstaclePosition);
                return true;
            }
            return false;
    }

    // Updates bullet rotation based on current velocity
    private void updateRotation() {
        // Calculate rotation based on velocity vector
        rotation = Math.atan2(yVelocity, xVelocity);
    }

    // Create split bullets for Bomb powerup
    public void createSplitBullets(double angle1, double angle2) {
    splitBullets = new ArrayList<>();
    
    // Create new bullets with same properties as parent
    Bullet bullet1 = new Bullet(x, y, width, height, isFromLeftPlayer);
    bullet1.bulletImage = this.bulletImage; // Share the same bullet image
    bullet1.setDirection(Math.cos(angle1), Math.sin(angle1));
    
    Bullet bullet2 = new Bullet(x, y, width, height, isFromLeftPlayer);
    bullet2.bulletImage = this.bulletImage; // Share the same bullet image
    bullet2.setDirection(Math.cos(angle2), Math.sin(angle2));
    
    splitBullets.add(bullet1);
    splitBullets.add(bullet2);
}

    // Resize bullet for BigBullet powerup
    public void resize(double scaleFactor) {
        scale *= scaleFactor;
        width *= scaleFactor;
        height *= scaleFactor;
        if (bulletImage != null) {
            bulletImage = resizeImage(bulletImage, width, height);
        }
    }

    // Returns the previous X position of the bullet
    public int getPreviousX() {
        return previousX;
    }

    // Returns the previous Y position of the bullet
    public int getPreviousY() {
        return previousY;
    }
    
    // Returns the current X velocity of the bullet
    public int getxVelocity() {
        return xVelocity;
    }
    
    // Returns the current Y velocity of the bullet
    public int getyVelocity() {
        return yVelocity;
    }

    // Checks if bullet is outside the game screen boundaries
    public boolean isOutOfBounds(int screenWidth) {
        return x < -width || x > screenWidth;
    }

    // Checks if bullet intersects with a player
    public boolean collidesWith(Player player) {
        return this.intersects(player);
    }

    // Returns the list of split bullets created by Bomb powerup
    public ArrayList<Bullet> getSplitBullets() {
        return splitBullets;
    }
    
    // Checks if bullet has any active split bullets
    public boolean hasSplitBullets() {
        return splitBullets != null && !splitBullets.isEmpty();
    }
    
    // Checks collision for both main bullet and any split bullets
    public boolean collidesWithAny(Player player) {
        // Check main bullet collision
        if (this.intersects(player)) {
            return true;
        }
        
        // Check split bullets collisions if they exist
        if (hasSplitBullets()) {
            for (Bullet splitBullet : splitBullets) {
                if (splitBullet.intersects(player)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // Checks if main bullet or any split bullets are out of bounds
    public boolean isAnyBulletOutOfBounds(int screenWidth) {
        // Check main bullet
        if (isOutOfBounds(screenWidth)) {
            return true;
        }
        
        // Check split bullets if they exist
        if (hasSplitBullets()) {
            for (Bullet splitBullet : splitBullets) {
                if (splitBullet.isOutOfBounds(screenWidth)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Returns the collision bounds of the bullet
    public Rectangle getBounds() {
        return this;
    }

    // Returns whether bullet was shot by left player
    public boolean isFromLeftPlayer() {
        return isFromLeftPlayer;
    }

    // Checks if bullet has freeze effect active
    public boolean hasFreezeEffect() {
      return hasFreezeEffect;
    }

    // Sets freeze effect and freezes target player
    public void setFreezeEffect(boolean hasFreezeEffect, Player playerToFreeze) {
        this.hasFreezeEffect = hasFreezeEffect;
        if (hasFreezeEffect) {
            this.playerToUnfreeze = playerToFreeze;
            playerToFreeze.freeze();
        }
    }

    // Returns the player that needs to be unfrozen
    public Player getPlayerToUnfreeze() {
        return playerToUnfreeze;
    }

    // Renders the bullet and any split bullets to the screen
    public void draw(Graphics g) {
        if (bulletImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(x + width/2, y + height/2);
            g2d.rotate(rotation);
            g2d.translate(-width/2, -height/2);
            g2d.drawImage(bulletImage, 0, 0, width, height, null);
            g2d.dispose();
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, width, height);
        }
        
        // Draw split bullets if they exist
        if (splitBullets != null) {
            for (Bullet bullet : splitBullets) {
                bullet.draw(g);
            }
        }
    }
}