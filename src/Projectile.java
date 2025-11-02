import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class Projectile {
    private Point2D.Double position;
    private Enemy target;
    private String type;
    private int damage;
    private double speed = 300.0;
    private boolean exploded = false;

    // For bomb clusters
    private boolean isClusterBomb = false;

    public Projectile(double x, double y, Enemy target, String type, int damage) {
        this.position = new Point2D.Double(x, y);
        this.target = target;
        this.type = type;
        this.damage = damage;

        if(type.equals("bomb") && damage == 0) isClusterBomb = true;
    }

    public void update(double dt, List<Projectile> projectiles) {
        if(target == null || target.isDead()) {
            exploded = true;
            return;
        }

        double dx = target.getPosition().x - position.x;
        double dy = target.getPosition().y - position.y;
        double dist = Math.sqrt(dx*dx + dy*dy);

        if(dist < 4.0) {
            switch(type) {
                case "ice":
                    // Slow enemies in radius if tower is level 4
                    slowInRadius(projectiles, 96, 0.5); // 96px radius example
                    break;
                case "bomb":
                    target.takeDamage(damage);
                    spawnClusterIfNeeded(projectiles);
                    break;
                default:
                    target.takeDamage(damage);
            }
            exploded = true;
            return;
        }

        double factor = (speed*dt)/dist;
        position.x += dx*factor;
        position.y += dy*factor;
    }

    // For IceTower upgrade 4: slow all enemies in a radius
    private void slowInRadius(List<Projectile> projectiles, int radius, double slowFactor) {
        for(Projectile p : projectiles) {
            if(p.target != null && !p.target.isDead()) {
                double dx = p.target.getPosition().x - position.x;
                double dy = p.target.getPosition().y - position.y;
                if(Math.sqrt(dx*dx + dy*dy) <= radius) {
                    p.target.slow(slowFactor);
                }
            }
        }
    }

    // For BombTower clusters
    private void spawnClusterIfNeeded(List<Projectile> projectiles) {
        if(isClusterBomb) {
            // Spawn 4 extra bombs around the explosion
            double[][] offsets = {{-32,0},{32,0},{0,-32},{0,32}};
            for(double[] offset : offsets) {
                if(target != null && !target.isDead()) {
                    projectiles.add(new Projectile(
                        position.x + offset[0],
                        position.y + offset[1],
                        target,
                        "bomb",
                        damage
                    ));
                }
            }
        }
    }

    public boolean hasExploded() { return exploded; }

    public Point2D.Double getPosition() { return position; }

    public void draw(Graphics2D g2) {
        switch(type) {
            case "ice": g2.setColor(Color.CYAN); break;
            case "bomb": g2.setColor(Color.ORANGE); break;
            default: g2.setColor(Color.YELLOW);
        }
        g2.fillOval((int)position.x, (int)position.y, 24, 24);
    }
}
