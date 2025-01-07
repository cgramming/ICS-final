/*
* Swapnil Kabir and Syed Bazif Shah
* Date: December 16, 2024
* Description: GamePanel class manages game objects, rendering,
* and primary game loop for the Duel game.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable, KeyListener {
   // Screen dimensions
   public static final int GAME_WIDTH = 1000;
   public static final int GAME_HEIGHT = 600;
   // Game thread and rendering
   public Thread gameThread;
   public Image image;
   public Graphics graphics;
   // Game objects
   public Player playerLeft;
   public Player playerRight;
   public Bullet bulletLeft;
   public Bullet bulletRight;
   public Score score;
   // Menu and game state
   public Menu menu;
   private boolean gameStarted = false;
   // Bullet dimensions
   int bulletWidth = 50;
   int bulletHeight = 50;
   // Map management
   private MapManager mapManager;
   private BufferedImage backgroundImage;
   private BufferedImage obstacleImage;
   // Variables to randomize obstacles
   private ArrayList<Point> obstaclePositions;
   private Random random;
   
   // Constructor initializes game panel and menu
   public GamePanel() {
       // Initialize Random first
       random = new Random();
       
       // Initialize map manager and load assets
       mapManager = new MapManager();
       obstaclePositions = new ArrayList<>();
       
       // Panel configuration
       setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
       setBackground(Color.WHITE);
       setFocusable(true);
       addKeyListener(this);
       
       // Create menu first
       menu = new Menu(this);
       setLayout(new BorderLayout());
       add(menu, BorderLayout.CENTER);
       
       // Create players
       playerLeft = new Player(50, GAME_HEIGHT/2, 25, 100, GAME_HEIGHT, false);
       playerRight = new Player(GAME_WIDTH - 75, GAME_HEIGHT/2, 25, 100, GAME_HEIGHT, false);
       
       // Initialize score and thread
       score = new Score();
       gameThread = new Thread(this);
       
       // Load map assets (which will also generate obstacle positions)
       loadMapAssets();
   }
   //Starts the game thread when game begins
   public void startGame() {
       // Remove menu and show game
       removeAll();
       setLayout(new BorderLayout());
       
       gameStarted = true;
       this.requestFocusInWindow();
       gameThread.start();
   }
   // Paints the game components
   public void paint(Graphics g) {
       super.paint(g);
       
       // Only paint game objects if game has started
       if (gameStarted) {
           image = createImage(getWidth(), getHeight());
           graphics = image.getGraphics();
           draw(graphics);
           g.drawImage(image, 0, 0, this);
       }
   }
   // Draws all game objects
   public void draw(Graphics g) {
       // Draw background
       if (backgroundImage != null) {
           g.drawImage(backgroundImage, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
       } else {
           g.setColor(Color.WHITE);
           g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
       }
       
       // Draw obstacles
       if (obstacleImage != null && obstaclePositions != null) {
           for (Point p : obstaclePositions) {
               g.drawImage(obstacleImage, p.x, p.y, null);
           }
       }
       
       // Draw game objects
       playerLeft.draw(g);
       playerRight.draw(g);
       if (bulletLeft != null) {
           bulletLeft.draw(g);
       }
       if (bulletRight != null) {
           bulletRight.draw(g);
       }
       score.draw(g, GAME_WIDTH, GAME_HEIGHT);
   }
   // Load map assets
   private void loadMapAssets() {
       try {
           // Load images
           backgroundImage = ImageIO.read(
               getClass().getResourceAsStream(mapManager.getBackgroundImage())
           );
           obstacleImage = ImageIO.read(
               getClass().getResourceAsStream(mapManager.getObstacleImage())
           );
           
           // Generate positions after images are loaded
           generateObstaclePositions();
           
       } catch (IOException e) {
           System.err.println("Error loading map assets: " + e.getMessage());
           backgroundImage = null;
           obstacleImage = null;
       }
   }
   
   // Generate random positions for obstacles to spawn in
   private void generateObstaclePositions() {
	    // Clear existing positions
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

   // Updates positions of game objects
   public void move() {
       playerLeft.move();
       playerRight.move();
       // Move bullets
       if (bulletLeft != null) {
           bulletLeft.move();
           // Remove bullet if out of bounds
           if (bulletLeft.isOutOfBounds(GAME_WIDTH)) {
               bulletLeft = null;
           }
       }
       if (bulletRight != null) {
           bulletRight.move();
           // Remove bullet if out of bounds
           if (bulletRight.isOutOfBounds(GAME_WIDTH)) {
               bulletRight = null;
           }
       }
   }
   // Checks and handles game object collisions
   public void checkCollision() {
	   // Check bullet collisions with players and update scores
       if (bulletLeft != null && bulletLeft.collidesWith(playerRight)) {
           score.scoreLeftPlayer(); // Left player scores
           bulletLeft = null;
       }
       if (bulletRight != null && bulletRight.collidesWith(playerLeft)) {
           score.scoreRightPlayer(); // Right player scores
           bulletRight = null;
       }
   }
   // Primary game loop
   public void run() {
       // Game loop
       long lastTime = System.nanoTime();
       double amountOfTicks = 60.0;
       double ns = 1000000000 / amountOfTicks;
       double delta = 0;
       while(gameStarted) {
           long now = System.nanoTime();
           delta += (now - lastTime) / ns;
           lastTime = now;
           if(delta >= 1) {
               move();
               checkCollision();
               repaint();
               delta--;
           }
       }
   }
   // Method to reset the game/map
   public void resetGame() {
       score.reset();
       mapManager.randomizeMap();
       loadMapAssets();
       
       playerLeft = new Player(50, GAME_HEIGHT/2, 25, 100, GAME_HEIGHT, false);
       playerRight = new Player(GAME_WIDTH - 75, GAME_HEIGHT/2, 25, 100, GAME_HEIGHT, false);
       
       bulletLeft = null;
       bulletRight = null;
       
       repaint();
   }
   // Handles key press events
   public void keyPressed(KeyEvent e) {
       // Only process game keys if game has started
       if (!gameStarted) {
           return;
       }
       // Existing key press logic
       switch(e.getKeyCode()) {
           case KeyEvent.VK_W:
               playerLeft.shoot(System.currentTimeMillis());
               if (bulletLeft == null) {
                   bulletLeft = new Bullet(playerLeft.x + playerLeft.width,
                                           playerLeft.y + playerLeft.height/2,
                                           bulletWidth, bulletHeight, true);
               }
               playerLeft.setYDirection(0);
               break;
           case KeyEvent.VK_UP:
               playerRight.shoot(System.currentTimeMillis());
               if (bulletRight == null) {
                   bulletRight = new Bullet(playerRight.x,
                                            playerRight.y + playerRight.height/2,
                                            bulletWidth, bulletHeight, false);
               }
               playerRight.setYDirection(0);
               break;
       }
   }
   // Handles key release events
   public void keyReleased(KeyEvent e) {
       // Only process game keys if game has started
       if (!gameStarted) {
           return;
       }
       switch(e.getKeyCode()) {
           case KeyEvent.VK_W:
               playerLeft.resumeMovement(System.currentTimeMillis());
               break;
           case KeyEvent.VK_UP:
               playerRight.resumeMovement(System.currentTimeMillis());
               break;
       }
   }
   // Handles key typed events (not used in this program, but must be initialized)
   public void keyTyped(KeyEvent e) {
       // Not used
   }
}
