import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;

/**
 * GameView.java
 * 
 * Handles rendering and display of the Space Invaders game.
 * Extends JPanel and is responsible for:
 * - Drawing game entities on screen
 * - Rendering game state (score, lives, game over screen)
 * - Handling visual updates
 * - Receiving updates from GameModel
 */
public class GameView extends JPanel implements MouseListener {
    
    private GameModel model;
    private GameController controller;
    private static final int PLAYER_WIDTH = 40;
    private static final int PLAYER_HEIGHT = 40;
    private static final int ALIEN_WIDTH = 30;
    private static final int ALIEN_HEIGHT = 30;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 50;
    private Random random;
    private BufferedImage galaxyBackground;
    
    /**
     * Constructor - Initialize view with reference to game model and controller
     */
    public GameView(GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        setFocusable(true);
        setBackground(Color.BLACK);
        addMouseListener(this);
        random = new Random(42); // Fixed seed for consistent background
        createGalaxyBackground();
    }
    
    /**
     * Override paintComponent to render the game
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw galaxy background
        drawGalaxyBackground(g);
        
        // Draw player
        drawPlayer(g);
        
        // Draw aliens
        drawAliens(g);
        
        // Draw player bullets
        drawPlayerBullets(g);
        
        // Draw alien bullets
        drawAlienBullets(g);
        
        // Draw powerup if active
        drawPowerup(g);
        
        // Draw HUD (score and lives)
        drawHUD(g);
        
        // Draw game over message if applicable
        if (isGameOver()) {
            drawGameOver(g);
        }
    }
    
    /**
     * Create the static galaxy background image
     */
    private void createGalaxyBackground() {
        galaxyBackground = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = galaxyBackground.createGraphics();
        
        int width = galaxyBackground.getWidth();
        int height = galaxyBackground.getHeight();
        
        // Create gradient from dark purple to black
        GradientPaint gradient1 = new GradientPaint(0, 0, new Color(25, 0, 50), // Dark purple
                                                   width, height, Color.BLACK);
        g2d.setPaint(gradient1);
        g2d.fillRect(0, 0, width, height);
        
        // Add magenta and dark blue swirls
        g2d.setColor(new Color(139, 0, 139, 80)); // Dark magenta with transparency
        for (int i = 0; i < 20; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = random.nextInt(100) + 50;
            g2d.fillOval(x - size/2, y - size/2, size, size);
        }
        
        g2d.setColor(new Color(0, 0, 139, 60)); // Dark blue with transparency
        for (int i = 0; i < 15; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = random.nextInt(80) + 40;
            g2d.fillOval(x - size/2, y - size/2, size, size);
        }
        
        // Add some stars
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g2d.fillRect(x, y, 1, 1);
        }
        
        // Add brighter stars
        g2d.setColor(new Color(255, 255, 255, 200));
        for (int i = 0; i < 20; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g2d.fillRect(x, y, 2, 2);
        }
        
        g2d.dispose();
    }
    
    /**
     * Draw galaxy background with magenta, dark blue, dark purple, and black
     */
    private void drawGalaxyBackground(Graphics g) {
        if (galaxyBackground != null) {
            g.drawImage(galaxyBackground, 0, 0, null);
        }
    }
    
    /**
     * Draw the player ship
     */
    private void drawPlayer(Graphics g) {
        int x = model.getPlayerX();
        int y = model.getPlayerY();
        
        // Draw player as a simple light pink rectangle
        g.setColor(Color.PINK);
        g.fillRect(x, y, PLAYER_WIDTH, PLAYER_HEIGHT);
        
        // Draw a triangle shape for the nose of the ship
        g.fillPolygon(
            new int[]{x + PLAYER_WIDTH / 2, x + 5, x + PLAYER_WIDTH - 5},
            new int[]{y - 10, y, y},
            3
        );
    }
    
    /**
     * Draw all alive aliens in the formation
     */
    private void drawAliens(Graphics g) {
        GameModel.Alien[][] aliens = model.getAliens();
        
        for (int row = 0; row < aliens.length; row++) {
            for (int col = 0; col < aliens[row].length; col++) {
                GameModel.Alien alien = aliens[row][col];
                if (alien.alive) {
                    drawCatAlien(g, alien.x, alien.y);
                }
            }
        }
    }
    
    /**
     * Draw a single cat alien at the specified position
     */
    private void drawCatAlien(Graphics g, int x, int y) {
        // Draw cat body/head (rounded rectangle)
        g.setColor(new Color(186, 85, 211)); // Light purple color
        g.fillRoundRect(x, y + 5, ALIEN_WIDTH, ALIEN_HEIGHT - 5, 8, 8);
        
        // Draw cat ears (triangles)
        g.fillPolygon(
            new int[]{x + 5, x + 10, x + 15}, // left ear
            new int[]{y + 5, y - 2, y + 5},
            3
        );
        g.fillPolygon(
            new int[]{x + 15, x + 20, x + 25}, // right ear
            new int[]{y + 5, y - 2, y + 5},
            3
        );
        
        // Draw "0.0" eyes
        g.setColor(Color.BLACK);
        Font eyeFont = new Font("Arial", Font.BOLD, 8);
        g.setFont(eyeFont);
        g.drawString("0", x + 6, y + 15);
        g.drawString("0", x + 18, y + 15);
        
        // Draw period eyes
        g.fillRect(x + 10, y + 18, 1, 1);
        g.fillRect(x + 22, y + 18, 1, 1);
        
        // Draw whiskers
        g.setColor(Color.BLACK);
        // Left whiskers
        g.drawLine(x + 2, y + 12, x - 2, y + 10);
        g.drawLine(x + 2, y + 16, x - 2, y + 18);
        // Right whiskers
        g.drawLine(x + 28, y + 12, x + 32, y + 10);
        g.drawLine(x + 28, y + 16, x + 32, y + 18);
    }
    
    /**
     * Draw all player bullets
     */
    private void drawPlayerBullets(Graphics g) {
        g.setColor(Color.YELLOW);
        List<GameModel.Bullet> bullets = model.getPlayerBullets();
        for (GameModel.Bullet bullet : bullets) {
            g.fillRect(bullet.x - 2, bullet.y, 4, 10);
        }
    }
    
    /**
     * Draw all alien bullets
     */
    private void drawAlienBullets(Graphics g) {
        g.setColor(Color.RED);
        List<GameModel.Bullet> bullets = model.getAlienBullets();
        for (GameModel.Bullet bullet : bullets) {
            g.fillRect(bullet.x - 2, bullet.y, 4, 10);
        }
    }
    
    /**
     * Draw the active powerup if present
     */
    private void drawPowerup(Graphics g) {
        GameModel.Powerup powerup = model.getActivePowerup();
        if (powerup != null) {
            g.setColor(Color.CYAN); // Light blue color
            g.fillOval(powerup.x - 10, powerup.y - 10, 20, 20);
            
            // Add a glowing effect
            g.setColor(new Color(173, 216, 230, 128)); // Lighter blue with transparency
            g.fillOval(powerup.x - 15, powerup.y - 15, 30, 30);
        }
    }
    
    /**
     * Draw score and lives in the top-left corner
     */
    private void drawHUD(Graphics g) {
        g.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.BOLD, 16);
        g.setFont(font);
        
        g.drawString("Score: " + model.getScore(), 10, 20);
        g.drawString("Lives: " + model.getLives(), 10, 40);
        g.drawString("Level: " + model.getCurrentLevel(), 10, 60);
        g.drawString("Aliens Left: " + model.getAlienAliveCount(), 10, 80);
    }
    
    /**
     * Draw centered game-over message and restart button
     */
    private void drawGameOver(Graphics g) {
        // Draw light pink overlay with 50% opacity
        g.setColor(new Color(255, 192, 203, 127)); // Light pink with 50% opacity
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw message
        g.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.BOLD, 48);
        g.setFont(font);
        
        String message = "GAME OVER";
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (getWidth() - metrics.stringWidth(message)) / 2;
        int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        
        g.drawString(message, x, y);
        
        // Draw score
        Font scoreFont = new Font("Arial", Font.BOLD, 24);
        g.setFont(scoreFont);
        String scoreMsg = "Final Score: " + model.getScore();
        FontMetrics scoreMetrics = g.getFontMetrics(scoreFont);
        int scoreX = (getWidth() - scoreMetrics.stringWidth(scoreMsg)) / 2;
        g.drawString(scoreMsg, scoreX, y + 60);
        
        // Draw restart button
        drawRestartButton(g);
    }
    
    /**
     * Draw restart button and return its bounding rectangle
     */
    private Rectangle drawRestartButton(Graphics g) {
        int buttonX = (getWidth() - BUTTON_WIDTH) / 2;
        int buttonY = getHeight() / 2 + 100;
        
        // Draw button background (same pink as player)
        g.setColor(Color.PINK);
        g.fillRect(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        
        // Draw button border
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new java.awt.BasicStroke(2));
        g2d.drawRect(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        
        // Draw button text (white)
        g.setColor(Color.WHITE);
        Font buttonFont = new Font("Arial", Font.BOLD, 20);
        g.setFont(buttonFont);
        String buttonText = "RESTART";
        FontMetrics buttonMetrics = g.getFontMetrics(buttonFont);
        int textX = buttonX + (BUTTON_WIDTH - buttonMetrics.stringWidth(buttonText)) / 2;
        int textY = buttonY + ((BUTTON_HEIGHT - buttonMetrics.getHeight()) / 2) + buttonMetrics.getAscent();
        g.drawString(buttonText, textX, textY);
        
        return new Rectangle(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    /**
     * Get the restart button bounds for click detection
     */
    private Rectangle getRestartButtonBounds() {
        int buttonX = (getWidth() - BUTTON_WIDTH) / 2;
        int buttonY = getHeight() / 2 + 100;
        return new Rectangle(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    /**
     * Check if the game is over (lives <= 0 or all aliens defeated)
     */
    private boolean isGameOver() {
        if (model.getLives() <= 0) {
            return true;
        }
        
        // Check if all aliens are dead
        GameModel.Alien[][] aliens = model.getAliens();
        for (int row = 0; row < aliens.length; row++) {
            for (int col = 0; col < aliens[row].length; col++) {
                if (aliens[row][col].alive) {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (isGameOver()) {
            Rectangle buttonBounds = getRestartButtonBounds();
            if (buttonBounds.contains(e.getPoint())) {
                controller.restart();
            }
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
}
