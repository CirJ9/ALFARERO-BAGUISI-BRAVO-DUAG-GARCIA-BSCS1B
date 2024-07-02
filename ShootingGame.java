/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package shootinggame;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class ShootingGame extends Frame implements Runnable {
    private int playerX = 400, playerY = 500;
    private boolean running = true;
    private ArrayList<Rectangle> bullets = new ArrayList<>();
    private ArrayList<Rectangle> targets = new ArrayList<>();
    private int score = 0;
    private int playerHealth = 3;
    private int level = 1;
    private Random random = new Random();

    public ShootingGame() {
        setSize(800, 600);
        setVisible(true);
        setTitle("Mobai Ligend: Beng Beng");
        setBackground(Color.GRAY);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                running = false;
                System.exit(0);
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_LEFT && playerX > 0) {
                    playerX -= 10;
                } else if (key == KeyEvent.VK_RIGHT && playerX < getWidth() - 50) {
                    playerX += 10;
                } else if (key == KeyEvent.VK_SPACE) {
                    bullets.add(new Rectangle(playerX + 20, playerY, 5, 10));
                }
            }
        });

        spawnTargets();

        new Thread(this).start();
    }

    private void spawnTargets() {
        targets.clear();
        for (int i = 0; i < level + 4; i++) {
            targets.add(new Rectangle(i * 150 % 700 + 50, 50 + random.nextInt(100), 50, 50));
        }
    }

    public void run() {
        while (running) {
            updateGame();
            repaint();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGame() {
        // Move bullets
        for (int i = 0; i < bullets.size(); i++) {
            Rectangle bullet = bullets.get(i);
            bullet.y -= 5;
            if (bullet.y < 0) {
                bullets.remove(i);
                i--;
            }
        }


        for (Rectangle target : targets) {
            target.x += random.nextBoolean() ? 1 : -1;
            if (target.x < 0 || target.x > getWidth() - 50) {
                target.x = Math.max(0, Math.min(getWidth() - 50, target.x));
            }
        }


        for (int i = 0; i < bullets.size(); i++) {
            for (int j = 0; j < targets.size(); j++) {
                if (bullets.get(i).intersects(targets.get(j))) {
                    bullets.remove(i);
                    targets.remove(j);
                    score += 10;
                    i--;
                    break;
                }
            }
        }


        if (targets.isEmpty()) {
            level++;
            spawnTargets();
        }


        for (int i = 0; i < targets.size(); i++) {
            if (targets.get(i).y >= playerY) {
                targets.remove(i);
                playerHealth--;
                i--;
            }
        }

        if (playerHealth <= 0) {
            running = false;
        }
    }

    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 50);
        g.drawString("Health: " + playerHealth, 10, 70);
        g.drawString("Level: " + level, 10, 90);

        g.setColor(Color.BLUE);
        g.fillRect(playerX, playerY, 50, 10);

        g.setColor(Color.WHITE);
        for (Rectangle bullet : bullets) {
            g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
        }

        g.setColor(Color.RED);
        for (Rectangle target : targets) {
            g.fillRect(target.x, target.y, target.width, target.height);
        }

        if (playerHealth <= 0) {
            g.drawString("Game Over!", 350, 300);
        } else if (targets.isEmpty() && level > 1) {
            g.drawString("You Win!", 350, 300);
        }
    }

    public static void main(String[] args) {
        new ShootingGame();
    }
}
