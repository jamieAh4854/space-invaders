import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * GameController.java
 * 
 * Main controller class that coordinates the MVC architecture.
 * Responsible for:
 * - Creating and configuring the main JFrame window
 * - Instantiating GameModel and GameView
 * - Setting up the game loop
 * - Handling input events and delegating to appropriate components
 */
public class GameController implements KeyListener {
    
    final private JFrame frame;
    final private GameModel model;
    final private GameView view;
    private Timer gameLoop;
    
    // Key state tracking for smooth movement
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    
    /**
     * Constructor - Initialize the game and set up the window
     */
    public GameController() {
        // Create the frame
        frame = new JFrame("Space Invaders");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        // Create model and view (pass this controller to view)
        model = new GameModel();
        view = new GameView(model, this);
        
        // Add view to frame
        frame.add(view);
        
        // Set frame properties
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Add keyboard listener
        view.addKeyListener(this);
        view.requestFocusInWindow();
        
        // Start game loop (60 FPS = ~16.67ms per frame)
        gameLoop = new Timer(17, e -> gameLoopTick());
        gameLoop.start();
    }
    
    /**
     * Main game loop tick - called by timer each frame
     */
    private void gameLoopTick() {
        // Handle continuous key input for smooth movement
        if (leftPressed) {
            model.movePlayerLeft();
        }
        if (rightPressed) {
            model.movePlayerRight();
        }
        
        // Update model (bullets, aliens, collisions)
        model.update();
        
        // Redraw view
        view.repaint();
        
        // Check for game over and stop loop if needed
        if (isGameOver()) {
            gameLoop.stop();
        }
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
    
    /**
     * Restart the game - reset model and restart game loop
     */
    public void restart() {
        model.reset();
        gameLoop.stop();
        gameLoop = new Timer(17, e -> gameLoopTick());
        gameLoop.start();
        view.requestFocusInWindow();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = true;
        } else if (key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        } else if (key == KeyEvent.VK_SPACE) {
            model.firePlayerBullet();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = false;
        } else if (key == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
    
    /**
     * Main method - Entry point for the application
     */
    public static void main(String[] args) {
        // Create and run the game
        new GameController();
    }
}
