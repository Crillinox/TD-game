import java.awt.*;
import java.util.List;

public abstract class Tower {
    protected Point gridPosition;
    protected int level = 1;
    protected double fireRate = 1.0;
    protected double fireCooldown = 0.0;
    protected int range = 128;
    protected int upgradeCost = 100;

    public Tower(int x,int y){ gridPosition=new Point(x,y); }

    public abstract void update(double dt, List<Enemy> enemies, List<Projectile> projectiles);
    public abstract boolean canUpgrade();
    public abstract void upgrade();

    public int getLevel(){ return level; }
    public int getUpgradeCost(){ return upgradeCost; }
    public Point getGridPosition(){ return gridPosition; }
    public int getRadius(){ return range; }

    public void draw(Graphics2D g2){}
}
