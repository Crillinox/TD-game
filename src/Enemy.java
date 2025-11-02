import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;

public abstract class Enemy {
    protected Point2D.Double position;
    protected double speed;
    protected int hp;
    protected int maxHp;
    protected int damage;
    protected boolean slowed = false;

    protected List<Point> path;
    protected int currentTargetIndex = 0;

    public Enemy(List<Point> path, int maxHp, double speed, int damage) {
        this.path = path;
        this.position = new Point2D.Double(path.get(0).x * Map.TILE_SIZE, path.get(0).y * Map.TILE_SIZE);
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.speed = speed;
        this.damage = damage;
    }

    /** Update enemy position along path */
    public void update(double dt) {
        if (currentTargetIndex >= path.size()) return;
        Point targetTile = path.get(currentTargetIndex);
        double targetX = targetTile.x * Map.TILE_SIZE;
        double targetY = targetTile.y * Map.TILE_SIZE;

        double dx = targetX - position.x;
        double dy = targetY - position.y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < speed * dt) {
            position.x = targetX;
            position.y = targetY;
            currentTargetIndex++;
        } else {
            double factor = (speed * dt) / dist;
            position.x += dx * factor;
            position.y += dy * factor;
        }
    }

    /** Apply a slow factor */
    public void slow(double factor) {
        if (!slowed) {
            speed *= factor;
            slowed = true;
        }
    }

    public boolean reachedEnd() { return currentTargetIndex >= path.size(); }
    public boolean isDead() { return hp <= 0; }
    public void takeDamage(int dmg) { hp -= dmg; }
    public Point2D.Double getPosition() { return position; }
    public int getDamage() { return damage; }

    /** Return size for rendering */
    public int getRenderSize() { return 48; }

    /** Draw the enemy */
    public abstract void draw(Graphics2D g2);

    /** Reward for killing this enemy */
    public abstract int getReward();
}
