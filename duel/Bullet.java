/*
* Swapnil Kabir and Syed Bazif Shah
* Date: December 13, 2024
* Description: Bullet class representing projectiles shot by players,
* with directional movement and player-specific image rendering.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Bullet extends Rectangle {
    private int xVelocity;
    private int yVelocity;
    private final int BASE_SPEED = 10;
    private BufferedImage bulletImage;
    private boolean isFromLeftPlayer;
    private double rotation; // Track bullet rotation in radians
    private int previousX; // Track previous position
    private int previousY;

    public Bullet(int x, int y, int width, int height, boolean isFromLeftPlayer) {
        super(x, y, width, height);
        this.isFromLeftPlayer = isFromLeftPlayer;
        loadBulletImage();

        // Set initial velocities
        xVelocity = isFromLeftPlayer ? BASE_SPEED : -BASE_SPEED;
        yVelocity = 0;
        
        // Set initial rotation based on player
        rotation = isFromLeftPlayer ? 0 : Math.PI;
        
        // Initialize previous position
        previousX = x;
        previousY = y;
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

        // Update rotation when bouncing off screen bounds
        if (y < 0 || y > GamePanel.GAME_HEIGHT - height) {
            yVelocity = -yVelocity;
            updateRotation();
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

    public void bounceOffObstacle(Rectangle obstacle) {
        // This method is now deprecated in favor of the new circular collision system
        // Keep it for backwards compatibility if needed
        xVelocity = -xVelocity;
        
        int obstacleCenterY = obstacle.y + obstacle.height / 2;
        int bulletCenterY = y + height / 2;
        
        int distanceFromCenter = bulletCenterY - obstacleCenterY;
        double normalizedDistance = (double) distanceFromCenter / (obstacle.height / 2);
        
        yVelocity = (int) (normalizedDistance * 5);
        
        updateRotation();
    }

    private void updateRotation() {
        // Calculate rotation based on velocity vector
        rotation = Math.atan2(yVelocity, xVelocity);
    }

    // Getter methods for previous position
    public int getPreviousX() {
        return previousX;
    }

    public int getPreviousY() {
        return previousY;
    }

    public boolean isOutOfBounds(int screenWidth) {
        return x < 0 || x > screenWidth;
    }

    public boolean collidesWith(Player player) {
        return this.intersects(player);
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
            
            // Translate to bullet center, rotate, then translate back
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            
            g2d.translate(centerX, centerY);
            g2d.rotate(rotation);
            g2d.translate(-width / 2, -height / 2);
            
            g2d.drawImage(bulletImage, 0, 0, width, height, null);
            g2d.dispose();
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, width, height);
        }
    }
}