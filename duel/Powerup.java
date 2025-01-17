/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 17, 2025
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
    private Map<Point, BufferedImage> powerupImages;
    private Map<Point, String> powerupTypes;
    private ArrayList<Point> powerupPositions;
    private Map<Point, Long> usedPowerups;
    private static final long REGENERATION_DELAY = 7000; // 7 seconds
    private static final int TARGET_POWERUPS = 3;
    private Random random;
    private MapManager mapManager;
    private int circleRadius;
    private static final double COLLISION_RADIUS_MULTIPLIER = 0.45; // Aligned with Obstacle class
    private Obstacle obstacle;
    private Map<Point, Long> spawnTimes;
    private static final long SPAWN_INVINCIBILITY_DURATION = 500; // 0.5 seconds

    // Constructor initializes game dimensions, map manager, and powerup collections
    public Powerup(int gameWidth, int gameHeight, MapManager mapManager) {
        this.GAME_WIDTH = gameWidth;
        this.GAME_HEIGHT = gameHeight;
        this.mapManager = mapManager;
        this.random = new Random();
        this.powerupPositions = new ArrayList<>();
        this.powerupImages = new HashMap<>();
        this.powerupTypes = new HashMap<>();
        this.usedPowerups = new HashMap<>();
        this.spawnTimes = new HashMap<>();
    }

    // Loads and processes the powerup image, sets circle radius, and determines powerup type
    private void loadPowerupImage(Point position) {
        try {
            String powerupType = mapManager.getRandomPowerupType();
            BufferedImage image = ImageIO.read(getClass().getResourceAsStream(powerupType));
            powerupImages.put(position, image);
            powerupTypes.put(position, powerupType);
            
            // Set circle radius based on the smaller dimension of the image
            circleRadius = (int) (Math.min(image.getWidth(), image.getHeight()) * 0.45);
        } catch (IOException e) {
            System.err.println("Error loading powerup image: " + e.getMessage());
        }
    }

    // Clears existing powerups and generates new powerup positions avoiding obstacles
    public void generatePowerupPositions(ArrayList<Point> obstaclePositions) {
        powerupPositions.clear();
        powerupImages.clear();
        powerupTypes.clear();
        usedPowerups.clear();

        generatePowerups(TARGET_POWERUPS, obstaclePositions);
    }   

    // Places specified number of powerups in middle section of game area avoiding overlaps
    private void generatePowerups(int count, ArrayList<Point> obstaclePositions) {
        int middleStart = GAME_WIDTH / 4;
        int middleWidth = GAME_WIDTH / 2;
        int topMargin = (int) (GAME_HEIGHT * 0.12);
        int usableHeight = GAME_HEIGHT - (2 * topMargin);
        
        int successfulPlacements = 0;
        int maxAttempts = 20;
        int totalAttempts = 0;

        while (successfulPlacements < count && totalAttempts < maxAttempts) {
            int x = middleStart + random.nextInt(middleWidth - 50); // Using 50 as default width
            int y = topMargin + random.nextInt(usableHeight - 50); // Using 50 as default height
            Point newPoint = new Point(x, y);
            
            if (!checkOverlap(newPoint, obstaclePositions) && 
                !powerupPositions.contains(newPoint)) {
                powerupPositions.add(newPoint);
                loadPowerupImage(newPoint); // Load random powerup image for this position
                successfulPlacements++;
            }
            totalAttempts++;
        }
    }

    // Get circle center point from powerup position
    public Point getCircleCenter(Point powerupPosition) {
        BufferedImage image = powerupImages.get(powerupPosition);
        if (image != null) {
            return new Point(
                powerupPosition.x + image.getWidth() / 4,  // Divide by 4 since image is half size
                powerupPosition.y + image.getHeight() / 4  // Divide by 4 since image is half size
            );
        }
        return powerupPosition; // Fallback if image not found
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
        // Use default dimensions for initial placement
        int defaultWidth = 50;  // Default width for checking overlap before image is loaded
        int defaultHeight = 50; // Default height for checking overlap before image is loaded
    
        Point newCenter = new Point(
            newPoint.x + defaultWidth / 4,
            newPoint.y + defaultHeight / 4
        );
        double newRadius = Math.min(defaultWidth, defaultHeight) * COLLISION_RADIUS_MULTIPLIER;
    
        // Check overlap with other powerups
        for (Point existing : powerupPositions) {
            BufferedImage existingImage = powerupImages.get(existing);
            int scaledWidth = existingImage != null ? existingImage.getWidth() / 2 : defaultWidth;
            int scaledHeight = existingImage != null ? existingImage.getHeight() / 2 : defaultHeight;
        
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
        for (Point p : powerupPositions) {
            BufferedImage image = powerupImages.get(p);
            if (image != null) {
                g.drawImage(image, 
                    p.x, p.y, 
                    image.getWidth() / 2, 
                    image.getHeight() / 2, 
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
                // Only generate new powerup if below target count
                if (powerupPositions.size() < TARGET_POWERUPS) {
                    generatePowerups(1, obstaclePositions);
                }
            }
        }
    }

    // Manages functionality of different powerups, tells game which one to activate
    public String activatePowerup(Point position, Bullet bullet, Player otherPlayer) {
        Long spawnTime = spawnTimes.get(position);
        if (spawnTime != null && 
        System.currentTimeMillis() - spawnTime < SPAWN_INVINCIBILITY_DURATION) {
        return null; // Powerup is still invincible
        }
        String powerupType = powerupTypes.get(position);
        powerupPositions.remove(position);
        powerupImages.remove(position);
        usedPowerups.put(position, System.currentTimeMillis());
        
        // Extract powerup type from filename
        String type = powerupType.replace(".png", "");
        
        switch (type) {
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
        spawnTimes.remove(position);
        return type;
    }

    // Reloads powerup image and generates new powerup positions
    public void regeneratePowerups(ArrayList<Point> obstaclePositions) {
        powerupPositions.clear();
        powerupImages.clear();
        powerupTypes.clear();
        generatePowerupPositions(obstaclePositions);
    }

    // Returns the current powerup image (for use by Obstacle class)
    public BufferedImage getPowerupImage() {
        // Since we now have multiple images, return the first one if it exists
        if (!powerupPositions.isEmpty() && !powerupImages.isEmpty()) {
            return powerupImages.get(powerupPositions.get(0));
        }
        return null;
    }

    // Returns the list of current powerup positions
    public ArrayList<Point> getPowerupPositions() {
        return powerupPositions;
    }
}