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
import java.util.ArrayList;
import java.util.Iterator;
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
   private Obstacle obstacle;
   private Powerup powerup;
   // Menu and game state
   public Menu menu;
   private boolean gameStarted = false;
   // Bullet dimensions
   int bulletWidth = 50;
   int bulletHeight = 50;
   // Map management
   private MapManager mapManager;
   private BufferedImage backgroundImage;
   // Turn and bullet management
   private long lastBulletClearTime;
   private static final long BULLET_RESET_DELAY = 1000; // 1 second delay
   private boolean canShoot = true;
   private boolean firstPlayerHasShot = false;
   private boolean secondPlayerHasShot = false;
   private Player firstShootingPlayer = null;
   private Player secondShootingPlayer = null;
   // Shooting animation timing
   private boolean isLeftPlayerShooting = false;
   private boolean isRightPlayerShooting = false;
   private static final long SHOOT_PAUSE_DURATION = 500; // 0.5 seconds pause for shooting
   private long leftPlayerShootStartTime = 0;
   private long rightPlayerShootStartTime = 0;
   
   // Constructor initializes game panel and menu
   public GamePanel() {
        // Initialize map manager and pass to Obstacle, Powerup
        mapManager = new MapManager();
        obstacle = new Obstacle(GAME_WIDTH, GAME_HEIGHT, mapManager);
        powerup = new Powerup(GAME_WIDTH, GAME_HEIGHT, mapManager);
        
        obstacle.setPowerup(powerup);
        powerup.setObstacle(obstacle);

        // Set initial positions
        obstacle.generateObstaclePositions();
        powerup.generatePowerupPositions(obstacle.getObstaclePositions());
        
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
    
    // Draw powerups
    powerup.draw(g);
    
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

   // Manages shooting logic for both players
   private void handlePlayerShoot(Player shooter, Player otherPlayer, boolean isLeftPlayer) {
        if (canShoot && shooter.hasGun()) {
            // Handle first player's shot
            if (!firstPlayerHasShot) {
                firstPlayerHasShot = true;
                firstShootingPlayer = shooter;
                shooter.setHasGun(false);
                
                if (isLeftPlayer) {
                    cleanupBullet(bulletLeft);  // Clean up old bullet if it exists
                    isLeftPlayerShooting = true;
                    leftPlayerShootStartTime = System.currentTimeMillis();
                    bulletLeft = new Bullet(
                        shooter.x + shooter.width,
                        shooter.y + shooter.height/2,
                        bulletWidth, bulletHeight,
                        true
                    );
                } else {
                    cleanupBullet(bulletRight);  // Clean up old bullet if it exists
                    isRightPlayerShooting = true;
                    rightPlayerShootStartTime = System.currentTimeMillis();
                    bulletRight = new Bullet(
                        shooter.x - bulletWidth,
                        shooter.y + shooter.height/2,
                        bulletWidth, bulletHeight,
                        false
                    );
                }
                shooter.shoot(System.currentTimeMillis());
            }
            // Handle second player's shot
            else if (!secondPlayerHasShot && shooter != firstShootingPlayer) {
                secondPlayerHasShot = true;
                secondShootingPlayer = shooter;
                shooter.setHasGun(false);
                
                if (isLeftPlayer) {
                    cleanupBullet(bulletLeft);  // Clean up old bullet if it exists
                    isLeftPlayerShooting = true;
                    leftPlayerShootStartTime = System.currentTimeMillis();
                    bulletLeft = new Bullet(
                        shooter.x + shooter.width,
                        shooter.y + shooter.height/2,
                        bulletWidth, bulletHeight,
                        true
                    );
                } else {
                    cleanupBullet(bulletRight);  // Clean up old bullet if it exists
                    isRightPlayerShooting = true;
                    rightPlayerShootStartTime = System.currentTimeMillis();
                    bulletRight = new Bullet(
                        shooter.x - bulletWidth,
                        shooter.y + shooter.height/2,
                        bulletWidth, bulletHeight,
                        false
                    );
                }
                shooter.shoot(System.currentTimeMillis());
                canShoot = false;
            }
        } else if (!shooter.hasGun()) {
            shooter.setYDirection(-shooter.getYDirection());
            shooter.move();
        }
    }

   // Handles bullet clearing and turn management
   private void handleBulletCleared() {
       // Check if both bullets are gone after both players shot
       if (firstPlayerHasShot && secondPlayerHasShot && 
           bulletLeft == null && bulletRight == null && !canShoot) {
           lastBulletClearTime = System.currentTimeMillis();
       }
   }

   // Resets bullet and turn state for new round
   private void resetBullets() {
       canShoot = true;
       firstPlayerHasShot = false;
       secondPlayerHasShot = false;
       firstShootingPlayer = null;
       secondShootingPlayer = null;
       playerLeft.setHasGun(true);
       playerRight.setHasGun(true);
   }

   // Removes bullets from the game when necessary
   private void cleanupBullet(Bullet bullet) {
        if (bullet != null) {
            // Always unfreeze the tracked player when cleaning up a bullet
            if (bullet.hasFreezeEffect() && bullet.getPlayerToUnfreeze() != null) {
                bullet.getPlayerToUnfreeze().unfreeze();
            }
        }
    }

   // Checks and handles game object collisions
   public void checkCollision() {
        // Check bullet collisions with players
        if (bulletLeft != null) {
            if (bulletLeft.collidesWithAny(playerRight)) {
                score.scoreLeftPlayer();
                cleanupBullet(bulletLeft);
                bulletLeft = null;
                handleBulletCleared();
            } else if (bulletLeft.collidesWithAny(playerLeft)) {
                score.scoreRightPlayer();
                cleanupBullet(bulletLeft);
                bulletLeft = null;
                handleBulletCleared();
            } else if (bulletLeft.isAnyBulletOutOfBounds(GAME_WIDTH)) {
                cleanupBullet(bulletLeft);
                bulletLeft = null;
                handleBulletCleared();
            }
        }
        
        if (bulletRight != null) {
            if (bulletRight.collidesWithAny(playerLeft)) {
                score.scoreRightPlayer();
                cleanupBullet(bulletRight);
                bulletRight = null;
                handleBulletCleared();
            } else if (bulletRight.collidesWithAny(playerRight)) {
                score.scoreLeftPlayer();
                cleanupBullet(bulletRight);
                bulletRight = null;
                handleBulletCleared();
            } else if (bulletRight.isAnyBulletOutOfBounds(GAME_WIDTH)) {
                cleanupBullet(bulletRight);
                bulletRight = null;
                handleBulletCleared();
            }
        }
    // Store powerups to remove in a separate list
ArrayList<Point> powerupsToRemove = new ArrayList<>();

// Check bullet collisions with powerups
for (Point powerupPosition : new ArrayList<>(powerup.getPowerupPositions())) {
    Point powerupCenter = powerup.getCircleCenter(powerupPosition);

    if (bulletLeft != null) {
        Point bulletCenter = new Point(
            bulletLeft.x + bulletLeft.width/2,
            bulletLeft.y + bulletLeft.height/2
        );
        Point bulletPrevCenter = new Point(
            bulletLeft.getPreviousX() + bulletLeft.width/2,
            bulletLeft.getPreviousY() + bulletLeft.height/2
        );

        if (powerup.lineIntersectsCircle(powerupCenter, bulletPrevCenter, bulletCenter)) {
            String powerupType = powerup.activatePowerup(powerupPosition, bulletLeft, playerRight);
            powerupsToRemove.add(powerupPosition);
        }
    }

    if (bulletRight != null) {
        Point bulletCenter = new Point(
            bulletRight.x + bulletRight.width/2,
            bulletRight.y + bulletRight.height/2
        );
        Point bulletPrevCenter = new Point(
            bulletRight.getPreviousX() + bulletRight.width/2,
            bulletRight.getPreviousY() + bulletRight.height/2
        );

        if (powerup.lineIntersectsCircle(powerupCenter, bulletPrevCenter, bulletCenter)) {
            String powerupType = powerup.activatePowerup(powerupPosition, bulletRight, playerLeft);
            powerupsToRemove.add(powerupPosition);
        }
    }
}

// Remove the powerups after iteration is complete
powerup.getPowerupPositions().removeAll(powerupsToRemove);

    // Check bullet collisions with obstacles
    Iterator<Point> obstacleIterator = obstacle.getObstaclePositions().iterator();
    while (obstacleIterator.hasNext()) {
        Point obstaclePosition = obstacleIterator.next();
        Point obstacleCenter = obstacle.getCircleCenter(obstaclePosition);

        if (bulletLeft != null) {
            Boolean isend;

            isend = bulletLeft.bulletBounce(obstacleCenter, obstaclePosition, obstacle, bulletLeft);

            if(isend){
                break;
            }
        }

        if (bulletRight != null) {
            Boolean isend;

            isend = bulletRight.bulletBounce(obstacleCenter, obstaclePosition, obstacle, bulletRight);

            if(isend){
                break;
            }
        }
    }

    // Update obstacles (check for regeneration)
    obstacle.update(powerup.getPowerupPositions());
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
            // Fix: Always pass powerup positions when calling update
            obstacle.update(powerup.getPowerupPositions());
            powerup.update(obstacle.getObstaclePositions());
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
        
        // Clean up any existing bullets before nullifying them
        cleanupBullet(bulletLeft);
        cleanupBullet(bulletRight);
        bulletLeft = null;
        bulletRight = null;
        
        canShoot = true;
        firstPlayerHasShot = false;
        secondPlayerHasShot = false;
        firstShootingPlayer = null;
        secondShootingPlayer = null;
        
        // Make sure players are unfrozen when game resets
        playerLeft.unfreeze();
        playerRight.unfreeze();
        
        powerup.regeneratePowerups(obstacle.getObstaclePositions());
        
        repaint();
    }

   // Handles key press events
   public void keyPressed(KeyEvent e) {
        if (!gameStarted) {
            return;
        }
        
        switch(e.getKeyCode()) {
            case KeyEvent.VK_W:
                handlePlayerShoot(playerLeft, playerRight, true);
                break;
            case KeyEvent.VK_UP:
                handlePlayerShoot(playerRight, playerLeft, false);
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

   // Handles key typed events (not used in this program)
   public void keyTyped(KeyEvent e) {
       // Not used
   }
}