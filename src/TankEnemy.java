import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class TankEnemy extends Enemy {
    public TankEnemy(List<Point> path) {
        super(path, 40, 60, 20);
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(Color.DARK_GRAY);
        g2.fillOval((int)position.x, (int)position.y, 64, 64);
    }

    @Override
    public int getReward() { return 20; }
}
