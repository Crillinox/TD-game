import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class FastEnemy extends Enemy {
    public FastEnemy(List<Point> path) {
        super(path, 10, 240, 5);
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(Color.ORANGE);
        g2.fillOval((int)position.x, (int)position.y, 32, 32);
    }

    @Override
    public int getReward() { return 7; }
}
