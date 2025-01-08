/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 7, 2025
* Description: Obstacle class manages the generation, positioning,
* and rendering of obstacles in the game.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

public class Obstacle {
    // Game dimensions for obstacle placement
    private final int GAME_WIDTH;
    private final int GAME_HEIGHT;

    // Obstacle image and positions
    private BufferedImage obstacleImage;
    private ArrayList<Point> obstaclePositions;
    private Random random;

    // Reference to MapManager
    private MapManager mapManager;

    // Constructor initializes obstacle management
    public Obstacle(int gameWidth, int gameHeight, MapManager mapManager) {
        this.GAME_WIDTH = gameWidth;
        this.GAME_HEIGHT = gameHeight;
        this.mapManager = mapManager; // Save MapManager reference
        this.random = new Random();
        this.obstaclePositions = new ArrayList<>();
        loadObstacleImage();
    }

    // Load obstacle image asset
    private void loadObstacleImage() {
        try {
            obstacleImage = ImageIO.read(
                getClass().getResourceAsStream(mapManager.getObstacleImage())
            );
        } catch (IOException e) {
            System.err.println("Error loading obstacle image: " + e.getMessage());
            obstacleImage = null;
        }
    }

    // Generate random positions for obstacles to spawn in
    public void generateObstaclePositions() {
        obstaclePositions.clear();

        if (obstacleImage != null) {
            int middleStart = GAME_WIDTH / 4;
            int middleWidth = GAME_WIDTH / 2;
            int topMargin = (int) (GAME_HEIGHT * 0.1);
            int usableHeight = GAME_HEIGHT - (2 * topMargin);
            int numObstacles = 5;

            for (int i = 0; i < numObstacles; i++) {
                Point newPoint;
                boolean overlaps;
                int attempts = 0;
                do {
                    int x = middleStart + random.nextInt(middleWidth - obstacleImage.getWidth());
                    int y = topMargin + random.nextInt(usableHeight - obstacleImage.getHeight());
                    newPoint = new Point(x, y);
                    overlaps = false;

                    // Check for overlap with existing obstacles
                    for (Point existing : obstaclePositions) {
                        if (new Rectangle(newPoint.x, newPoint.y, obstacleImage.getWidth(), obstacleImage.getHeight())
                            .intersects(new Rectangle(existing.x, existing.y, obstacleImage.getWidth(), obstacleImage.getHeight()))) {
                            overlaps = true;
                            break;
                        }
                    }
                    attempts++;
                } while (overlaps && attempts < 10); // Limit attempts to avoid infinite loops

                obstaclePositions.add(newPoint);
            }
        }
    }

    // Draw obstacles on the game panel
    public void draw(Graphics g) {
        if (obstacleImage != null && obstaclePositions != null) {
            for (Point p : obstaclePositions) {
                g.drawImage(obstacleImage, p.x, p.y, null);
            }
        }
    }

    // Getters
    public ArrayList<Point> getObstaclePositions() {
        return obstaclePositions;
    }

    public BufferedImage getObstacleImage() {
        return obstacleImage;
    }
    
    // Generate new positions for obstacles after map change
    public void regenerateObstacles() {
        loadObstacleImage();
        generateObstaclePositions();
    }
}
