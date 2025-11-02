import java.util.List;
import java.util.ArrayList;

/**
 * Handles delayed enemy spawns for smoother waves.
 * Each enemy is scheduled with a time delay, and this class updates them each frame.
 */
public class EnemySpawnScheduler {

    private static class ScheduledSpawn {
        Enemy enemy;
        double delay;
        ScheduledSpawn(Enemy e, double d) {
            enemy = e;
            delay = d;
        }
    }

    private static final List<ScheduledSpawn> scheduled = new ArrayList<>();

    /** Schedule an enemy to appear after a certain delay (seconds). */
    public static void schedule(List<Enemy> enemies, Enemy e, double delay) {
        scheduled.add(new ScheduledSpawn(e, delay));
    }

    /** Called every frame by the game to update delays and spawn enemies when ready. */
    public static void update(double dt, List<Enemy> enemies) {
        List<ScheduledSpawn> toSpawn = new ArrayList<>();

        for (ScheduledSpawn s : scheduled) {
            s.delay -= dt;
            if (s.delay <= 0) toSpawn.add(s);
        }

        // Spawn ready enemies and remove them from the queue
        for (ScheduledSpawn s : toSpawn) {
            enemies.add(s.enemy);
            scheduled.remove(s);
        }
    }

    /** Returns true if all scheduled spawns have finished. */
    public static boolean isDone() {
        return scheduled.isEmpty();
    }

    /** Clear all scheduled enemies (optional reset). */
    public static void clear() {
        scheduled.clear();
    }
}
