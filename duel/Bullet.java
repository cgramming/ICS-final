/*
* Swapnil Kabir and Syed Bazif Shah
* Date: December 13, 2024
* Description: Bullet class representing projectiles shot by players,
* with directional movement and player-specific image rendering.
*/

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
public class Bullet extends Rectangle {
   private int xVelocity; // Bullet's speed in x direction
   private final int BASE_SPEED = 10; // Base speed of the bullet
   private BufferedImage bulletImage; // Image for the bullet
   private boolean isFromLeftPlayer; // Indicates which player shot a bullet, used to determine which bullet image to display
   private AffineTransform flipped;
   /*Constructor for Bullet class*/
   public Bullet(int x, int y, int width, int height, boolean isFromLeftPlayer) {
       super(x, y, width, height);
       this.isFromLeftPlayer = isFromLeftPlayer;
       loadBulletImage(x, y);
      
       // Set initial velocity based on player
       xVelocity = isFromLeftPlayer ? BASE_SPEED : -BASE_SPEED;
   }
   // Loads the appropriate bullet image based on player
   private void loadBulletImage(int x, int y) {
       try {
           // Load image based on which player shot the bullet
           String imageName = isFromLeftPlayer ? "bulletRight.png" : "bulletLeft.png";
           bulletImage = ImageIO.read(getClass().getResourceAsStream(imageName));
           bulletImage = resize(bulletImage, x, y);
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
   public BufferedImage resize(BufferedImage img, int newW, int newH) { 
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        
        flipped = AffineTransform.getScaleInstance(-1, 1);
        flipped.translate(-tmp.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(flipped, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        dimg = op.filter(dimg, null);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }  
}
