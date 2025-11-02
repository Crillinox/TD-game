import java.awt.Point;
import java.util.List;
import java.util.Random;

public class WaveManager {
    private int currentWave = 0;
    private Random random;
    private boolean waveInProgress = false;

    public WaveManager(long seed) {
        random = new Random(seed);
    }

    public void startNextWave(List<Enemy> enemies, List<Point> path) {
        if (waveInProgress) return; // prevent overlap

        waveInProgress = true;
        currentWave++;

        // Base enemy count and mix
        int baseCount = 5 + currentWave * 2;
        double healthMultiplier = getHealthMultiplier();

        for (int i = 0; i < baseCount; i++) {
            int type = random.nextInt(100); // probability-based spawn variety
            Enemy e;
            if (type < 50) e = new NormalEnemy(path);
            else if (type < 80) e = new FastEnemy(path);
            else e = new TankEnemy(path);

            // Scale HP using floats
            e.hp = (int) Math.ceil(e.maxHp * healthMultiplier);
            e.maxHp = e.hp;

            // Add slight spawn offset (so enemies appear gradually)
            double delay = i * 0.5 + random.nextDouble();
            EnemySpawnScheduler.schedule(enemies, e, delay);
        }
    }

    private double getHealthMultiplier() {
        if (currentWave <= 10) return 1.0 / 3.0;
        else if (currentWave <= 20) return 0.5;
        else if (currentWave <= 40) return 1.0;
        else return Math.pow(1.1, currentWave - 40);
    }

    public void endWave() { waveInProgress = false; }

    public boolean isWaveInProgress() { return waveInProgress; }

    public int getCurrentWave() { return currentWave; }
}
