/*
 * Swapnil Kabir and Syed Bazif Shah
 * Date: January 17, 2025
 * Description: MapManager class manages the game's map selection,
 * including randomization of maps and retrieval of associated image assets
 * for backgrounds and obstacles. Powerups are now independent of maps.
 */

import java.util.Random;

public class MapManager {
    // Static inner class to hold map data
    private static class MapData {
        final String backgroundImage;
        final String obstacleImage;
        
        // Constructor to initialize map assets paths
        MapData(String backgroundImage, String obstacleImage) {
            this.backgroundImage = backgroundImage;
            this.obstacleImage = obstacleImage;
        }
    }
    
    // Available maps with their corresponding image assets
    private static final MapData[] MAPS = {
        new MapData("grassBackground.png", "Bush.png"),
        new MapData("snowBackground.png", "Igloo.png"),
        new MapData("dirtBackground.png", "Boulder.png")
    };
    
    // Available powerup types - now public static so Powerup class can access them
    public static final String[] POWERUP_TYPES = {
        "Bomb.png",
        "Freeze.png",
        "BigBullet.png"
    };
    
    private MapData currentMap;
    private Random random;
    
    // Initialize MapManager with a random map selection
    public MapManager() {
        random = new Random();
        randomizeMap();
    }
    
    // Randomly selects a new map from available maps
    public void randomizeMap() {
        currentMap = MAPS[random.nextInt(MAPS.length)];
    }
    
    // Returns the filename of the current map's background image
    public String getBackgroundImage() {
        return currentMap.backgroundImage;
    }
    
    // Returns the filename of the current map's obstacle image
    public String getObstacleImage() {
        return currentMap.obstacleImage;
    }
    
    // Returns a random powerup type
    public String getRandomPowerupType() {
        return POWERUP_TYPES[random.nextInt(POWERUP_TYPES.length)];
    }
}