import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Bullet extends Rectangle {
<<<<<<< HEAD
    private int xVelocity; // Bullet's speed in x direction
    private int yVelocity; // Bullet's speed in y direction
    private final int BASE_SPEED = 10; // Base speed of the bullet
    private BufferedImage bulletImage; // Image for the bullet
    private boolean isFromLeftPlayer; // Indicates which player shot the bullet, used to determine which bullet image to display
    private AffineTransform flipped;
    private double slope = 0;
=======
    private int xVelocity;
    private int yVelocity;
    private final int BASE_SPEED = 10;
    private BufferedImage bulletImage;
    private boolean isFromLeftPlayer;
    private double rotation; // Track bullet rotation in radians
>>>>>>> cac729dc3458e319b4d4886a1bcff4d51e90f675

    public Bullet(int x, int y, int width, int height, boolean isFromLeftPlayer) {
        super(x, y, width, height);
        this.isFromLeftPlayer = isFromLeftPlayer;
        loadBulletImage();

        // Set initial velocities
        xVelocity = isFromLeftPlayer ? BASE_SPEED : -BASE_SPEED;
        yVelocity = 0;
        
        // Set initial rotation based on player
        rotation = isFromLeftPlayer ? 0 : Math.PI;
    }

    private void loadBulletImage() {
        try {
<<<<<<< HEAD
            // Load image based on which player shot the bullet
            String imageName = isFromLeftPlayer ? "bulletRight.png" : "bulletLeft.png";
            bulletImage = ImageIO.read(getClass().getResourceAsStream(imageName));
            
        } catch (IOException | IllegalArgumentException e) {
=======
            // Now using a single bullet image
            bulletImage = ImageIO.read(getClass().getResourceAsStream("bullet.png"));
            bulletImage = resizeImage(bulletImage, width, height);
        } catch (IOException e) {
>>>>>>> cac729dc3458e319b4d4886a1bcff4d51e90f675
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
        double newslope;
        if(xVelocity == 0) newslope = 1000;
        else newslope =(double)(((y + yVelocity) - y))/(double)(((x + xVelocity) - x));
        
        if(newslope != slope){
            double angle = Math.atan(newslope) + Math.PI;
            System.out.println(angle);
            bulletImage = rotate(bulletImage, angle);
            bulletImage = resize(bulletImage, x, y);
            slope = newslope;
        }
        x += xVelocity;
        y += yVelocity;

<<<<<<< HEAD
        
        // Ensure the bullet stays within vertical bounds
=======
        // Update rotation when bouncing off screen bounds
>>>>>>> cac729dc3458e319b4d4886a1bcff4d51e90f675
        if (y < 0 || y > GamePanel.GAME_HEIGHT - height) {
            yVelocity = -yVelocity;
            updateRotation();
        }
        
    }

    public void bounceOffObstacle(Rectangle obstacle) {

        // Calculate y velocity based on collision point
        int obstacleCenterY = obstacle.y + obstacle.height / 2;
        int bulletCenterY = y + height / 2;

        int distanceFromCenter = bulletCenterY - obstacleCenterY;
        double normalizedDistance = (double) distanceFromCenter / (obstacle.height / 2);

<<<<<<< HEAD
        // Scale normalized distance to a y velocity range (e.g., -5 to 5)
        yVelocity = (int) (normalizedDistance * 7);

        xVelocity = (int)  - Math.sqrt(BASE_SPEED*BASE_SPEED - (double)yVelocity*yVelocity);
=======
        // Scale normalized distance to a y velocity range
        yVelocity = (int) (normalizedDistance * 5);

        // Update bullet rotation based on new velocity
        updateRotation();
    }

    private void updateRotation() {
        // Calculate rotation based on velocity vector
        rotation = Math.atan2(yVelocity, xVelocity);
>>>>>>> cac729dc3458e319b4d4886a1bcff4d51e90f675
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
