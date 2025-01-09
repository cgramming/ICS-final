/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 9, 2025
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
    private static final double COLLISION_RADIUS_MULTIPLIER = 0.45; // Aligned with Obstacle class
    private Obstacle obstacle;

    // Constructor initializes game dimensions, map manager, and powerup collections
    public Powerup(int gameWidth, int gameHeight, MapManager mapManager) {
        this.GAME_WIDTH = gameWidth;
        this.GAME_HEIGHT = gameHeight;
        this.mapManager = mapManager;
        this.random = new Random();
        this.powerupPositions = new ArrayList<>();
        this.usedPowerups = new HashMap<>();
        loadPowerupImage();
    }

    // Loads and processes the powerup image, sets circle radius, and determines powerup type
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

    // Clears existing powerups and generates new powerup positions avoiding obstacles
    public void generatePowerupPositions(ArrayList<Point> obstaclePositions) {
        powerupPositions.clear();
        usedPowerups.clear();

        if (powerupImage != null) {
            generatePowerups(TARGET_POWERUPS, obstaclePositions);
        }
    }

    // Places specified number of powerups in middle section of game area avoiding overlaps
    private void generatePowerups(int count, ArrayList<Point> obstaclePositions) {
        int middleStart = GAME_WIDTH / 4;
        int middleWidth = GAME_WIDTH / 2;
        int topMargin = (int) (GAME_HEIGHT * 0.12); // Blocks off powerups from top 12% of screen, ensuring players can get high enough to shoot the powerups
        int usableHeight = GAME_HEIGHT - (2 * topMargin);

        int successfulPlacements = 0;
        int maxAttempts = 20;
        int totalAttempts = 0;

        while (successfulPlacements < count && totalAttempts < maxAttempts) {
            int x = middleStart + random.nextInt(middleWidth - powerupImage.getWidth());
            int y = topMargin + random.nextInt(usableHeight - powerupImage.getHeight());
            Point newPoint = new Point(x, y);
            
            if (!checkOverlap(newPoint, obstaclePositions) && 
                !powerupPositions.contains(newPoint)) {
                powerupPositions.add(newPoint);
                successfulPlacements++;
            }
            totalAttempts++;
        }
    }

    // Get circle center point from powerup position
    public Point getCircleCenter(Point powerupPosition) {
        return new Point(
            powerupPosition.x + powerupImage.getWidth() / 4,  // Divide by 4 since image is half size
            powerupPosition.y + powerupImage.getHeight() / 4  // Divide by 4 since image is half size
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

    // Checks for overlaps between powerups and obstacles
    private boolean checkOverlap(Point newPoint, ArrayList<Point> obstaclePositions) {
        // Calculate powerup dimensions at half size
        int scaledWidth = powerupImage.getWidth() / 2;
        int scaledHeight = powerupImage.getHeight() / 2;
        
        Point newCenter = new Point(
            newPoint.x + scaledWidth / 2,
            newPoint.y + scaledHeight / 2
        );
        double newRadius = Math.min(scaledWidth, scaledHeight) * COLLISION_RADIUS_MULTIPLIER;
        
        // Check overlap with other powerups
        for (Point existing : powerupPositions) {
            Point existingCenter = new Point(
                existing.x + scaledWidth / 2,
                existing.y + scaledHeight / 2
            );
            double distance = Math.sqrt(
                Math.pow(newCenter.x - existingCenter.x, 2) + 
                Math.pow(newCenter.y - existingCenter.y, 2)
            );
            // Add padding to ensure no overlap
            if (distance < (newRadius * 2) + 10) {
                return true;
            }
        }
    
        // Check overlap with obstacles if they exist
        if (obstaclePositions != null && obstacle != null) {
            BufferedImage obstacleImg = obstacle.getObstacleImage();
            if (obstacleImg != null) {
                // Account for obstacle's half size
                int obstacleWidth = obstacleImg.getWidth() / 2;
                int obstacleHeight = obstacleImg.getHeight() / 2;
                
                for (Point obstaclePoint : obstaclePositions) {
                    Point obstacleCenter = new Point(
                        obstaclePoint.x + obstacleWidth / 2,
                        obstaclePoint.y + obstacleHeight / 2
                    );
                    double obstacleRadius = Math.min(obstacleWidth, obstacleHeight) * COLLISION_RADIUS_MULTIPLIER;
                    
                    double minDistance = newRadius + obstacleRadius;
                    double distance = Math.sqrt(
                        Math.pow(newCenter.x - obstacleCenter.x, 2) + 
                        Math.pow(newCenter.y - obstacleCenter.y, 2)
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

    // Sets the obstacle reference for collision checking
    public void setObstacle(Obstacle obstacle) {
        this.obstacle = obstacle;
    }

    // Renders all active powerups to the screen
    public void draw(Graphics g) {
        if (powerupImage != null) {
            for (Point p : powerupPositions) {
                // Draw the image at half size
                g.drawImage(powerupImage, 
                    p.x, p.y, 
                    powerupImage.getWidth() / 2, 
                    powerupImage.getHeight() / 2, 
                    null);
            }
        }
    }

    // Updates powerup states and regenerates them after delay
    public void update(ArrayList<Point> obstaclePositions) {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<Point, Long>> iterator = usedPowerups.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Point, Long> entry = iterator.next();
            if (currentTime - entry.getValue() >= REGENERATION_DELAY) {
                iterator.remove();
                // Only generate new powerup if we're below target count
                if (powerupPositions.size() < TARGET_POWERUPS) {
                    generatePowerups(1, obstaclePositions);
                }
            }
        }
    }

    // Manages functionality of different powerups, tells game which one to activate
    public String activatePowerup(Point position, Bullet bullet, Player otherPlayer) {
        powerupPositions.remove(position);
        usedPowerups.put(position, System.currentTimeMillis());
        
        switch (currentPowerupType) {
            case "Bomb":
                // Create two additional bullets at ±30 degrees
                double currentAngle = Math.atan2(bullet.getyVelocity(), bullet.getxVelocity());
                double upAngle = currentAngle - Math.PI/6;
                double downAngle = currentAngle + Math.PI/6;
                bullet.createSplitBullets(upAngle, downAngle);
                break;
                
            case "Freeze":
                // Set freeze effect and track player to unfreeze
                bullet.setFreezeEffect(true, otherPlayer);
                double targetAngle = Math.atan2(
                    otherPlayer.y - bullet.y,
                    otherPlayer.x - bullet.x
                );
                bullet.setDirection(Math.cos(targetAngle), Math.sin(targetAngle));
                break;
                
            case "BigBullet":
                bullet.resize(2.0);
                break;
        }
        
        return currentPowerupType;
    }

    // Reloads powerup image and generates new powerup positions
    public void regeneratePowerups(ArrayList<Point> obstaclePositions) {
        loadPowerupImage();
        generatePowerupPositions(obstaclePositions);
    }

    // Returns the current powerup image (for use by Obstacle class)
    public BufferedImage getPowerupImage() {
        return powerupImage;
    }

    // Returns the list of current powerup positions
    public ArrayList<Point> getPowerupPositions() {
        return powerupPositions;
    }
}