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
    private static final long REGENERATION_DELAY = 5000; // 5 seconds in milliseconds
    private static final int TARGET_OBSTACLES = 5;
    private Random random;
    private MapManager mapManager;

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

    private boolean checkOverlap(Point newPoint) {
        Rectangle newRect = new Rectangle(newPoint.x, newPoint.y, 
            obstacleImage.getWidth(), obstacleImage.getHeight());
            
        for (Point existing : obstaclePositions) {
            Rectangle existingRect = new Rectangle(existing.x, existing.y, 
                obstacleImage.getWidth(), obstacleImage.getHeight());
            if (newRect.intersects(existingRect)) {
                return true;
            }
        }
        return false;
    }

    public void draw(Graphics g) {
        if (obstacleImage != null) {
            for (Point p : obstaclePositions) {
                g.drawImage(obstacleImage, p.x, p.y, null);
            }
        }
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