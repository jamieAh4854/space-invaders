import java.util.ArrayList;
import java.util.List;

/**
 * GameModel.java
 * 
 * Contains the game state and logic for Space Invaders.
 * This class has no Swing dependencies and handles:
 * - Game entities (player, enemies, bullets)
 * - Game state (running, paused, game over)
 * - Collision detection
 * - Score and lives tracking
 * - Update game state each frame
 */
public class GameModel {
    
    // Game constants
    private static final int GAME_WIDTH = 800;
    private static final int GAME_HEIGHT = 600;
    private static final int PLAYER_WIDTH = 40;
    private static final int PLAYER_HEIGHT = 40;
    private static final int ALIEN_WIDTH = 30;
    private static final int ALIEN_HEIGHT = 30;
    private static final int ALIEN_ROWS = 5;
    private static final int ALIEN_COLS = 11;
    
    // Player state
    private int playerX;
    private int playerY;
    
    // Alien state
    private Alien[][] aliens;
    private int alienDirection;  // 1 for right, -1 for left
    private double alienSpeed;
    private int alienAliveCount = ALIEN_ROWS * ALIEN_COLS;
    
    // Bullets
    final private List<Bullet> playerBullets;
    final private List<Bullet> alienBullets;
    
    // Powerups
    private Powerup activePowerup;
    private boolean tripleShotActive;
    private long tripleShotEndTime;
    
    // Game state
    private int score;
    private int lives;
    private int alienFireCounter;

    //levels
    private int currentLevel = 1;
    
    /**
     * Inner class representing an alien
     */
    public static class Alien {
        public int x, y;
        public boolean alive;
        
        public Alien(int x, int y) {
            this.x = x;
            this.y = y;
            this.alive = true;
        }
    }
    
    /**
     * Inner class representing a powerup
     */
    public static class Powerup {
        public int x, y;
        public int vy;  // falling speed
        
        public Powerup(int x, int y) {
            this.x = x;
            this.y = y;
            this.vy = 3;  // Fall down slowly
        }
    }
    
    /**
     * Inner class representing a bullet
     */
    public static class Bullet {
        public int x, y;
        public int vx, vy;  // velocity
        
        public Bullet(int x, int y, int vx, int vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }
    }
    
    /**
     * Constructor - Initialize game model with default values
     */
    public GameModel() {
        playerX = GAME_WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = GAME_HEIGHT - 60;
        
        alienDirection = 1;
        alienSpeed = 1;
        
        score = 0;
        lives = 3;
        alienFireCounter = 0;
        
        alienBullets = new ArrayList<>();
        playerBullets = new ArrayList<>();
        activePowerup = null;
        tripleShotActive = false;
        tripleShotEndTime = 0;
        
        initializeAliens();
    }
    
    /**
     * Initialize alien formation (5 rows x 11 columns)
     */
    private void initializeAliens() {
        aliens = new Alien[ALIEN_ROWS][ALIEN_COLS];
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                int x = 50 + col * 60;
                int y = 30 + row * 50;
                aliens[row][col] = new Alien(x, y);
            }
        }
        playerBullets.clear();
    }
    
    /**
     * Update game logic for the current frame
     */
    public void update() {
        // Update player bullets
        for (int i = playerBullets.size() - 1; i >= 0; i--) {
            Bullet b = playerBullets.get(i);
            b.y += b.vy;
            if (b.y < 0) {
                playerBullets.remove(i);
            } else {
                checkPlayerBulletCollisions(b);
            }
        }
        
        // Update alien bullets
        for (int i = alienBullets.size() - 1; i >= 0; i--) {
            Bullet b = alienBullets.get(i);
            b.y += b.vy;
            if (b.y > GAME_HEIGHT) {
                alienBullets.remove(i);
            } else {
                checkAlienBulletCollisions(b);
            }
        }
        
        // Update powerup
        if (activePowerup != null) {
            activePowerup.y += activePowerup.vy;
            if (activePowerup.y > GAME_HEIGHT) {
                activePowerup = null;
            } else {
                checkPowerupCollection();
            }
        }
        
        // Check if triple shot powerup has expired
        if (tripleShotActive && System.currentTimeMillis() > tripleShotEndTime) {
            tripleShotActive = false;
        }
        
        // Move aliens and handle formation
        moveAliens();
        
        // Fire alien bullets at random intervals
        fireAlienBullets();
    }
    
    /**
     * Move alien formation right/left, and down when reaching edges
     */
    private void moveAliens() {
        // Check if any alive alien reached the edge
        boolean shouldMoveDown = false;
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                if (aliens[row][col].alive) {
                    if ((alienDirection == 1 && aliens[row][col].x + ALIEN_WIDTH >= GAME_WIDTH - 20) ||
                        (alienDirection == -1 && aliens[row][col].x <= 20)) {
                        shouldMoveDown = true;
                        break;
                    }
                }
            }
            if (shouldMoveDown) break;
        }
        
        if (shouldMoveDown) {
            // Move down and reverse direction
            alienDirection *= -1;
            for (int row = 0; row < ALIEN_ROWS; row++) {
                for (int col = 0; col < ALIEN_COLS; col++) {
                    aliens[row][col].y += 30;
                }
            }
        } else {
            // Move horizontally
            for (int row = 0; row < ALIEN_ROWS; row++) {
                for (int col = 0; col < ALIEN_COLS; col++) {
                    aliens[row][col].x += alienDirection * alienSpeed;
                }
            }
        }
    }
    
    /**
     * Fire alien bullets at random intervals from random aliens
     */
    private void fireAlienBullets() {
        alienFireCounter++;
        if (alienFireCounter > 20) {  // Fire approximately every 20 ticks
            alienFireCounter = 0;
            
            // Find all alive aliens
            List<int[]> aliveAliens = new ArrayList<>();
            for (int row = 0; row < ALIEN_ROWS; row++) {
                for (int col = 0; col < ALIEN_COLS; col++) {
                    if (aliens[row][col].alive) {
                        aliveAliens.add(new int[]{row, col});
                    }
                }
            }
            
            // Pick a random one to fire
            if (!aliveAliens.isEmpty()) {
                int[] chosen = aliveAliens.get((int)(Math.random() * aliveAliens.size()));
                Alien alien = aliens[chosen[0]][chosen[1]];
                alienBullets.add(new Bullet(alien.x + ALIEN_WIDTH / 2, alien.y + ALIEN_HEIGHT, 0, 5));
            }
        }
    }
    
    /**
     * Check if a player bullet hit any aliens
     */
    private void checkPlayerBulletCollisions(Bullet bullet) {
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                Alien alien = aliens[row][col];
                if (alien.alive && checkCollision(bullet.x, bullet.y, 5, 10,
                                                   alien.x, alien.y, ALIEN_WIDTH, ALIEN_HEIGHT)) {
                    alien.alive = false;
                    playerBullets.remove(bullet);
                    score += 10;
                    alienAliveCount--;
                    if (alienAliveCount == 0) {
                        currentLevel++;
                        alienSpeed += 0.5; // Increase speed for next level
                        initializeAliens();
                    }
                    
                    // 30% chance to spawn powerup when alien dies
                    if (Math.random() < 0.3 && activePowerup == null) {
                        activePowerup = new Powerup(alien.x + ALIEN_WIDTH / 2, alien.y + ALIEN_HEIGHT);
                    }
                    
                    return;
                }
            }
        }
    }
    
    /**
     * Check if an alien bullet hit the player
     */
    private void checkAlienBulletCollisions(Bullet bullet) {
        if (checkCollision(bullet.x, bullet.y, 5, 10, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT)) {
            alienBullets.remove(bullet);
            lives--;
        }
    }
    
    /**
     * Check if player collected a powerup
     */
    private void checkPowerupCollection() {
        if (activePowerup == null) return;
        
        if (checkCollision(activePowerup.x, activePowerup.y, 20, 20,
                          playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT)) {
            activePowerup = null;
            tripleShotActive = true;
            tripleShotEndTime = System.currentTimeMillis() + 7000; // 7 seconds
        }
    }
    
    /**
     * Axis-aligned bounding box collision detection
     */
    private boolean checkCollision(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }
    
    /**
     * Move player left (if not at left boundary)
     */
    public void movePlayerLeft() {
        if (playerX > 0) {
            playerX -= 5;
        }
    }
    
    /**
     * Move player right (if not at right boundary)
     */
    public void movePlayerRight() {
        if (playerX < GAME_WIDTH - PLAYER_WIDTH) {
            playerX += 5;
        }
    }
    
    /**
     * Fire player bullets (1 or 3 depending on powerup status)
     */
    public void firePlayerBullet() {
        if (tripleShotActive) {
            // Fire 3 bullets in a spread pattern
            if (playerBullets.size() < 3) {  // Limit to 3 bullets max
                playerBullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 10, playerY, 0, -7)); // Left
                playerBullets.add(new Bullet(playerX + PLAYER_WIDTH / 2, playerY, 0, -7));      // Center
                playerBullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 + 10, playerY, 0, -7)); // Right
            }
        } else {
            // Fire single bullet
            if (playerBullets.isEmpty()) {
                playerBullets.add(new Bullet(playerX + PLAYER_WIDTH / 2, playerY, 0, -7));
            }
        }
    }
    
    /**
     * Reset the game to initial state
     */
    public void reset() {
        playerX = GAME_WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = GAME_HEIGHT - 60;
        score = 0;
        lives = 3;
        alienFireCounter = 0;
        initializeAliens();
        alienBullets.clear();
        playerBullets.clear();
        activePowerup = null;
        tripleShotActive = false;
        tripleShotEndTime = 0;
        currentLevel = 1;
        alienSpeed = 1;
        alienAliveCount = ALIEN_ROWS * ALIEN_COLS;
    }
    
    // Getters for GameView to access game state
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getCurrentLevel() { return currentLevel; }
    public int getAlienAliveCount() { return alienAliveCount; }
    public Alien[][] getAliens() { return aliens; }
    public List<Bullet> getPlayerBullets() { return playerBullets; }
    public List<Bullet> getAlienBullets() { return alienBullets; }
    public Powerup getActivePowerup() { return activePowerup; }
    public boolean isTripleShotActive() { return tripleShotActive; }
}
