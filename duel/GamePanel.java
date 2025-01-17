/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 17, 2025
* Description: GamePanel class manages game objects, rendering,
* and primary game loop for the Duel game.
*/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
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
   private PauseMenu pauseMenu;
   private EndScreen endScreen;
   private boolean isPaused = false;
   private boolean gameStarted = false;
   private SoundManager soundManager;
   // Bullet dimensions
   int bulletWidth = 50;
   int bulletHeight = 50;
   // Map management
   private MapManager mapManager;
   private BufferedImage backgroundImage;
   // Turn and bullet management
   private long lastBulletClearTime;
   private static final long BULLET_RESET_DELAY = 1000; // 0.5 second delay before players get their guns back
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
       // Initialize sound manager
       soundManager = new SoundManager();
       soundManager.playBackgroundMusic(); 

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
       
       // Use null layout for proper overlay positioning
       setLayout(null);
       
       // Create menu components
       menu = new Menu(this);
       pauseMenu = new PauseMenu(this);
       endScreen = new EndScreen(this);
       
       // Set bounds for all components
       menu.setBounds(0, 0, GAME_WIDTH, GAME_HEIGHT);
       pauseMenu.setBounds(0, 0, GAME_WIDTH, GAME_HEIGHT);
       endScreen.setBounds(0, 0, GAME_WIDTH, GAME_HEIGHT);
       
       // Add components in correct order
       add(menu);
       add(pauseMenu);
       add(endScreen);
       
       // Set initial visibility
       menu.setVisible(true);
       pauseMenu.setVisible(false);
       endScreen.setVisible(false);
       
       // Initialize score and thread
       score = new Score();
       gameThread = new Thread(this);
       
       // Load map assets
       loadMapAssets();

       // Initialize game objects (but don't start movement yet)
       initializeGameObjects();
   }

   // Starts the game thread when game begins and plays associated sound effects
   public void startGame() {
       soundManager.playGameStart();
       soundManager.adjustBackgroundMusicVolume(false);
       menu.setVisible(false);
       pauseMenu.setVisible(true); // Make pause menu visible but not paused
       endScreen.setVisible(false);
       gameStarted = true;
       isPaused = false;
       
       // Initialize/reset game state
       initializeGameObjects();
       score.reset();
       
       // Start game thread if not already running
       if (!gameThread.isAlive()) {
           gameThread = new Thread(this);
           gameThread.start();
       }
       
       this.requestFocusInWindow();
   }
   
// Initialize all game objects
   private void initializeGameObjects() {
        playerLeft = new Player(50, GAME_HEIGHT / 2, 25, 100, GAME_HEIGHT, true);
        playerRight = new Player(GAME_WIDTH - 75, GAME_HEIGHT / 2, 25, 100, GAME_HEIGHT, true);
        bulletLeft = null;
        bulletRight = null;
        
        // Reset shooting states
        canShoot = true;
        firstPlayerHasShot = false;
        secondPlayerHasShot = false;
        firstShootingPlayer = null;
        secondShootingPlayer = null;
        
        // Ensure both players start with guns
        playerLeft.setHasGun(true);
        playerRight.setHasGun(true);
    }
   
   // Paints the game components
   public void paint(Graphics g) {
       super.paint(g);
       
       if (gameStarted) {
           image = createImage(getWidth(), getHeight());
           graphics = image.getGraphics();
           draw(graphics);
           g.drawImage(image, 0, 0, this);
       }
       
       // Paint overlays last
       paintChildren(g);
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
                    cleanupBullet(bulletLeft);
                    isLeftPlayerShooting = true;
                    leftPlayerShootStartTime = System.currentTimeMillis();
                    bulletLeft = new Bullet(
                        shooter.x + shooter.width,
                        shooter.y + shooter.height/2,
                        bulletWidth, bulletHeight,
                        true
                    );
                    soundManager.playBulletSound();
                } else {
                    cleanupBullet(bulletRight);
                    isRightPlayerShooting = true;
                    rightPlayerShootStartTime = System.currentTimeMillis();
                    bulletRight = new Bullet(
                        shooter.x - bulletWidth,
                        shooter.y + shooter.height/2,
                        bulletWidth, bulletHeight,
                        false
                    );
                    soundManager.playBulletSound();
                }
                shooter.shoot(System.currentTimeMillis());
            }
            // Handle second player's shot
            else if (!secondPlayerHasShot && shooter != firstShootingPlayer) {
                secondPlayerHasShot = true;
                secondShootingPlayer = shooter;
                shooter.setHasGun(false);
                
                if (isLeftPlayer) {
                    cleanupBullet(bulletLeft);
                    isLeftPlayerShooting = true;
                    leftPlayerShootStartTime = System.currentTimeMillis();
                    bulletLeft = new Bullet(
                        shooter.x + shooter.width,
                        shooter.y + shooter.height/2,
                        bulletWidth, bulletHeight,
                        true
                    );
                    soundManager.playBulletSound();
                } else {
                    cleanupBullet(bulletRight);
                    isRightPlayerShooting = true;
                    rightPlayerShootStartTime = System.currentTimeMillis();
                    bulletRight = new Bullet(
                        shooter.x - bulletWidth,
                        shooter.y + shooter.height/2,
                        bulletWidth, bulletHeight,
                        false
                    );
                    soundManager.playBulletSound();
                }
                shooter.shoot(System.currentTimeMillis());
                canShoot = false;  // Prevent further shooting until bullets are cleared
            }
        } else if (!shooter.hasGun()) {
            shooter.setYDirection(-shooter.getYDirection());
            shooter.move();
        }
    }


   // Handles bullet clearing and turn management
   private void handleBulletCleared() {
        boolean leftBulletCleared = bulletLeft == null || 
            (bulletLeft != null && !bulletLeft.hasSplitBullets());
        boolean rightBulletCleared = bulletRight == null || 
            (bulletRight != null && !bulletRight.hasSplitBullets());
            
        // Only reset when both players have shot AND all bullets are cleared
        if (firstPlayerHasShot && secondPlayerHasShot && 
            leftBulletCleared && rightBulletCleared) {
            lastBulletClearTime = System.currentTimeMillis();
            // Wait for BULLET_RESET_DELAY before resetting
            if (System.currentTimeMillis() - lastBulletClearTime >= BULLET_RESET_DELAY) {
                resetBullets();
            }
        }
    }

   // Resets bullet and turn state for new round
    private void resetBullets() {
        // Only reset if both bullets are cleared
        boolean leftBulletCleared = bulletLeft == null || 
            (bulletLeft != null && !bulletLeft.hasSplitBullets());
        boolean rightBulletCleared = bulletRight == null || 
            (bulletRight != null && !bulletRight.hasSplitBullets());
            
        if (leftBulletCleared && rightBulletCleared) {
            // Reset all shooting states
            canShoot = true;
            firstPlayerHasShot = false;
            secondPlayerHasShot = false;
            firstShootingPlayer = null;
            secondShootingPlayer = null;
            
            // Give BOTH players their guns back simultaneously
            playerLeft.setHasGun(true);
            playerRight.setHasGun(true);
            
            // Reset bullets
            bulletLeft = null;
            bulletRight = null;
        }
    }

   // Removes bullets from the game when necessary
   private void cleanupBullet(Bullet bullet) {
        if (bullet != null && bullet.hasFreezeEffect()) {
            Player playerToUnfreeze = bullet.getPlayerToUnfreeze();
            if (playerToUnfreeze != null) {
                // Clear reference to freeze effect
                bullet.setFreezeEffect(false, null);
            }
        }
    }

   // Checks and handles game object collisions
   public void checkCollision() {
    // Handle main bullet collisions
    handleBulletCollisions(bulletLeft);
    handleBulletCollisions(bulletRight);
    
    // Check if either player has reached 10 points to trigger end game condition
    if (score.getLeftPlayerScore() >= 10 || score.getRightPlayerScore() >= 10) {
        checkWinCondition();
        }
    }

    // Handle bullet collisions
    private void handleBulletCollisions(Bullet bullet) {
    if (bullet == null) return;
    
    // Create list of bullets to process (main bullet and any split bullets)
    ArrayList<Bullet> bulletsToProcess = new ArrayList<>();
    bulletsToProcess.add(bullet);
    if (bullet.hasSplitBullets()) {
        bulletsToProcess.addAll(bullet.getSplitBullets());
    }
    
    // Track bullets that need to be removed
    ArrayList<Bullet> bulletsToRemove = new ArrayList<>();
    
    // Process each bullet individually
    for (Bullet currentBullet : bulletsToProcess) {
        boolean shouldRemoveBullet = false;
        
        // Check player collisions
        if (currentBullet.isFromLeftPlayer()) {
            if (currentBullet.collidesWith(playerRight)) {
                score.scoreLeftPlayer();
                shouldRemoveBullet = true;
            } else if (currentBullet.collidesWith(playerLeft)) {
                score.scoreRightPlayer();
                shouldRemoveBullet = true;
            }
        } else {
            if (currentBullet.collidesWith(playerLeft)) {
                score.scoreRightPlayer();
                shouldRemoveBullet = true;
            } else if (currentBullet.collidesWith(playerRight)) {
                score.scoreLeftPlayer();
                shouldRemoveBullet = true;
            }
        }
        
        if (!shouldRemoveBullet) {  // Only process further if bullet hasn't hit a player
            // Check powerup collisions
            for (Point powerupPosition : new ArrayList<>(powerup.getPowerupPositions())) {
                Point powerupCenter = powerup.getCircleCenter(powerupPosition);
                Point bulletCenter = new Point(
                    currentBullet.x + currentBullet.width/2,
                    currentBullet.y + currentBullet.height/2
                );
                Point bulletPrevCenter = new Point(
                    currentBullet.getPreviousX() + currentBullet.width/2,
                    currentBullet.getPreviousY() + currentBullet.height/2
                );

                if (powerup.lineIntersectsCircle(powerupCenter, bulletPrevCenter, bulletCenter)) {
                    String powerupType = powerup.activatePowerup(powerupPosition, currentBullet, 
                        currentBullet.isFromLeftPlayer() ? playerRight : playerLeft);
                    powerup.getPowerupPositions().remove(powerupPosition);
                    
                    // Play appropriate powerup sound
                    switch (powerupType) {
                        case "Bomb":
                            soundManager.playBombSound();
                            break;
                        case "Freeze":
                            soundManager.playFreezeSound();
                            break;
                        case "BigBullet":
                            soundManager.playBigBulletSound();
                            break;
                    }
                    break;
                }
            }
            
            // Check obstacle collisions
            for (Point obstaclePosition : new ArrayList<>(obstacle.getObstaclePositions())) {
                Point obstacleCenter = obstacle.getCircleCenter(obstaclePosition);
                if (currentBullet.bulletBounce(obstacleCenter, obstaclePosition, obstacle, currentBullet)) {
                    soundManager.playObstacleBounce();  // Play sound when bullet bounces off obstacle
                    break;
                }
            }
            
            // Check if bullet is out of bounds
            if (currentBullet.isOutOfBounds(GAME_WIDTH)) {
                shouldRemoveBullet = true;
            }
        }
        
        // Add bullet to removal list if necessary
        if (shouldRemoveBullet) {
            bulletsToRemove.add(currentBullet);
        }
    }
    
    // Remove bullets that need to be removed
    for (Bullet bulletToRemove : bulletsToRemove) {
        if (bulletToRemove == bullet) {
            // This is the main bullet
            if (bullet == bulletLeft) {
                bulletLeft = null;
            } else {
                bulletRight = null;
            }
        } else {
            // This is a split bullet, remove it from its parent's split bullets list
            bullet.getSplitBullets().remove(bulletToRemove);
        }
    }
    
    // Check if we need to handle bullet cleared
    if ((bulletLeft == null || 
         (bulletLeft != null && !bulletLeft.hasSplitBullets())) && 
        (bulletRight == null || 
         (bulletRight != null && !bulletRight.hasSplitBullets()))) {
        handleBulletCleared();
    }
}

   // Primary game loop
   public void run() {
       long lastTime = System.nanoTime();
       double amountOfTicks = 60.0;
       double ns = 1000000000 / amountOfTicks;
       double delta = 0;
       
       while(gameStarted) {
           long now = System.nanoTime();
           delta += (now - lastTime) / ns;
           lastTime = now;
           
           if(delta >= 1) {
               if (!isPaused) {
                   move();
                   obstacle.update(powerup.getPowerupPositions());
                   powerup.update(obstacle.getObstaclePositions());
                   checkCollision();
               }
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
       
       // Reset game objects and state
       initializeGameObjects();
       
       // Reset menu state
       isPaused = false;
       pauseMenu.setVisible(true);
       endScreen.setVisible(false);
       gameStarted = true;
       
       // Make sure game thread is running
       if (!gameThread.isAlive()) {
           gameThread = new Thread(this);
           gameThread.start();
       }
       
       this.requestFocusInWindow();
       repaint();
   }
   
   // Pauses the game when the player opens the pause menu
   public void setPaused(boolean paused) {
    this.isPaused = paused;
    soundManager.playPauseAndPlay();
   }

   // Returns directly to the main menu (used when the "Main Menu" button is pressed in the pause menu or end screen
   public void returnToMainMenu() {
       soundManager.adjustBackgroundMusicVolume(true);
       gameStarted = false;
       isPaused = false;
       pauseMenu.setVisible(false);
       endScreen.setVisible(false);
       menu.setVisible(true);
       
       // Reset game state
       score.reset();
       initializeGameObjects();
       
       revalidate();
       repaint();
   }

	// Ends the game once a player reaches 10 points
   private void checkWinCondition() {
       if (score.getLeftPlayerScore() >= 10 || score.getRightPlayerScore() >= 10) {
           //gameStarted = false;
           pauseMenu.setVisible(false);
           
           if (score.getLeftPlayerScore() >= 10) {
               endScreen.showEndScreen("Left Player");
           } else {
               endScreen.showEndScreen("Right Player");
           }
       }
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