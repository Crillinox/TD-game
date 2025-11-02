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
    private BufferedImage moneyIcon, healthIcon, upgradeButtonIcon;

    private int playerMoney = 500;
    private int playerLives = 20;
    private int selectedTowerType = 0; // 0=none, 1=gun, 2=ice, 3=bomb
    private Tower focusedTower = null; // currently focused tower

    private WaveManager waveManager;
    private boolean waveRunning = false;

    private Thread gameThread;
    private boolean running = false;

    public Game() {
        setFocusable(true);
        requestFocus();
        addMouseListener(this);
        addKeyListener(this);

        loadSprites();
        map = new Map();
        long seed = new Random().nextLong();
        waveManager = new WaveManager(seed);

        setPreferredSize(new Dimension(Map.WIDTH * Map.TILE_SIZE + 150, Map.HEIGHT * Map.TILE_SIZE + UI_BAR_HEIGHT));
    }

    private void loadSprites() {
        try {
            gunTowerSprite = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Towers/GunTower.png"));
            iceTowerSprite = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Towers/IceTower.png"));
            bombTowerSprite = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Towers/bombShooter.png"));

            normalEnemySprite = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Enemies/normalEnemy.png"));
            fastEnemySprite   = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Enemies/fastEnemy.png"));
            tankEnemySprite   = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Enemies/tankEnemy.png"));

            tileGrass = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_grass.png"));
            tilePath  = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_path.png"));
            tileSpawn = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_spawn.png"));
            tileGoal  = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_goal.png"));

            moneyIcon  = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Button/moneyIcon.png"));
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

        Iterator<Enemy> ei = enemies.iterator();
        while(ei.hasNext()) {
            Enemy e = ei.next();
            e.update(dt);
            if (e.isDead()) {
                playerMoney += e.getReward();
                ei.remove();
            } else if(e.reachedEnd()) {
                playerLives -= e.getDamage();
                ei.remove();
            }
        }

        Iterator<Projectile> pi = projectiles.iterator();
        while(pi.hasNext()) {
            Projectile p = pi.next();
            p.update(dt, projectiles);
            if(p.hasExploded()) pi.remove();
        }

        for(Tower t : towers) t.update(dt, enemies, projectiles);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        map.draw(g2);

        for(Tower t : towers) t.draw(g2);
        for(Projectile p : projectiles) p.draw(g2);
        for(Enemy e : enemies) e.draw(g2);

        // Draw only focused tower range
        if(focusedTower != null) {
            Point gp = focusedTower.getGridPosition();
            int radius = focusedTower.getRadius();
            g2.setColor(new Color(0,0,255,50));
            g2.fillOval(gp.x*Map.TILE_SIZE - radius + Map.TILE_SIZE/2,
                        gp.y*Map.TILE_SIZE - radius + Map.TILE_SIZE/2,
                        radius*2, radius*2);
        }

        drawHUD(g2);

        // Draw upgrade button
        if(focusedTower != null && focusedTower.canUpgrade()) {
            int bx = Map.WIDTH*Map.TILE_SIZE + 20;
            int by = 50;
            g2.setColor(Color.GRAY);
            g2.fillRect(bx, by, 120, 40);
            g2.setColor(Color.WHITE);
            g2.drawRect(bx, by, 120, 40);
            g2.drawString("Upgrade $" + focusedTower.getUpgradeCost(), bx + 10, by + 25);
        }
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0,0,0,150));
        g2.fillRect(0, Map.HEIGHT*Map.TILE_SIZE, Map.WIDTH*Map.TILE_SIZE, UI_BAR_HEIGHT);

        g2.drawImage(moneyIcon, 10, Map.HEIGHT*Map.TILE_SIZE + 5, 32, 32, null);
        g2.setColor(Color.YELLOW);
        g2.drawString(String.valueOf(playerMoney), 50, Map.HEIGHT*Map.TILE_SIZE + 25);

        g2.drawImage(healthIcon, 120, Map.HEIGHT*Map.TILE_SIZE + 5, 32, 32, null);
        g2.setColor(Color.RED);
        g2.drawString(String.valueOf(playerLives), 160, Map.HEIGHT*Map.TILE_SIZE + 25);

        g2.setColor(Color.WHITE);
        g2.drawString("1 - Gun Tower ($100)", 300, Map.HEIGHT*Map.TILE_SIZE + 20);
        g2.drawString("2 - Ice Tower ($150)", 300, Map.HEIGHT*Map.TILE_SIZE + 40);
        g2.drawString("3 - Bomb Tower ($200)", 300, Map.HEIGHT*Map.TILE_SIZE + 60);
        g2.drawString("SPACE - Start Next Wave", 450, Map.HEIGHT*Map.TILE_SIZE + 40);

        if(!running && playerLives <= 0) {
            g2.setColor(Color.RED);
            g2.drawString("GAME OVER! Press ESC to exit or restart the program.", 600, Map.HEIGHT*Map.TILE_SIZE + 60);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX() / Map.TILE_SIZE;
        int y = e.getY() / Map.TILE_SIZE;
        if (y >= Map.HEIGHT) return;

        focusedTower = null;
        for(Tower t : towers) {
            Point gp = t.getGridPosition();
            if(gp.x == x && gp.y == y) {
                focusedTower = t;
                return;
            }
        }

        if(selectedTowerType != 0 && playerMoney >= 100) {
            Point clickTile = new Point(x, y);
            if(map.getPath().contains(clickTile)) return;

            for(Tower t : towers) if(t.getGridPosition().equals(clickTile)) return;

            Tower t;
            int cost = 0;
            switch(selectedTowerType) {
                case 1: t = new GunTower(x, y, gunTowerSprite); cost = 100; break;
                case 2: t = new IceTower(x, y, iceTowerSprite); cost = 150; break;
                case 3: t = new BombTower(x, y, bombTowerSprite); cost = 200; break;
                default: return;
            }
            towers.add(t);
            playerMoney -= cost;
            focusedTower = t;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Check upgrade button click
        if(focusedTower != null && focusedTower.canUpgrade()) {
            int mx = e.getX();
            int my = e.getY();
            int bx = Map.WIDTH*Map.TILE_SIZE + 20;
            int by = 50;
            if(mx >= bx && mx <= bx+120 && my >= by && my <= by+40) {
                int cost = focusedTower.getUpgradeCost();
                if(playerMoney >= cost) {
                    focusedTower.upgrade();
                    playerMoney -= cost;
                }
            }
        }
    }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_1: selectedTowerType = 1; break;
            case KeyEvent.VK_2: selectedTowerType = 2; break;
            case KeyEvent.VK_3: selectedTowerType = 3; break;
            case KeyEvent.VK_SPACE:
                if(!waveRunning) {
                    waveManager.startNextWave(enemies, map.getPath());
                    waveRunning = true;
                }
                break;
            case KeyEvent.VK_ESCAPE: System.exit(0); break;
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
