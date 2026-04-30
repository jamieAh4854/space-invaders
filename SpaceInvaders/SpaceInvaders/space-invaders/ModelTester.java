/**
 * ModelTester.java
 * 
 * Manual unit tests for GameModel behavior.
 * Tests core game mechanics without external testing libraries.
 */
import java.util.List;

public class ModelTester {
    
    private static int passCount = 0;
    private static int failCount = 0;
    
    public static void main(String[] args) {
        System.out.println("=== Space Invaders Model Tester ===\n");
        
        testPlayerBoundaryLeft();
        testPlayerBoundaryRight();
        testSingleBulletInFlight();
        testBulletRemovalAtTop();
        testAlienDestructionIncreasesScore();
        testGameOverOnZeroLives();
        
        System.out.println("\n=== Test Results ===");
        System.out.println("PASSED: " + passCount);
        System.out.println("FAILED: " + failCount);
    }
    
    /**
     * Test: Player cannot move past the left edge (x=0)
     */
    private static void testPlayerBoundaryLeft() {
        GameModel model = new GameModel();
        
        // Move left many times
        for (int i = 0; i < 100; i++) {
            model.movePlayerLeft();
        }
        
        boolean passed = model.getPlayerX() >= 0;
        printResult("Player cannot move past left edge", passed);
    }
    
    /**
     * Test: Player cannot move past the right edge (x + width = 800)
     */
    private static void testPlayerBoundaryRight() {
        GameModel model = new GameModel();
        
        // Move right many times
        for (int i = 0; i < 100; i++) {
            model.movePlayerRight();
        }
        
        int maxX = 800 - 40;  // GAME_WIDTH - PLAYER_WIDTH
        boolean passed = model.getPlayerX() <= maxX;
        printResult("Player cannot move past right edge", passed);
    }
    
    /**
     * Test: Firing while a bullet is already in flight does nothing (single shot mode)
     */
    private static void testSingleBulletInFlight() {
        GameModel model = new GameModel();
        
        // Fire first bullet
        model.firePlayerBullet();
        List<GameModel.Bullet> bullets1 = model.getPlayerBullets();
        
        // Try to fire again
        model.firePlayerBullet();
        List<GameModel.Bullet> bullets2 = model.getPlayerBullets();
        
        // Should still have only 1 bullet
        boolean passed = bullets1.size() == 1 && bullets2.size() == 1;
        printResult("Cannot fire while bullet in flight", passed);
    }
    
    /**
     * Test: A bullet that reaches the top (y < 0) is removed
     */
    private static void testBulletRemovalAtTop() {
        GameModel model = new GameModel();
        
        // Fire a bullet
        model.firePlayerBullet();
        
        // Advance the bullet to the top by calling update many times
        for (int i = 0; i < 100; i++) {
            model.update();
        }
        
        // Bullet list should be empty after reaching top
        boolean passed = model.getPlayerBullets().isEmpty();
        printResult("Bullet removed when reaching top", passed);
    }
    
    /**
     * Test: Destroying an alien increases the score by 10
     */
    private static void testAlienDestructionIncreasesScore() {
        GameModel model = new GameModel();
        int initialScore = model.getScore();
        
        // Get the first alive alien
        GameModel.Alien[][] aliens = model.getAliens();
        GameModel.Alien targetAlien = aliens[0][0];
        
        // Fire a bullet and position it to hit the alien
        model.firePlayerBullet();
        List<GameModel.Bullet> bullets = model.getPlayerBullets();
        GameModel.Bullet bullet = bullets.get(0);
        bullet.x = targetAlien.x + 15;
        bullet.y = targetAlien.y + 15;
        
        // Call update to trigger collision detection
        model.update();
        
        // Check if alien is destroyed and score increased
        boolean alienDestroyed = !targetAlien.alive;
        boolean scoreIncreased = model.getScore() == initialScore + 10;
        boolean passed = alienDestroyed && scoreIncreased;
        
        printResult("Destroying alien increases score by 10", passed);
    }
    
    /**
     * Test: Losing all lives triggers game-over state
     */
    private static void testGameOverOnZeroLives() {
        GameModel model = new GameModel();
        
        // Verify we start with 3 lives
        if (model.getLives() != 3) {
            printResult("Game over when lives reach 0", false);
            return;
        }
        
        // Simulate getting hit by alien bullets
        for (int i = 0; i < 100; i++) {
            // Fire random alien bullets
            GameModel.Bullet bullet = new GameModel.Bullet(
                model.getPlayerX() + 20, 
                model.getPlayerY() - 50,
                0, 
                5
            );
            model.getAlienBullets().add(bullet);
            
            // Update to process collision
            model.update();
            
            if (model.getLives() <= 0) {
                break;
            }
        }
        
        boolean passed = model.getLives() <= 0;
        printResult("Game over when lives reach 0", passed);
    }
    
    /**
     * Helper method to print test results
     */
    private static void printResult(String testName, boolean passed) {
        String result = passed ? "PASS" : "FAIL";
        System.out.println("[" + result + "] " + testName);
        
        if (passed) {
            passCount++;
        } else {
            failCount++;
        }
    }
}
