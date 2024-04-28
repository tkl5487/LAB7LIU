/** Project: Solo Lab 7 Assignment
* Purpose Details: To create a space game with different levels, health, images, sounds.
* Course: IST 242
* Author: Taylor Liu
* Date Developed: April 24, 2024
* Last Date Changed:
* Rev:

*/

// Imports all the important packages that are needed for the code
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import javax.swing.Timer;
import java.util.TimerTask;
import java.applet.*;
import java.net.URL;


// Define the Spacegame Class
public class SpaceGame extends JFrame implements KeyListener {
    // Game dimension, size, speed, and other parameters
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 100;
    private static final int PLAYER_HEIGHT = 100;
    private static final int OBSTACLE_WIDTH = 100;
    private static final int OBSTACLE_HEIGHT = 100;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 5;
    private static final int OBSTACLE_SPEED = 3;
    private static final int PROJECTILE_SPEED = 10;
    private static final int MAX_LIVES =3;
    private static final int INVULNERABILITY_DURATION = 2000;
    private static final int POWER_UP_WIDTH = 100;
    private static final int POWER_UP_HEIGHT = 100;

    private static final int CHALLENGE_SCORE = 100;

    // Tracks the game score, lives, who fast objects will move, and state of game
    private int score = 0;
    private int lives = MAX_LIVES;
    private int shieldDuration = 0;
    private boolean isShieldActive = false;
    private boolean isShipInvulnerable = false;
    private double obstacleSpeedMultiplier = 1.0;
    private double powerUpSpeedMultiplier = 1.0;
    private double obstacleFrequenecyMultiplier =1.0;
    private double powerUpFrequencyMultiplier =1.0;
    private boolean isChallengeActive = false;
    private List<Point> powerUps = new ArrayList<>();
    private BufferedImage powerUpImage;






    // Label shows the game
    private JPanel gamePanel;

    // Label will show the score
    private JLabel scoreLabel;

    // Label will show the timer
    private Timer timer;

    // Label will show the health of the ship
    private JLabel healthLabel;

    // Label shows the shield length
    private JLabel shieldLabel;
    // Label will show countdown timer
    private JLabel countdownTimerLabel;

    // Label will show for the challenge
    private JLabel challengeLabel;
    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private java.util.List<Point> obstacles;
    private Random random;
    private BufferedImage playerImage;
    private BufferedImage obstacleImage;
    private AudioClip fireSound;
    private AudioClip collisionSound;
    private int countdown;
    private int remainingTime = 60;

    // Constructor for the space game
    public SpaceGame() {
        // properties of the frame
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        // Loads the images for the player, obstacle, and power ups
         try{
             playerImage = ImageIO.read(new File("BMO-1.png_1.png"));
             obstacleImage = ImageIO.read(new File("objectpurp-1.png.png"));
             powerUpImage = ImageIO.read(new File("bonus-1.png.png"));

         } catch (IOException e){
             e.printStackTrace();
         }
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

         // This will load the audio clips for firing the projectile and the collision sounds
         fireSound = Applet.newAudioClip(getClass().getResource("fire-88783.wav"));
         collisionSound = Applet.newAudioClip(getClass().getResource("fast-collision-reverb-14611.wav"));
        // Label will show the score of the player
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.blue);
        scoreLabel.setBounds(10, 10, 100, 20);
        gamePanel.add(scoreLabel);
        // Label will show the health of the player by showing how many lives they have
        healthLabel = new JLabel("Lives: " + MAX_LIVES);
        healthLabel.setBounds(10,30,100,20);
        gamePanel.add(healthLabel);
        // Label will show how long the shield will last
        shieldLabel = new JLabel("Shield: " +shieldDuration);
        shieldLabel.setBounds(10,50,100,20);
        gamePanel.add(shieldLabel);
        // Label will show how long you have in the game to play before its over
        countdownTimerLabel = new JLabel("Time: " + remainingTime);
        countdownTimerLabel.setBounds(10,70,100,20);
        gamePanel.add(countdownTimerLabel);
        // Label will show when the challenge is activated once the player hits a score of 100
        challengeLabel = new JLabel("Challenge activated since you hit 100 score!");
        challengeLabel.setForeground(Color.red);
        challengeLabel.setBounds(10,90,200,20);
        gamePanel.add(challengeLabel);
        challengeLabel.setVisible(false);
        // Adds the game panel to frame and will allow us to get the focus and the key listeners events
        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);
        // This will initialize the player, projectiles and obstacles in the game
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;
        obstacles = new ArrayList<>();
        random = new Random();

        // This will start the countdown of the game
        countdown = 60;

        // There will be a delay of 1000 milliseconds which is 1 second
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--;
                // If the countdown = 0 the game will be over and the timer wil lstop
                if (countdown == 0){
                    gameOver();
                    timer.stop();
                }
            }
        });
        timer.setInitialDelay(0);
        timer.start();
        // This will start the countdown for how much time is remaining in the game
        Timer countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remainingTime--;
                if (remainingTime <= 0) {
                    // End the game when the timer reaches zero
                    gameOver();
                    // Stops the timer
                    ((Timer) e.getSource()).stop();
                } else {
                    // Updates the label of the remaining time
                    countdownTimerLabel.setText("Time: " + remainingTime);
                }
            }
        });
        countdownTimer.setInitialDelay(0);
        countdownTimer.start();

        // Creates a timer for updating the game with 20 milliseconds
        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If the game isn't over then the game will update its state
                if (!isGameOver) {
                    update();
                    gamePanel.repaint();
                }
            }
        });
        timer.start();
    }

    // Method to draw the graphics in the game
    private void draw(Graphics g) {
        // Background color
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.drawString("Time: " + remainingTime, 10, 70);

        for (int i = 0; i <100; i++){
           int x = random.nextInt(WIDTH);
           int y = random.nextInt(HEIGHT);

           g.setColor(Color.white);
           g.fillRect(x, y, 2,2);

       }
        if (isShieldActive) {
            // Draws a  translucent shield for the ship
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(playerX - 5, playerY - 5, PLAYER_WIDTH + 10, PLAYER_HEIGHT + 10);
        }
        // Draws the power-ups
       g.drawImage(playerImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT,null);
        for (Point powerUp :powerUps){
            g.drawImage(powerUpImage,powerUp.x,powerUp.y,POWER_UP_WIDTH,POWER_UP_HEIGHT, null);
        }
        // Draws the projectile
        if (isProjectileVisible) {
            g.setColor(Color.PINK);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        // Draws the obstacle
        for (Point obstacle :obstacles){
            g.drawImage(obstacleImage, obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT, null);
        }
        // Draws the game over message
        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }

    }

    private void update() {
        if (!isGameOver) {
            // Updates the obstacles
            for (int i = 0; i < obstacles.size(); i++) {
                obstacles.get(i).y += OBSTACLE_SPEED;
                if (obstacles.get(i).y > HEIGHT) {
                    obstacles.remove(i);
                    i--;
                }
            }
            // This will add the new obstacles in randomly
            if (Math.random() < (0.02 * obstacleFrequenecyMultiplier)) {
                int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                obstacles.add(new Point(obstacleX, 0));
            }

            // Generate the new obstacles in game
            if (Math.random() < 0.02) {
                int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                obstacles.add(new Point(obstacleX, 0));
            }
            // This will also add random new power-ups
            if (Math.random() < (0.01 * powerUpFrequencyMultiplier)) {
                int powerUpX = (int) (Math.random() * (WIDTH - POWER_UP_WIDTH));
                powerUps.add(new Point(powerUpX, 0));
            }

            // Moves the Power-ups
            for (int i =0; i<powerUps.size(); i++){
                powerUps.get(i).y += OBSTACLE_SPEED;
                if (powerUps.get(i).y > HEIGHT){
                    powerUps.remove(i);
                    i--;
                }
            }

            // Generates new power-ups in game
            if (Math.random() < 0.01) {
                int powerUpX = (int) (Math.random() * (WIDTH - POWER_UP_WIDTH));
                powerUps.add(new Point(powerUpX, 0));
            }

            // Move the projectile
            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }

            // Check collision with player
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            for (Point obstacle : obstacles) {
                Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(obstacleRect)) {
                    if (!isShipInvulnerable) {
                        // Plays the collision sound
                        collisionSound.play();
                        // Reduces the lives only if the ship is not currently invulnerable
                        lives--;
                        if (lives <= 0) {
                            gameOver();
                        }
                        healthLabel.setText("Lives: " + lives);

                        // Make the ship invulnerable and start a timer to reset it
                        isShipInvulnerable = true;
                        Timer invulnerabilityTimer = new Timer(INVULNERABILITY_DURATION, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                isShipInvulnerable = false;
                            }
                        });
                        invulnerabilityTimer.setRepeats(false);
                        invulnerabilityTimer.start();
                    }
                    break;
                }
            }

            // Check collision with power-ups
            for (Point powerUp : powerUps){
                Rectangle powerUpRect = new Rectangle(powerUp.x,powerUp.y, POWER_UP_WIDTH,POWER_UP_HEIGHT);
                if (playerRect.intersects(powerUpRect)){
                    // Randomly choose a power-up type
                    int powerUpType = random.nextInt(2);

                    switch (powerUpType) {
                        //Can give an Extra life
                        case 0:
                            if (lives < MAX_LIVES) {
                                lives++;
                                healthLabel.setText("Lives: " + lives);
                            }
                            break;

                        case 1:
                            // Can increase score by a bonus amount
                            score += 50;
                            scoreLabel.setText("Score: " + score);
                            break;

                    }

                    // Remove the power-up
                    powerUps.remove(powerUp);
                    break;
                }
            }


            // Check collision with obstacle
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            for (int i = 0; i < obstacles.size(); i++) {
                Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (projectileRect.intersects(obstacleRect)) {
                    obstacles.remove(i);
                    score += 10;
                    isProjectileVisible = false;
                    break;
                }
            }
            // Challenge for when score hits 100 the players will activate
            if (score >= CHALLENGE_SCORE && !isChallengeActive){
                isChallengeActive = true;
                challengeLabel.setVisible(true);
                // Will make the obstacles fall down faster, so it is harder for players
                obstacleFrequenecyMultiplier = 2.0;
                // Makes them have an extra two lives
                lives +=2;
                healthLabel.setText("Lives: "+ lives);
            }

            // Dispalys the score label
            scoreLabel.setText("Score: " + score);
            // Checks if the shield is active 
            if (isShieldActive){
                // The duration of the shield will be decreased by 1
                shieldDuration--;
                // This will update the shield duration of how much time is remaining of the shield
                shieldLabel.setText("Shield: " + shieldDuration);
                // If the shield is at 0 then the shield will be deactivated
                if(shieldDuration == 0){
                    isShieldActive = false;
                }
            }
        }
    }

    private void gameOver(){
        isGameOver = true;
        timer.stop();
    }
    // This is the code that will handle all the key presses which are for the movement, firing, and activating the shield
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            // Plays the fire sound when firing the projectile
            fireSound.play();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500); // Limit firing rate
                        isFiring = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } else if (keyCode == KeyEvent.VK_S && shieldDuration == 0){
            isShieldActive = true;
            shieldDuration = 100;
            shieldLabel.setText("Shield: " + shieldDuration);
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
    // This will start the game and show the window of the game
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}
