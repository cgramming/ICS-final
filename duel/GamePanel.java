/*
* Swapnil Kabir and Syed Bazif Shah
* Date: December 16, 2024
* Description: GamePanel class manages game objects, rendering,
* and primary game loop for the Duel game.
*/

import java.awt.*;
import java.awt.event.*;
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
   //Bullet Dimensions
   int bulletWidth = 100;
   int bulletHeight = 100;
   
   // Constructor initializes game panel and menu
   public GamePanel() {
       // Panel configuration
       setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
       setBackground(Color.WHITE);
       setFocusable(true);
       addKeyListener(this);
       // Create menu first
       menu = new Menu(this);
       setLayout(new BorderLayout());
       add(menu, BorderLayout.CENTER);
       // Create players (but don't start game yet)
       playerLeft = new Player(50, GAME_HEIGHT/2, 25, 100, GAME_HEIGHT, false);
       playerRight = new Player(GAME_WIDTH - 75, GAME_HEIGHT/2, 25, 100, GAME_HEIGHT, false);
       // Prepare game thread (but don't start)
       gameThread = new Thread(this);
       score = new Score();
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
       playerLeft.draw(g);
       playerRight.draw(g);
       // Draw bullets if they exist
       if (bulletLeft != null) {
           bulletLeft.draw(g);
       }
       if (bulletRight != null) {
           bulletRight.draw(g);
       }
       // Draw scores
       score.draw(g, GAME_WIDTH, GAME_HEIGHT);
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
               break;
           case KeyEvent.VK_UP:
               playerRight.shoot(System.currentTimeMillis());
               if (bulletRight == null) {
                   bulletRight = new Bullet(playerRight.x,
                                            playerRight.y + playerRight.height/2,
                                            bulletWidth, bulletHeight, false);
               }
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
