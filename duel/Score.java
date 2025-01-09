/*
* Swapnil Kabir and Syed Bazif Shah
* Date: January 9, 2025
* Description: Score class to track and manage player scores in Top-Down Duel
*/

import java.awt.*;

public class Score {
    private int leftPlayerScore;
    private int rightPlayerScore;

    public Score() {
        leftPlayerScore = 0;
        rightPlayerScore = 0;
    }

    // Increment score for left player
    public void scoreLeftPlayer() {
        leftPlayerScore++;
    }

    // Increment score for right player
    public void scoreRightPlayer() {
        rightPlayerScore++;
    }

    // Get current score for left player
    public int getLeftPlayerScore() {
        return leftPlayerScore;
    }

    // Get current score for right player
    public int getRightPlayerScore() {
        return rightPlayerScore;
    }

    // Draw scores on the screen
    public void draw(Graphics g, int screenWidth, int screenHeight) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 40));

        // Format score as "LeftScore : RightScore"
        String scoreText = String.format("%d : %d", leftPlayerScore, rightPlayerScore);

        // Measure text width to center it
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(scoreText);

        // Draw score in top middle of screen
        g.drawString(scoreText, 
                     (screenWidth - textWidth) / 2, 
                     575);
    }

    // Reset scores for a new game
    public void reset() {
        leftPlayerScore = 0;
        rightPlayerScore = 0;
    }
}