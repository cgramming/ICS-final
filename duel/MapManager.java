/*
 * Swapnil Kabir and Syed Bazif Shah
 * Date: January 9, 2025
 * Description: MapManager class manages the game's map selection,
 * including randomization of maps and retrieval of associated image assets
 * for backgrounds, power-ups, and obstacles.
 */

import java.util.Random;

public class MapManager {
    // Static inner class to hold map data
    private static class MapData {
        final String backgroundImage;
        final String powerupImage;
        final String obstacleImage;
        
        // Constructor to initialize map assets paths
        MapData(String backgroundImage, String powerupImage, String obstacleImage) {
            this.backgroundImage = backgroundImage;
            this.powerupImage = powerupImage;
            this.obstacleImage = obstacleImage;
        }
    }
    
    // Available maps with their corresponding image assets
    private static final MapData[] MAPS = {
        new MapData("grassBackground.png", "Bomb.png", "Bush.png"),
        new MapData("snowBackground.png", "Freeze.png", "Igloo.png"),
        new MapData("dirtBackground.png", "BigBullet.png", "Boulder.png")
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
    
    // Returns the filename of the current map's powerup image
    public String getPowerupImage() {
        return currentMap.powerupImage;
    }
    
    // Returns the filename of the current map's obstacle image
    public String getObstacleImage() {
        return currentMap.obstacleImage;
    }
}