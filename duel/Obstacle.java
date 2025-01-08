/*
 * Description: Obstacle class manages obstacle generation, rendering,
 * and collision detection for the Duel game. Handles the randomization
 * of obstacle placement and maintains obstacle properties.
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

public class Obstacle extends Rectangle {
    // Image and collection management
    private static BufferedImage obstacleImage;
    private static final Random random = new Random();
    private static ArrayList<Obstacle> obstacles = new ArrayList<>();
    
    // Constants for obstacle generation
    private static final int NUM_OBSTACLES = 5;
    
    // Constructor for creating individual obstacles
    public Obstacle(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    
    // Loads the obstacle image from resources
    public static void loadObstacleImage(String imagePath) {
        try {
            obstacleImage = ImageIO.read(Obstacle.class.getResourceAsStream(imagePath));
        } catch (IOException e) {
            System.err.println("Error loading obstacle image: " + e.getMessage());
            obstacleImage = null;
        }
    }
    
    // Generates obstacles in random positions within the middle section of the game area.
    public static void generateObstacles(int gameWidth, int gameHeight) {
        // Clear existing positions
        obstacles.clear();
        
        if (obstacleImage != null) {
            int middleStart = gameWidth / 4;
            int middleWidth = gameWidth / 2;
            int topMargin = (int) (gameHeight * 0.1);
            int usableHeight = gameHeight - (2 * topMargin);
            
            for (int i = 0; i < NUM_OBSTACLES; i++) {
                Point newPoint;
                boolean overlaps;
                int attempts = 0;
                
                do {
                    // Generate random position within the middle section
                    int x = middleStart + random.nextInt(middleWidth - obstacleImage.getWidth());
                    int y = topMargin + random.nextInt(usableHeight - obstacleImage.getHeight());
                    newPoint = new Point(x, y);
                    overlaps = false;
                    
                    // Check for overlap with existing obstacles
                    for (Obstacle existing : obstacles) {
                        if (new Rectangle(newPoint.x, newPoint.y, 
                                obstacleImage.getWidth(), obstacleImage.getHeight())
                            .intersects(existing)) {
                            overlaps = true;
                            break;
                        }
                    }
                    attempts++;
                } while (overlaps && attempts < 10); // Limit attempts to avoid infinite loops
                
                obstacles.add(new Obstacle(
                    newPoint.x, newPoint.y,
                    obstacleImage.getWidth(), obstacleImage.getHeight()
                ));
            }
        }
    }
    
    // Draws all obstacles on the game panel
    public static void drawAllObstacles(Graphics g) {
        for (Obstacle obstacle : obstacles) {
            obstacle.draw(g);
        }
    }
    
    // Returns the list of all obstacles
    public static ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }
    
    // Draws an individual obstacle
    private void draw(Graphics g) {
        if (obstacleImage != null) {
            g.drawImage(obstacleImage, x, y, width, height, null);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(x, y, width, height);
        }
    }
}