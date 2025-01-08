/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 7, 2025
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

    public Obstacle(int gameWidth, int gameHeight, MapManager mapManager) {
        this.GAME_WIDTH = gameWidth;
        this.GAME_HEIGHT = gameHeight;
        this.mapManager = mapManager;
        this.random = new Random();
        this.obstaclePositions = new ArrayList<>();
        this.brokenObstacles = new HashMap<>();
        loadObstacleImage();
    }

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

    public void generateObstaclePositions() {
        obstaclePositions.clear();
        brokenObstacles.clear();

        if (obstacleImage != null) {
            generateObstacles(TARGET_OBSTACLES);
        }
    }

    private void generateObstacles(int count) {
        int middleStart = GAME_WIDTH / 4;
        int middleWidth = GAME_WIDTH / 2;
        int topMargin = (int) (GAME_HEIGHT * 0.1);
        int usableHeight = GAME_HEIGHT - (2 * topMargin);

        for (int i = 0; i < count; i++) {
            Point newPoint;
            boolean overlaps;
            int attempts = 0;
            do {
                int x = middleStart + random.nextInt(middleWidth - obstacleImage.getWidth());
                int y = topMargin + random.nextInt(usableHeight - obstacleImage.getHeight());
                newPoint = new Point(x, y);
                overlaps = checkOverlap(newPoint);
                attempts++;
            } while (overlaps && attempts < 10);

            if (!overlaps) {
                obstaclePositions.add(newPoint);
            }
        }
    }

    // Get circle center point from obstacle position
    public Point getCircleCenter(Point obstaclePosition) {
        return new Point(
            obstaclePosition.x + obstacleImage.getWidth() / 2,
            obstaclePosition.y + obstacleImage.getHeight() / 2
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

    private boolean checkOverlap(Point newPoint) {
        Point newCenter = getCircleCenter(newPoint);
        
        for (Point existing : obstaclePositions) {
            Point existingCenter = getCircleCenter(existing);
            double distance = Math.sqrt(
                Math.pow(newCenter.x - existingCenter.x, 2) + 
                Math.pow(newCenter.y - existingCenter.y, 2)
            );
            // Use circle radius * 2 for minimum distance between centers
            if (distance < circleRadius * 2) {
                return true;
            }
        }
        return false;
    }

    // For debugging: draw collision circles
    public void draw(Graphics g) {
        if (obstacleImage != null) {
            for (Point p : obstaclePositions) {
                // Draw the image
                g.drawImage(obstacleImage, p.x, p.y, null);
                
                // Uncomment to debug: draw collision circle
                /*
                Point center = getCircleCenter(p);
                g.setColor(Color.RED);
                g.drawOval(
                    center.x - circleRadius,
                    center.y - circleRadius,
                    circleRadius * 2,
                    circleRadius * 2
                );
                */
            }
        }
    }

    // Get radius of circle
    public int getCircleRadius() {
        return circleRadius;
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<Point, Long>> iterator = brokenObstacles.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Point, Long> entry = iterator.next();
            if (currentTime - entry.getValue() >= REGENERATION_DELAY) {
                iterator.remove();
                generateObstacles(1);
            }
        }
    }

    public void breakObstacle(Point position) {
        obstaclePositions.remove(position);
        brokenObstacles.put(position, System.currentTimeMillis());
    }

    public ArrayList<Point> getObstaclePositions() {
        return obstaclePositions;
    }

    public BufferedImage getObstacleImage() {
        return obstacleImage;
    }

    public void regenerateObstacles() {
        loadObstacleImage();
        generateObstaclePositions();
    }
}