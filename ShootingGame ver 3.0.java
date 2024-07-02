package shootinggame;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShootingGame extends Frame implements Runnable {
    private enum GameState {
        PLAYING,
        GAME_OVER,
        LEVEL_COMPLETE,
        CHARACTER_SELECTION,
        LEVEL_START
    }

    private GameState gameState = GameState.CHARACTER_SELECTION;

    private Player player;
    private List<Bullet> bullets;
    private List<Target> targets;
    private int score;
    private int level;
    private Random random;

    private Image[] playerImages;
    private Image bulletImage;
    private Image targetImage;
    private Image explosionImage;
    private int selectedPlayerIndex = 0;
    private long levelCompleteTime;

    public ShootingGame() {
        loadImages();
        initializeGame();
        setupUI();
        new Thread(this).start();
    }

    private void loadImages() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        playerImages = new Image[3];
        playerImages[0] = toolkit.getImage(getClass().getResource("/shootinggame/player1.png")).getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        playerImages[1] = toolkit.getImage(getClass().getResource("/shootinggame/player2.png")).getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        playerImages[2] = toolkit.getImage(getClass().getResource("/shootinggame/player3.png")).getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        bulletImage = toolkit.getImage(getClass().getResource("/shootinggame/bullet.png")).getScaledInstance(10, 20, Image.SCALE_SMOOTH);
        targetImage = toolkit.getImage(getClass().getResource("/shootinggame/enemy.png")).getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        explosionImage = toolkit.getImage(getClass().getResource("/shootinggame/explosion.png")).getScaledInstance(50, 50, Image.SCALE_SMOOTH);
    }

    private void initializeGame() {
        setSize(800, 600);
        setTitle("Shooting Game");
        setBackground(Color.GRAY);
        bullets = new ArrayList<>();
        targets = new ArrayList<>();
        score = 0;
        level = 1;
        random = new Random();
    }

    private void setupUI() {
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (gameState == GameState.CHARACTER_SELECTION) {
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_LEFT) {
                        selectedPlayerIndex = (selectedPlayerIndex + playerImages.length - 1) % playerImages.length;
                    } else if (key == KeyEvent.VK_RIGHT) {
                        selectedPlayerIndex = (selectedPlayerIndex + 1) % playerImages.length;
                    } else if (key == KeyEvent.VK_ENTER) {
                        player = new Player(400, 500, playerImages[selectedPlayerIndex]);
                        gameState = GameState.LEVEL_START;
                    }
                    repaint();
                } else if (gameState == GameState.PLAYING) {
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_LEFT) {
                        player.moveLeft();
                    } else if (key == KeyEvent.VK_RIGHT) {
                        player.moveRight(getWidth());
                    } else if (key == KeyEvent.VK_SPACE) {
                        bullets.add(new Bullet(player.getX() + 20, player.getY(), bulletImage));
                    }
                } else if (gameState == GameState.LEVEL_COMPLETE || gameState == GameState.GAME_OVER) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (gameState == GameState.LEVEL_COMPLETE) {
                            level++;
                            gameState = GameState.LEVEL_START;
                        } else if (gameState == GameState.GAME_OVER) {
                            level = 1;
                            score = 0;
                            gameState = GameState.CHARACTER_SELECTION;
                        }
                    }
                }
            }
        });
    }

    private void spawnTargets() {
        targets.clear();
        int numTargets = level + 4;
        int targetWidth = 50;
        int targetHeight = 50;
        int startY = 50;
        int maxX = getWidth() - targetWidth;
        for (int i = 0; i < numTargets; i++) {
            int x = i * 150 % maxX + 50;
            int y = startY + random.nextInt(100);
            targets.add(new Target(x, y, targetWidth, targetHeight, targetImage, level));
        }
    }

    public void run() {
        while (true) {
            if (gameState == GameState.PLAYING) {
                updateGame();
                repaint();
            } else if (gameState == GameState.LEVEL_COMPLETE) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - levelCompleteTime > 3000) {
                    gameState = GameState.LEVEL_START;
                }
                repaint();
            } else if (gameState == GameState.LEVEL_START) {
                spawnTargets();
                gameState = GameState.PLAYING;
            }
            try {
                Thread.sleep(16); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGame() {
        for (Bullet bullet : new ArrayList<>(bullets)) {
            bullet.move();
            if (bullet.getY() < 0) {
                bullets.remove(bullet);
            }
        }

        for (Target target : targets) {
            target.move(getWidth());
        }

        
        for (Bullet bullet : new ArrayList<>(bullets)) {
            for (Target target : new ArrayList<>(targets)) {
                if (bullet.intersects(target)) {
                    bullets.remove(bullet);
                    targets.remove(target);
                    score += 10;
                }
            }
        }

        if (targets.isEmpty()) {
            levelCompleteTime = System.currentTimeMillis();
            gameState = GameState.LEVEL_COMPLETE;
        }

        for (Target target : new ArrayList<>(targets)) {
            if (target.getY() >= player.getY()) {
                targets.remove(target);
            }
        }

        if (targets.stream().anyMatch(target -> target.getY() >= player.getY())) {
            gameState = GameState.GAME_OVER;
        }
    }

    public void paint(Graphics g) {
        super.paint(g);
        if (gameState == GameState.CHARACTER_SELECTION) {
            g.setColor(Color.WHITE);
            g.drawString("Select your character with LEFT/RIGHT keys, press ENTER to start", 200, 100);
            g.drawImage(playerImages[selectedPlayerIndex], 375, 200, this);
        } else if (gameState == GameState.LEVEL_START) {
            g.setColor(Color.WHITE);
            g.drawString("Level " + level, 350, 300);
        } else {
            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, 10, 50);
            g.drawString("Level: " + level, 10, 70);

            player.draw(g);
            for (Bullet bullet : bullets) {
                bullet.draw(g);
            }
            for (Target target : targets) {
                target.draw(g);
            }

            if (gameState == GameState.GAME_OVER) {
                g.setColor(Color.RED);
                g.drawString("Game Over! Press ENTER to restart.", 350, 300);
            } else if (gameState == GameState.LEVEL_COMPLETE) {
                g.setColor(Color.GREEN);
                g.drawString("Level " + (level - 1) + " complete! Get ready for Level " + level, 300, 300);
            }
        }
    }

    public static void main(String[] args) {
        new ShootingGame();
    }
}

class Player {
    private int x, y;
    private int speed = 10;
    private Image image;

    public Player(int x, int y, Image image) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    public void moveLeft() {
        x -= speed;
        if (x < 0) {
            x = 0;
        }
    }

    public void moveRight(int screenWidth) {
        x += speed;
        if (x > screenWidth - image.getWidth(null)) {
            x = screenWidth - image.getWidth(null);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }
}

class Bullet {
    private int x, y;
    private int speed = 5;
    private int width;
    private int height;
    private Image image;

    public Bullet(int x, int y, Image image) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.width = image.getWidth(null);
        this.height = image.getHeight(null);
    }

    public void move() {
        y -= speed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }

    public boolean intersects(Target target) {
        Rectangle bulletRect = new Rectangle(x, y, width, height);
        Rectangle targetRect = new Rectangle(target.getX(), target.getY(), target.getWidth(), target.getHeight());
        return bulletRect.intersects(targetRect);
    }
}

class Target {
    private int x, y;
    private int width, height;
    private int speed;
    private Image image;

    public Target(int x, int y, int width, int height, Image image, int level) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
        this.speed = 1 + level; 
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void move(int screenWidth) {
        if (x <= 0 || x >= screenWidth - width) {
            speed = -speed;
        }
        x += speed;
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }
}