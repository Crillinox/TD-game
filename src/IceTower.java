import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

public class IceTower extends Tower {
    private BufferedImage sprite;

    public IceTower(int x, int y, BufferedImage sprite) {
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
                    projectiles.add(new Projectile(gridPosition.x*TILE_SIZE+32, gridPosition.y*TILE_SIZE+32, e, "ice", 0));
                    fireCooldown = 1.5 / fireRate;
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
            case 2: range = (int)(range*1.85); break;          // bigger radius
            case 3: fireRate *= 2; range *= 2; break;          // adds gun, increased range
            case 4: /* optional: make all projectiles slow in radius */ break;
        }
        upgradeCost *= 2;
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(sprite, gridPosition.x*Map.TILE_SIZE, gridPosition.y*Map.TILE_SIZE, 64, 64, null);
    }

    public int getRadius() { return range; }

    private static final int TILE_SIZE = 64;
}
