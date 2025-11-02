import java.awt.Point;
import java.util.List;
import java.util.Random;

public class WaveManager {
    private int currentWave = 0;
    private Random random;

    public WaveManager(long seed) {
        random = new Random(seed);
    }

    public void startNextWave(List<Enemy> enemies, List<Point> path) {
        // Prevent multiple calls
        if (isWaveInProgress()) return;

        currentWave++;
        // You can add wave spawning logic here, like adding enemies with delays
        int baseCount = 5 + currentWave * 2;
        double healthMultiplier = getHealthMultiplier();

        for (int i = 0; i < baseCount; i++) {
            int type = random.nextInt(100);
            Enemy e;
            if (type < 50) e = new NormalEnemy(path);
            else if (type < 80) e = new FastEnemy(path);
            else e = new TankEnemy(path);

            e.hp = (int) Math.ceil(e.maxHp * healthMultiplier);
            e.maxHp = e.hp;

            double delay = i * 0.5 + random.nextDouble();
            EnemySpawnScheduler.schedule(enemies, e, delay);
        }
    }

    private double getHealthMultiplier() {
        if (currentWave <= 10) return 1.0 / 3.0;
        if (currentWave <= 20) return 0.5;
        if (currentWave <= 40) return 1.0;
        return Math.pow(1.1, currentWave - 40);
    }

    public void endWave() {
        // Mark wave as finished
        // You can add more logic here if needed
    }

    public boolean isWaveInProgress() {
        // implement if needed, but for now, just manage externally
        return false; // For simplicity, we manage wave flags in Game.java
    }

    public int getCurrentWave() {
        return currentWave;
    }
}
