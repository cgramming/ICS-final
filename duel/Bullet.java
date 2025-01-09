/*
* Swapnil Kabir and Syed Bazif Shah
* Date: December 13, 2024
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
    private BufferedImage bulletImage;
    private boolean isFromLeftPlayer;
    private double rotation;
    private int previousX;
    private int previousY;
    private final int TOP_MARGIN; // Top margin constant only
    private ArrayList<Bullet> splitBullets; // List of split bullets, used when Bomb powerup is activated
    private double scale = 1.0; // Scaler for bullet size, used by Big Bullet powerup

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

    private void loadBulletImage() {
        try {
            bulletImage = ImageIO.read(getClass().getResourceAsStream("bullet.png"));
            bulletImage = resizeImage(bulletImage, width, height);
        } catch (IOException e) {
            System.err.println("Error loading bullet image: " + e.getMessage());
            bulletImage = null;
        }
    }

    private BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }

    public void move() {
        // Store current position before moving
        previousX = x;
        previousY = y;
        
        // Update position
        x += xVelocity;
        y += yVelocity;

        // Bounce off top margin and bottom screen edge
        if (y < TOP_MARGIN || y > GamePanel.GAME_HEIGHT - height) {
            yVelocity = -yVelocity;
            if (y < TOP_MARGIN) y = TOP_MARGIN;
            if (y > GamePanel.GAME_HEIGHT - height) y = GamePanel.GAME_HEIGHT - height;
            updateRotation();
        }
        
        // Move split bullets if they exist
        if (splitBullets != null) {
            for (Bullet bullet : splitBullets) {
                bullet.move();
            }
        }
    }

    public void setDirection(double dx, double dy) {
        // Calculate new velocity based on normalized direction and base speed
        double speed = Math.sqrt(BASE_SPEED * BASE_SPEED);
        xVelocity = (int)(dx * speed);
        yVelocity = (int)(dy * speed);
        
        // Update rotation to match new direction
        updateRotation();
    }

    private void updateRotation() {
        // Calculate rotation based on velocity vector
        rotation = Math.atan2(yVelocity, xVelocity);
    }

    // Create split bullets for Bomb powerup
    public void createSplitBullets(double angle1, double angle2) {
        splitBullets = new ArrayList<>();
        
        // Create two new bullets at the specified angles
        Bullet bullet1 = new Bullet(x, y, width, height, isFromLeftPlayer);
        bullet1.setDirection(Math.cos(angle1), Math.sin(angle1));
        
        Bullet bullet2 = new Bullet(x, y, width, height, isFromLeftPlayer);
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

    // Getter methods for previous position
    public int getPreviousX() {
        return previousX;
    }

    public int getPreviousY() {
        return previousY;
    }
    
    // Getter methods for velocity
    public int getxVelocity() {
        return xVelocity;
    }
    
    public int getyVelocity() {
        return yVelocity;
    }

    public boolean isOutOfBounds(int screenWidth) {
        return x < 0 || x > screenWidth;
    }

    public boolean collidesWith(Player player) {
        return this.intersects(player);
    }

    // Get list of split bullets
    public ArrayList<Bullet> getSplitBullets() {
        return splitBullets;
    }
    
    // Check if this bullet has split bullets
    public boolean hasSplitBullets() {
        return splitBullets != null && !splitBullets.isEmpty();
    }
    
    // Check collisions for both main bullet and split bullets
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
    
    // Check if any part of the bullet (main or split) is out of bounds
    public boolean isAnyBulletOutOfBounds(int screenWidth) {
        // Check main bullet
        if (isOutOfBounds(screenWidth)) {
            return true;
        }
        
        // Check split bullets
        if (hasSplitBullets()) {
            for (Bullet splitBullet : splitBullets) {
                if (splitBullet.isOutOfBounds(screenWidth)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Rectangle getBounds() {
        return this;
    }

    public boolean isFromLeftPlayer() {
        return isFromLeftPlayer;
    }

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