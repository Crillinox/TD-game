import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

public class GunTower extends Tower {
    private BufferedImage sprite;
    private int damage = 10;

    public GunTower(int x, int y, BufferedImage sprite) {
        super(x, y);
        this.sprite = sprite;
    }

    @Override
    public void update(double dt, List<Enemy> enemies, List<Projectile> projectiles) {
        fireCooldown -= dt;
        if (fireCooldown <= 0) {
            for (Enemy e : enemies) {
                double dx = e.getPosition().x/Map.TILE_SIZE - gridPosition.x;
                double dy = e.getPosition().y/Map.TILE_SIZE - gridPosition.y;
                double dist = Math.sqrt(dx*dx + dy*dy);
                if (dist*TILE_SIZE <= range) {
                    projectiles.add(new Projectile(gridPosition.x*TILE_SIZE+32, gridPosition.y*TILE_SIZE+32, e, "bullet", damage));
                    fireCooldown = 1.0 / fireRate;
                    break;
                }
            }
        }
    }

    @Override
    public boolean canUpgrade() { return level < 4; }

    @Override
    public void upgrade() {
        level++;
        switch(level) {
            case 2: damage += 10; break;          // +damage
            case 3: fireRate *= 1.5; break;       // +speed
            case 4: damage *= 3; fireRate *= 3; break; // full upgrade
        }
        upgradeCost *= 2;
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(sprite, gridPosition.x*Map.TILE_SIZE, gridPosition.y*Map.TILE_SIZE, 64, 64, null);
    }

    public int getRadius() { return range; }

    private static final int TILE_SIZE = 64;
}
