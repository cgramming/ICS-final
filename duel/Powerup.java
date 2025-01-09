/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 8, 2025
* Description: Powerup class manages the generation, positioning, and effects
* of powerups in the game. Supports Bomb, Freeze, and Big Bullet powerups
* with map-specific spawning and unique bullet modifications.
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

public class Powerup {
    private final int GAME_WIDTH;
    private final int GAME_HEIGHT;
    private BufferedImage powerupImage;
    private ArrayList<Point> powerupPositions;
    private Map<Point, Long> usedPowerups;
    private static final long REGENERATION_DELAY = 10000; // 10 seconds
    private static final int TARGET_POWERUPS = 2;
    private Random random;
    private MapManager mapManager;
    private int circleRadius;
    private String currentPowerupType;

    public Powerup(int gameWidth, int gameHeight, MapManager mapManager) {
        this.GAME_WIDTH = gameWidth;
        this.GAME_HEIGHT = gameHeight;
        this.mapManager = mapManager;
        this.random = new Random();
        this.powerupPositions = new ArrayList<>();
        this.usedPowerups = new HashMap<>();
        loadPowerupImage();
    }

    private void loadPowerupImage() {
        try {
            powerupImage = ImageIO.read(
                getClass().getResourceAsStream(mapManager.getPowerupImage())
            );
            // Set circle radius based on the smaller dimension of the image
            circleRadius = (int) (Math.min(powerupImage.getWidth(), powerupImage.getHeight()) * 0.45);
            
            // Determine powerup type based on image name
            String imageName = mapManager.getPowerupImage();
            if (imageName.contains("Bomb")) {
                currentPowerupType = "Bomb";
            } else if (imageName.contains("Freeze")) {
                currentPowerupType = "Freeze";
            } else if (imageName.contains("BigBullet")) {
                currentPowerupType = "BigBullet";
            }
        } catch (IOException e) {
            System.err.println("Error loading powerup image: " + e.getMessage());
            powerupImage = null;
        }
    }

    public void generatePowerupPositions(ArrayList<Point> obstaclePositions) {
        powerupPositions.clear();
        usedPowerups.clear();

        if (powerupImage != null) {
            generatePowerups(TARGET_POWERUPS, obstaclePositions);
        }
    }

    private void generatePowerups(int count, ArrayList<Point> obstaclePositions) {
        int middleStart = GAME_WIDTH / 4;
        int middleWidth = GAME_WIDTH / 2;
        int topMargin = (int) (GAME_HEIGHT * 0.1);
        int usableHeight = GAME_HEIGHT - (2 * topMargin);

        for (int i = 0; i < count; i++) {
            Point newPoint;
            boolean overlaps;
            int attempts = 0;
            do {
                int x = middleStart + random.nextInt(middleWidth - powerupImage.getWidth());
                int y = topMargin + random.nextInt(usableHeight - powerupImage.getHeight());
                newPoint = new Point(x, y);
                overlaps = checkOverlap(newPoint, obstaclePositions);
                attempts++;
            } while (overlaps && attempts < 10);

            if (!overlaps) {
                powerupPositions.add(newPoint);
            }
        }
    }

    // Get circle center point from powerup position
    public Point getCircleCenter(Point powerupPosition) {
        return new Point(
            powerupPosition.x + powerupImage.getWidth() / 2,
            powerupPosition.y + powerupImage.getHeight() / 2
        );
    }

    // Check if a line segment intersects with circle (for bullet collision)
    public boolean lineIntersectsCircle(Point center, Point lineStart, Point lineEnd) {
        double cx = center.x - lineStart.x;
        double cy = center.y - lineStart.y;
        double dx = lineEnd.x - lineStart.x;
        double dy = lineEnd.y - lineStart.y;
        double lengthSquared = dx * dx + dy * dy;
        double t = Math.max(0, Math.min(1, (cx * dx + cy * dy) / lengthSquared));
        double closestX = lineStart.x + t * dx;
        double closestY = lineStart.y + t * dy;
        double distanceSquared = Math.pow(center.x - closestX, 2) + 
                                Math.pow(center.y - closestY, 2);
        return distanceSquared <= circleRadius * circleRadius;
    }

    private boolean checkOverlap(Point newPoint, ArrayList<Point> obstaclePositions) {
        Point newCenter = getCircleCenter(newPoint);
        
        // Check overlap with other powerups
        for (Point existing : powerupPositions) {
            Point existingCenter = getCircleCenter(existing);
            double distance = Math.sqrt(
                Math.pow(newCenter.x - existingCenter.x, 2) + 
                Math.pow(newCenter.y - existingCenter.y, 2)
            );
            if (distance < circleRadius * 2) {
                return true;
            }
        }
        
        // Check overlap with obstacles
        for (Point obstacle : obstaclePositions) {
            Point obstacleCenter = new Point(
                obstacle.x + circleRadius,
                obstacle.y + circleRadius
            );
            double distance = Math.sqrt(
                Math.pow(newCenter.x - obstacleCenter.x, 2) + 
                Math.pow(newCenter.y - obstacleCenter.y, 2)
            );
            if (distance < circleRadius * 2) {
                return true;
            }
        }
        
        return false;
    }

    public void draw(Graphics g) {
        if (powerupImage != null) {
            for (Point p : powerupPositions) {
                g.drawImage(powerupImage, p.x, p.y, null);
            }
        }
    }

    public void update(ArrayList<Point> obstaclePositions) {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<Point, Long>> iterator = usedPowerups.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Point, Long> entry = iterator.next();
            if (currentTime - entry.getValue() >= REGENERATION_DELAY) {
                iterator.remove();
                generatePowerups(1, obstaclePositions);
            }
        }
    }

    public String activatePowerup(Point position, Bullet bullet, Player otherPlayer) {
        powerupPositions.remove(position);
        usedPowerups.put(position, System.currentTimeMillis());
        
        switch (currentPowerupType) {
            case "Bomb":
                // Create two additional bullets at ±30 degrees
                double currentAngle = Math.atan2(bullet.getyVelocity(), bullet.getxVelocity());
                double upAngle = currentAngle - Math.PI/6; // -30 degrees
                double downAngle = currentAngle + Math.PI/6; // +30 degrees
                
                // Set velocities for new bullets
                bullet.createSplitBullets(upAngle, downAngle);
                break;
                
            case "Freeze":
                // Freeze other player and guide bullet
                otherPlayer.freeze();
                double targetAngle = Math.atan2(
                    otherPlayer.y - bullet.y,
                    otherPlayer.x - bullet.x
                );
                bullet.setDirection(Math.cos(targetAngle), Math.sin(targetAngle));
                break;
                
            case "BigBullet":
                // Double bullet size
                bullet.resize(2.0);
                break;
        }
        
        return currentPowerupType;
    }

    public void regeneratePowerups(ArrayList<Point> obstaclePositions) {
        loadPowerupImage();
        generatePowerupPositions(obstaclePositions);
    }

    public ArrayList<Point> getPowerupPositions() {
        return powerupPositions;
    }
}