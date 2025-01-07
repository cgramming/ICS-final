import java.util.Random;

public class MapManager {
    // Static inner class to hold map data
    private static class MapData {
        final String backgroundImage;
        final String powerupImage;
        final String obstacleImage;
        
        MapData(String backgroundImage, String powerupImage, String obstacleImage) {
            this.backgroundImage = backgroundImage;
            this.powerupImage = powerupImage;
            this.obstacleImage = obstacleImage;
        }
    }
    
    // Available maps
    private static final MapData[] MAPS = {
        new MapData("grassBackground.png", "Bomb.png", "Bush.png"),
        new MapData("snowBackground.png", "Freeze.png", "Igloo.png"),
        new MapData("dirtBackground.png", "BigBullet.png", "Boulder.png")
    };
    
    private MapData currentMap;
    private Random random;
    
    public MapManager() {
        random = new Random();
        randomizeMap();
    }
    
    public void randomizeMap() {
        currentMap = MAPS[random.nextInt(MAPS.length)];
    }
    
    // Getters for image assets
    public String getBackgroundImage() {
        return currentMap.backgroundImage;
    }
    
    public String getPowerupImage() {
        return currentMap.powerupImage;
    }
    
    public String getObstacleImage() {
        return currentMap.obstacleImage;
    }
}
