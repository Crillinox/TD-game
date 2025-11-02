import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class NormalEnemy extends Enemy {
    public NormalEnemy(List<Point> path) {
        super(path, 20, 120, 10); // faster
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(Color.RED);
        g2.fillOval((int)position.x, (int)position.y, 48, 48);
    }

    @Override
    public int getReward() { return 10; }
}
