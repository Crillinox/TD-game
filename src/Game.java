import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

public class Game extends JPanel implements Runnable, MouseListener, KeyListener {
    private static final int UI_BAR_HEIGHT = 80;

    private Map map;
    private final java.util.List<Tower> towers = new ArrayList<>();
    private final java.util.List<Enemy> enemies = new ArrayList<>();
    private final java.util.List<Projectile> projectiles = new ArrayList<>();

    private BufferedImage gunTowerSprite, iceTowerSprite, bombTowerSprite;
    private BufferedImage normalEnemySprite, fastEnemySprite, tankEnemySprite;
    private BufferedImage tileGrass, tilePath, tileSpawn, tileGoal;
    private BufferedImage moneyIcon, healthIcon, upgradeButtonIcon, playButtonImage;

    private int playerMoney = 500;
    private int playerLives = 200;
    private int selectedTowerType = 0; // 0=none, 1=gun, 2=ice, 3=bomb
    private Tower focusedTower = null;

    private WaveManager waveManager;
    private boolean waveRunning = false;
    private int currentWaveNumber = 0;
    private int totalWaves = 40; // number of waves before freeplay
    private boolean repeatingWaves = true; // repeat first 40 waves
    private long seed = new Random().nextLong();

    private Thread gameThread;
    private boolean running = false;

    // Button positions
    private int playButtonX, playButtonY, playButtonWidth, playButtonHeight;
    private int upgradeButtonX, upgradeButtonY, upgradeButtonWidth, upgradeButtonHeight;

    public Game() {
        setFocusable(true);
        requestFocus();
        addMouseListener(this);
        addKeyListener(this);

        loadSprites();
        map = new Map();
        waveManager = new WaveManager(seed);

        // Load start wave button image
        try {
            playButtonImage = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Button/playButton.png"));
            playButtonWidth = 150;
            playButtonHeight = 50;
            playButtonX = Map.WIDTH * Map.TILE_SIZE - playButtonWidth - 20;
            playButtonY = Map.HEIGHT * Map.TILE_SIZE + 20;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load upgrade button icon
        try {
            upgradeButtonIcon = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Button/playButton.png"));
            upgradeButtonWidth = 120;
            upgradeButtonHeight = 40;
            upgradeButtonX = Map.WIDTH * Map.TILE_SIZE + 20;
            upgradeButtonY = 50;
        } catch (IOException e) {
            e.printStackTrace();
        }

        setPreferredSize(new Dimension(Map.WIDTH * Map.TILE_SIZE + 150, Map.HEIGHT * Map.TILE_SIZE + UI_BAR_HEIGHT + 70));
    }

    private void loadSprites() {
        try {
            gunTowerSprite = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Towers/GunTower.png"));
            iceTowerSprite = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Towers/IceTower.png"));
            bombTowerSprite = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Towers/bombShooter.png"));

            normalEnemySprite = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Enemies/normalEnemy.png"));
            fastEnemySprite = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Enemies/fastEnemy.png"));
            tankEnemySprite = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Enemies/tankEnemy.png"));

            tileGrass = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_grass.png"));
            tilePath = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_path.png"));
            tileSpawn = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_spawn.png"));
            tileGoal = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_goal.png"));

            moneyIcon = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Button/moneyIcon.png"));
            healthIcon = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Button/healthIcon.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            double dt = (now - lastTime) / 1e9;
            lastTime = now;

            updateGame(dt);
            repaint();

            try { Thread.sleep(16); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    private void updateGame(double dt) {
        if (playerLives <= 0) {
            running = false;
            return;
        }

        // Update enemies
        Iterator<Enemy> ei = enemies.iterator();
        while (ei.hasNext()) {
            Enemy e = ei.next();
            e.update(dt);
            if (e.isDead()) {
                playerMoney += e.getReward();
                ei.remove();
            } else if (e.reachedEnd()) {
                playerLives -= e.getDamage();
                ei.remove();
            }
        }

        // Update projectiles
        Iterator<Projectile> pi = projectiles.iterator();
        while (pi.hasNext()) {
            Projectile p = pi.next();
            p.update(dt, projectiles);
            if (p.hasExploded()) pi.remove();
        }

        // Update towers
        for (Tower t : towers) t.update(dt, enemies, projectiles);

        // Update spawn scheduler
        EnemySpawnScheduler.update(dt, enemies);

        // Check if wave ended and trigger next
        if (!waveRunning && enemies.isEmpty() && currentWaveNumber > 0) {
            startNextWaveTrigger();
        }
    }

    // Renamed method to avoid conflicts
    private void startNextWaveTrigger() {
        if (currentWaveNumber < totalWaves) {
            waveManager.startNextWave(enemies, map.getPath());
            currentWaveNumber++;
        } else if (!repeatingWaves) {
            seed = new Random().nextLong();
            waveManager = new WaveManager(seed);
            waveManager.startNextWave(enemies, map.getPath());
        } else {
            waveManager.startNextWave(enemies, map.getPath());
            currentWaveNumber++;
        }
        waveRunning = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        map.draw(g2);

        for (Tower t : towers) t.draw(g2);
        for (Projectile p : projectiles) p.draw(g2);
        for (Enemy e : enemies) e.draw(g2);

        if (focusedTower != null) {
            Point gp = focusedTower.getGridPosition();
            int radius = focusedTower.getRadius();
            g2.setColor(new Color(0, 0, 255, 50));
            g2.fillOval(gp.x * Map.TILE_SIZE - radius + Map.TILE_SIZE / 2,
                        gp.y * Map.TILE_SIZE - radius + Map.TILE_SIZE / 2,
                        radius * 2, radius * 2);
        }

        drawHUD(g2);

        // Upgrade button with icon
        if (focusedTower != null && focusedTower.canUpgrade()) {
            int bx = Map.WIDTH * Map.TILE_SIZE + 20;
            int by = 50;
            g2.setColor(Color.GRAY);
            g2.fillRect(bx, by, 120, 40);
            g2.setColor(Color.WHITE);
            g2.drawRect(bx, by, 120, 40);
            g2.drawString("Upgrade $" + focusedTower.getUpgradeCost(), bx + 40, by + 25);
            if (upgradeButtonIcon != null) {
                g2.drawImage(upgradeButtonIcon, bx + 5, by + 5, 30, 30, null);
            }
        }

        // Start wave button
        if (playButtonImage != null) {
            g2.drawImage(playButtonImage, playButtonX, playButtonY, playButtonWidth, playButtonHeight, null);
        }
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, Map.HEIGHT * Map.TILE_SIZE, Map.WIDTH * Map.TILE_SIZE, UI_BAR_HEIGHT);

        g2.drawImage(moneyIcon, 10, Map.HEIGHT * Map.TILE_SIZE + 5, 32, 32, null);
        g2.setColor(Color.YELLOW);
        g2.drawString(String.valueOf(playerMoney), 50, Map.HEIGHT * Map.TILE_SIZE + 25);

        g2.drawImage(healthIcon, 120, Map.HEIGHT * Map.TILE_SIZE + 5, 32, 32, null);
        g2.setColor(Color.RED);
        g2.drawString(String.valueOf(playerLives), 160, Map.HEIGHT * Map.TILE_SIZE + 25);

        g2.setColor(Color.WHITE);
        g2.drawString("1 - Gun Tower ($100)", 300, Map.HEIGHT * Map.TILE_SIZE + 20);
        g2.drawString("2 - Ice Tower ($150)", 300, Map.HEIGHT * Map.TILE_SIZE + 40);
        g2.drawString("3 - Bomb Tower ($200)", 300, Map.HEIGHT * Map.TILE_SIZE + 60);
        g2.drawString("SPACE - Start Next Wave", 450, Map.HEIGHT * Map.TILE_SIZE + 40);

        if (!running && playerLives <= 0) {
            g2.setColor(Color.RED);
            g2.drawString("GAME OVER! Press ESC to exit or restart.", 600, Map.HEIGHT * Map.TILE_SIZE + 60);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // Check if click is on start wave button
        if (x >= playButtonX && x <= playButtonX + playButtonWidth
            && y >= playButtonY && y <= playButtonY + playButtonHeight) {
            if (!waveRunning) {
                startNextWaveTrigger();
            }
            return;
        }

        int tileX = x / Map.TILE_SIZE;
        int tileY = y / Map.TILE_SIZE;
        if (tileY >= Map.HEIGHT) return;

        // Check if click is on a tower
        focusedTower = null;
        for (Tower t : towers) {
            Point gp = t.getGridPosition();
            if (gp.x == tileX && gp.y == tileY) {
                focusedTower = t;
                return;
            }
        }

        // Handle tower placement
        if (selectedTowerType != 0 && playerMoney >= 100) {
            Point clickTile = new Point(tileX, tileY);

            // Validate placement
            if (map.getPath().contains(clickTile))
                return; // Can't place on path
            if (tileX < 0 || tileX >= Map.WIDTH || tileY < 0 || tileY >= Map.HEIGHT)
                return; // Out of bounds
            for (Tower t : towers) {
                if (t.getGridPosition().equals(clickTile))
                    return; // Already occupied
            }

            // Place tower
            Tower t;
            int cost = 0;
            switch (selectedTowerType) {
                case 1:
                    t = new GunTower(tileX, tileY, gunTowerSprite);
                    cost = 100;
                    break;
                case 2:
                    t = new IceTower(tileX, tileY, iceTowerSprite);
                    cost = 150;
                    break;
                case 3:
                    t = new BombTower(tileX, tileY, bombTowerSprite);
                    cost = 200;
                    break;
                default:
                    return;
            }
            towers.add(t);
            playerMoney -= cost;
            focusedTower = t;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Handle upgrade button click
        if (focusedTower != null && focusedTower.canUpgrade()) {
            int mx = e.getX();
            int my = e.getY();
            int bx = Map.WIDTH * Map.TILE_SIZE + 20;
            int by = 50;
            if (mx >= bx && mx <= bx + upgradeButtonWidth && my >= by && my <= by + upgradeButtonHeight) {
                int cost = focusedTower.getUpgradeCost();
                if (playerMoney >= cost) {
                    focusedTower.upgrade();
                    playerMoney -= cost;
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_1:
                selectedTowerType = 1;
                break;
            case KeyEvent.VK_2:
                selectedTowerType = 2;
                break;
            case KeyEvent.VK_3:
                selectedTowerType = 3;
                break;
            case KeyEvent.VK_SPACE:
                if (!waveRunning) {
                    startNextWaveTrigger();
                }
                break;
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // Method to start next wave considering the wave count logic
    private void triggerStartNextWave() {
        if (currentWaveNumber < totalWaves) {
            waveManager.startNextWave(enemies, map.getPath());
            currentWaveNumber++;
        } else if (!repeatingWaves) {
            seed = new Random().nextLong();
            waveManager = new WaveManager(seed);
            waveManager.startNextWave(enemies, map.getPath());
        } else {
            waveManager.startNextWave(enemies, map.getPath());
            currentWaveNumber++;
        }
        waveRunning = true;
    }
}
