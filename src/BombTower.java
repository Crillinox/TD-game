import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class BombTower extends Tower {
    private BufferedImage sprite;
    private int clusterCounter = 0; // track every other shot for cluster

    private static final int TILE_SIZE = 64;

    public BombTower(int x, int y, BufferedImage sprite) {
        super(x, y);
        if(sprite == null) {
            try {
                this.sprite = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Towers/bombShooter.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.sprite = sprite;
        }

        // Base tower stats
        range = 128;
        fireRate = 0.5;
        upgradeCost = 100;
    }

    @Override
    public void update(double dt, List<Enemy> enemies, List<Projectile> projectiles) {
        fireCooldown -= dt;
        if(fireCooldown <= 0) {
            for(Enemy e : enemies) {
                double dx = e.getPosition().x / TILE_SIZE - gridPosition.x;
                double dy = e.getPosition().y / TILE_SIZE - gridPosition.y;
                double dist = Math.sqrt(dx*dx + dy*dy);
                if(dist * TILE_SIZE <= range) {

                    // Handle cluster logic per upgrade level
                    if(level == 2) { // second upgrade: every other shot spawns 8-cluster
                        clusterCounter++;
                        if(clusterCounter % 2 == 0) {
                            spawnCluster(projectiles, e, 8, 0);
                        }
                    } else if(level == 3) { // third upgrade: every shot spawns 8, every other spawns 4
                        clusterCounter++;
                        spawnCluster(projectiles, e, 8, 0);
                        if(clusterCounter % 2 == 0) {
                            spawnCluster(projectiles, e, 4, 64); // 4 bombs offset from each 8
                        }
                    }

                    // Main bomb projectile
                    projectiles.add(new Projectile(gridPosition.x*TILE_SIZE+32, gridPosition.y*TILE_SIZE+32, e, "bomb", 30));

                    fireCooldown = 2.0 / fireRate;
                    break;
                }
            }
        }
    }

    private void spawnCluster(List<Projectile> projectiles, Enemy target, int count, double offsetStep) {
        double angleStep = 360.0 / count;
        for(int i=0; i<count; i++) {
            double angle = Math.toRadians(i * angleStep);
            double offsetX = Math.cos(angle) * offsetStep;
            double offsetY = Math.sin(angle) * offsetStep;
            projectiles.add(new Projectile(gridPosition.x*TILE_SIZE+32 + offsetX,
                                           gridPosition.y*TILE_SIZE+32 + offsetY,
                                           target, "bomb", 30));
        }
    }

    @Override
    public boolean canUpgrade() { return level < 3; }

    @Override
    public void upgrade() {
        level++;
        switch(level) {
            case 2: range *= 2.25; upgradeCost += 150; break; // bigger blast + cluster
            case 3: range *= 4; upgradeCost += 300; break; // full cluster upgrade
        }
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(sprite, gridPosition.x*TILE_SIZE, gridPosition.y*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
    }

    public int getRadius() { return range; }
}
