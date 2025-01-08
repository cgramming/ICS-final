/*
* Swapnil Kabir and Syed Bazif Shah
* Date: December 16, 2024
* Description: GamePanel class manages game objects, rendering,
* and primary game loop for the Duel game.
*/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

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
   private Obstacle obstacle;
   // Turn management
   private long lastBulletClearTime;
   private static final long BULLET_RESET_DELAY = 1000; // 1 second delay
   private boolean canShoot = true;
   private boolean leftPlayerTurn = true;
   private boolean isLeftPlayerShooting = false;
   private boolean isRightPlayerShooting = false;
   private static final long SHOOT_PAUSE_DURATION = 500; // 0.5 seconds pause for shooting
   private long leftPlayerShootStartTime = 0;
   private long rightPlayerShootStartTime = 0;
   
   // Constructor initializes game panel and menu
   public GamePanel() {
	    // Initialize map manager and pass to Obstacle
	    mapManager = new MapManager();
	    obstacle = new Obstacle(GAME_WIDTH, GAME_HEIGHT, mapManager);
	    
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
	    playerLeft = new Player(50, GAME_HEIGHT / 2, 25, 100, GAME_HEIGHT, true);
	    playerRight = new Player(GAME_WIDTH - 75, GAME_HEIGHT / 2, 25, 100, GAME_HEIGHT, true);
	    
	    // Initialize score and thread
	    score = new Score();
	    gameThread = new Thread(this);
	    
	    // Load map assets
	    loadMapAssets();
	}

   // Starts the game thread when game begins
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
       obstacle.draw(g);
       
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
	        // Load background and obstacle assets via MapManager
	        backgroundImage = ImageIO.read(
	            getClass().getResourceAsStream(mapManager.getBackgroundImage())
	        );
	        obstacle.generateObstaclePositions();
	    } catch (IOException e) {
	        System.err.println("Error loading map assets: " + e.getMessage());
	        backgroundImage = null;
	    }
	}

   // Updates positions of game objects
   public void move() {
        // Handle left player shooting pause
        if (isLeftPlayerShooting) {
            if (System.currentTimeMillis() - leftPlayerShootStartTime >= SHOOT_PAUSE_DURATION) {
                isLeftPlayerShooting = false;
                playerLeft.resumeMovement(System.currentTimeMillis());
            }
        } else {
            playerLeft.move();
        }

        // Handle right player shooting pause
        if (isRightPlayerShooting) {
            if (System.currentTimeMillis() - rightPlayerShootStartTime >= SHOOT_PAUSE_DURATION) {
                isRightPlayerShooting = false;
                playerRight.resumeMovement(System.currentTimeMillis());
            }
        } else {
            playerRight.move();
        }

        // Move bullets
        if (bulletLeft != null) {
            bulletLeft.move();
            if (bulletLeft.isOutOfBounds(GAME_WIDTH)) {
                bulletLeft = null;
                handleBulletCleared();
            }
        }
        if (bulletRight != null) {
            bulletRight.move();
            if (bulletRight.isOutOfBounds(GAME_WIDTH)) {
                bulletRight = null;
                handleBulletCleared();
            }
        }

        // Check if it's time to reset bullets
        if (!canShoot && System.currentTimeMillis() - lastBulletClearTime >= BULLET_RESET_DELAY) {
            resetBullets();
        }
    }

   // Handle bullet clearing and turn management
   private void handleBulletCleared() {
       if ((bulletLeft == null && bulletRight == null) && !canShoot) {
           lastBulletClearTime = System.currentTimeMillis();
       }
   }

   // Reset bullets and turn state
   private void resetBullets() {
       canShoot = true;
       leftPlayerTurn = true;
       playerLeft.setHasGun(true);
       playerRight.setHasGun(true);
   }

   // Checks and handles game object collisions
   public void checkCollision() {
       // Check bullet collisions with players
       if (bulletLeft != null) {
           if (bulletLeft.collidesWith(playerRight)) {
               score.scoreLeftPlayer(); // Left player scores
               bulletLeft = null;
               handleBulletCleared();
           } else if (bulletLeft.collidesWith(playerLeft)) {
               score.scoreRightPlayer(); // Right player scores when left player hits themselves
               bulletLeft = null;
               handleBulletCleared();
           }
       }
       
       if (bulletRight != null) {
           if (bulletRight.collidesWith(playerLeft)) {
               score.scoreRightPlayer(); // Right player scores
               bulletRight = null;
               handleBulletCleared();
           } else if (bulletRight.collidesWith(playerRight)) {
               score.scoreLeftPlayer(); // Left player scores when right player hits themselves
               bulletRight = null;
               handleBulletCleared();
           }
       }

       // Check bullet collisions with obstacles
       for (Point obstaclePosition : obstacle.getObstaclePositions()) {
           Rectangle obstacleBounds = new Rectangle(
               obstaclePosition.x, 
               obstaclePosition.y, 
               obstacle.getObstacleImage().getWidth(), 
               obstacle.getObstacleImage().getHeight()
           );

           if (bulletLeft != null && bulletLeft.getBounds().intersects(obstacleBounds)) {
               bulletLeft.bounceOffObstacle(obstacleBounds);
           }

           if (bulletRight != null && bulletRight.getBounds().intersects(obstacleBounds)) {
               bulletRight.bounceOffObstacle(obstacleBounds);
           }
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
	    
	    playerLeft = new Player(50, GAME_HEIGHT / 2, 25, 100, GAME_HEIGHT, true);
	    playerRight = new Player(GAME_WIDTH - 75, GAME_HEIGHT / 2, 25, 100, GAME_HEIGHT, true);
	    
	    bulletLeft = null;
	    bulletRight = null;
	    
	    canShoot = true;
	    leftPlayerTurn = true;
	    
	    repaint();
	}

   // Handles key press events
   
    public void keyPressed(KeyEvent e) {
        if (!gameStarted) {
            return;
        }
        
        switch(e.getKeyCode()) {
            case KeyEvent.VK_W:
                if (canShoot && leftPlayerTurn && playerLeft.hasGun()) {
                    // Initiate shooting sequence for left player
                    isLeftPlayerShooting = true;
                    leftPlayerShootStartTime = System.currentTimeMillis();
                    playerLeft.setYDirection(0);
                    
                    // Create bullet after brief pause
                    bulletLeft = new Bullet(
                        playerLeft.x + playerLeft.width,
                        playerLeft.y + playerLeft.height/2,
                        bulletWidth, bulletHeight,
                        true
                    );
                    playerLeft.shoot(System.currentTimeMillis());
                    leftPlayerTurn = false;
                } else if (!playerLeft.hasGun()) {
                    // Change direction when no gun
                    playerLeft.reverseDirection();
                }
                break;

            case KeyEvent.VK_UP:
                if (canShoot && !leftPlayerTurn && playerRight.hasGun()) {
                    // Initiate shooting sequence for right player
                    isRightPlayerShooting = true;
                    rightPlayerShootStartTime = System.currentTimeMillis();
                    playerRight.setYDirection(0);
                    
                    // Create bullet after brief pause
                    bulletRight = new Bullet(
                        playerRight.x - bulletWidth,
                        playerRight.y + playerRight.height/2,
                        bulletWidth, bulletHeight,
                        false
                    );
                    playerRight.shoot(System.currentTimeMillis());
                    canShoot = false;
                } else if (!playerRight.hasGun()) {
                    // Change direction when no gun
                    playerRight.reverseDirection();
                }
                break;
        }
    }

   // Handles key release events
   public void keyReleased(KeyEvent e) {
        if (!gameStarted) {
            return;
        }
        
        // Only resume movement if not in shooting animation
        switch(e.getKeyCode()) {
            case KeyEvent.VK_W:
                if (!isLeftPlayerShooting) {
                    playerLeft.resumeMovement(System.currentTimeMillis());
                }
                break;
            case KeyEvent.VK_UP:
                if (!isRightPlayerShooting) {
                    playerRight.resumeMovement(System.currentTimeMillis());
                }
                break;
        }
    }

   // Handles key typed events (not used in this program, but must be initialized)
   public void keyTyped(KeyEvent e) {
       // Not used
   }
}