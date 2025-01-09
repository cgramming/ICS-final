/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 9, 2025
* Description: Obstacle class manages the generation, positioning,
* and rendering of obstacles in the game.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

public class Obstacle {
    private final int GAME_WIDTH;
    private final int GAME_HEIGHT;
    private BufferedImage obstacleImage;
    private ArrayList<Point> obstaclePositions;
    private Map<Point, Long> brokenObstacles;
    private static final long REGENERATION_DELAY = 5000;
    private static final int TARGET_OBSTACLES = 5;
    private Random random;
    private MapManager mapManager;
    private int circleRadius; // Radius for collision detection
    private static final double COLLISION_RADIUS_MULTIPLIER = 0.45; // Aligned with Powerup class
    private Powerup powerup;

    public Obstacle(int gameWidth, int gameHeight, MapManager mapManager) {
        this.GAME_WIDTH = gameWidth;
        this.GAME_HEIGHT = gameHeight;
        this.mapManager = mapManager;
        this.random = new Random();
        this.obstaclePositions = new ArrayList<>();
        this.brokenObstacles = new HashMap<>();
        loadObstacleImage();
    }

    // Loads the obstacle image from resources and calculates collision radius
    private void loadObstacleImage() {
        try {
            obstacleImage = ImageIO.read(
                getClass().getResourceAsStream(mapManager.getObstacleImage())
            );
            // Set circle radius based on the smaller dimension of the image
            // Multiply by 0.45 to make circle slightly smaller than image for visual accuracy
            circleRadius = (int) (Math.min(obstacleImage.getWidth(), obstacleImage.getHeight()) * 0.45);
        } catch (IOException e) {
            System.err.println("Error loading obstacle image: " + e.getMessage());
            obstacleImage = null;
        }
    }

    // Clears and regenerates all obstacle positions
    public void generateObstaclePositions() {
    obstaclePositions.clear();
    brokenObstacles.clear();

    if (obstacleImage != null) {
        // Pass an empty ArrayList if powerup is null or no positions available
        ArrayList<Point> powerupPositions = (powerup != null) ? 
            powerup.getPowerupPositions() : new ArrayList<>();
        generateObstacles(TARGET_OBSTACLES, powerupPositions);
        }
    }
    
    // Generates a specified number of non-overlapping obstacles
    private void generateObstacles(int count, ArrayList<Point> powerupPositions) {
    int middleStart = GAME_WIDTH / 4;
    int middleWidth = GAME_WIDTH / 2;
    int topMargin = (int) (GAME_HEIGHT * 0.1);
    int usableHeight = GAME_HEIGHT - (2 * topMargin);

    int successfulPlacements = 0;
    int maxAttempts = 20;
    int totalAttempts = 0;

    while (successfulPlacements < count && totalAttempts < maxAttempts) {
        int x = middleStart + random.nextInt(middleWidth - obstacleImage.getWidth());
        int y = topMargin + random.nextInt(usableHeight - obstacleImage.getHeight());
        Point newPoint = new Point(x, y);
        
        if (!checkOverlap(newPoint, powerupPositions) && 
            !obstaclePositions.contains(newPoint)) {
            obstaclePositions.add(newPoint);
            successfulPlacements++;
        }
        totalAttempts++;
    }
}

    // Get circle center point from obstacle position
    public Point getCircleCenter(Point obstaclePosition) {
        return new Point(
            obstaclePosition.x + (obstacleImage.getWidth() / 2),  // Changed from /4 to /2
            obstaclePosition.y + (obstacleImage.getHeight() / 2)  // Changed from /4 to /2
        );
    }

    // Check if a point is within the circular collision area
    public boolean isPointInCircle(Point center, Point test) {
        double distance = Math.sqrt(
            Math.pow(center.x - test.x, 2) + 
            Math.pow(center.y - test.y, 2)
        );
        return distance <= circleRadius;
    }

    // Check if a line segment intersects with circle
    public boolean lineIntersectsCircle(Point center, Point lineStart, Point lineEnd) {
        // Vector from line start to circle center
        double cx = center.x - lineStart.x;
        double cy = center.y - lineStart.y;
        
        // Vector from line start to line end
        double dx = lineEnd.x - lineStart.x;
        double dy = lineEnd.y - lineStart.y;
        
        // Length of line segment squared
        double lengthSquared = dx * dx + dy * dy;
        
        // Calculate closest point on line to circle center
        double t = Math.max(0, Math.min(1, (cx * dx + cy * dy) / lengthSquared));
        
        // Find closest point coordinates
        double closestX = lineStart.x + t * dx;
        double closestY = lineStart.y + t * dy;
        
        // Check if closest point is within circle radius
        double distanceSquared = Math.pow(center.x - closestX, 2) + 
                                Math.pow(center.y - closestY, 2);
        return distanceSquared <= circleRadius * circleRadius;
    }

    // Checks if a new obstacle would overlap with existing obstacles or powerups
    private boolean checkOverlap(Point newPoint, ArrayList<Point> powerupPositions) {
        // Get dimensions using full size
        int width = obstacleImage.getWidth();
        int height = obstacleImage.getHeight();
        
        Point newCenter = new Point(
            newPoint.x + width / 2,
            newPoint.y + height / 2
        );
        double newRadius = Math.min(width, height) * COLLISION_RADIUS_MULTIPLIER;
        
        // Check overlap with other obstacles
        for (Point existing : obstaclePositions) {
            Point existingCenter = new Point(
                existing.x + width / 2,
                existing.y + height / 2
            );
            double distance = Math.sqrt(
                Math.pow(newCenter.x - existingCenter.x, 2) + 
                Math.pow(newCenter.y - existingCenter.y, 2)
            );
            // Add some padding to ensure no overlap
            if (distance < (newRadius * 2) + 10) {
                return true;
            }
        }
    
    // Check overlap with powerups if they exist
    if (powerupPositions != null && powerup != null) {
        BufferedImage powerupImg = powerup.getPowerupImage();
        if (powerupImg != null) {
            int powerupWidth = powerupImg.getWidth();
            int powerupHeight = powerupImg.getHeight();
            
            for (Point powerupPoint : powerupPositions) {
                Point powerupCenter = new Point(
                    powerupPoint.x + powerupWidth / 2,
                    powerupPoint.y + powerupHeight / 2
                );
                double powerupRadius = Math.min(powerupWidth, powerupHeight) * COLLISION_RADIUS_MULTIPLIER;
                
                double minDistance = newRadius + powerupRadius;
                double distance = Math.sqrt(
                    Math.pow(newCenter.x - powerupCenter.x, 2) + 
                    Math.pow(newCenter.y - powerupCenter.y, 2)
                );
                
                // Add padding to ensure no overlap
                if (distance < minDistance + 10) {
                    return true;
                }
            }
        }
    }
    
    return false;
}

// Sets the powerup reference for collision checking
public void setPowerup(Powerup powerup) {
    this.powerup = powerup;
}

    // Renders all active obstacles on the game screen
    public void draw(Graphics g) {
        if (obstacleImage != null) {
            for (Point p : obstaclePositions) {
                // Draw the image at full size
                g.drawImage(obstacleImage, 
                    p.x, p.y, 
                    obstacleImage.getWidth(), 
                    obstacleImage.getHeight(), 
                    null);
            }
        }
    }

    // Updates obstacle states and regenerates broken obstacles after delay
    public void update(ArrayList<Point> powerupPositions) {
    long currentTime = System.currentTimeMillis();
    Iterator<Map.Entry<Point, Long>> iterator = brokenObstacles.entrySet().iterator();
    
    while (iterator.hasNext()) {
        Map.Entry<Point, Long> entry = iterator.next();
        if (currentTime - entry.getValue() >= REGENERATION_DELAY) {
            iterator.remove();
            // Only generate new obstacle if we're below target count
            if (obstaclePositions.size() < TARGET_OBSTACLES) {
                generateObstacles(1, powerupPositions);
            }
        }
    }
    }

    // Marks an obstacle as broken and starts its regeneration timer
    public void breakObstacle(Point position) {
        obstaclePositions.remove(position);
        brokenObstacles.put(position, System.currentTimeMillis());
    }

    // Returns the list of current obstacle positions
    public ArrayList<Point> getObstaclePositions() {
        return obstaclePositions;
    }

    // Returns the obstacle image for use by other classes
    public BufferedImage getObstacleImage() {
        return obstacleImage;
    }

    // Reloads obstacle image and regenerates all obstacles
    public void regenerateObstacles() {
        loadObstacleImage();
        generateObstaclePositions();
    }
}