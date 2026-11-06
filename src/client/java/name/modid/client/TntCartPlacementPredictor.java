package name.modid.client;

import name.modid.client.config.TntCartOptimizerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TntCartPlacementPredictor {
    private static final Logger LOGGER = LoggerFactory.getLogger("TntCartOptimizer");

    private final TntCartOptimizerConfig config;
    private final List<PredictedPlacement> predictions = new ArrayList<>();
    private final List<ConfirmedPlacement> confirmedPlacements = new ArrayList<>();

    public TntCartPlacementPredictor(TntCartOptimizerConfig config) {
        this.config = config;
        if (config.isLogging()) {
            LOGGER.info("TntCartPlacementPredictor initialized (enabled={})", config.isEnabled());
        }
    }

    public void tick(Minecraft minecraft) {
        if (!config.isEnabled() || minecraft.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        int beforePred = predictions.size();
        int beforeConf = confirmedPlacements.size();
        predictions.removeIf(p -> now - p.timestamp > 5000L);
        confirmedPlacements.removeIf(p -> now - p.confirmTime > 500L);
        if (beforePred > predictions.size() || beforeConf > confirmedPlacements.size() && config.isLogging()) {
            LOGGER.debug("Pruned expired entries: predictions {} -> {}, confirmed {} -> {}",
                    beforePred, predictions.size(), beforeConf, confirmedPlacements.size());
        }
    }

    public void onPlacementAttempt(Vec3 position) {
        if (!config.isEnabled()) {
            return;
        }
        predictions.add(new PredictedPlacement(position, System.currentTimeMillis()));
        if (config.isLogging()) {
            LOGGER.debug("Placement attempt recorded at [{}, {}, {}] (pending={})",
                    position.x, position.y, position.z, predictions.size());
        }
    }

    public void onEntitySpawned(Vec3 pos) {
        if (config.isLogging()) {
            LOGGER.debug("TNT minecart spawned at [{}, {}, {}]", pos.x, pos.y, pos.z);
        }
        Iterator<PredictedPlacement> it = predictions.iterator();
        while (it.hasNext()) {
            PredictedPlacement p = it.next();
            if (p.position.distanceToSqr(pos) < 4.0) {
                confirmedPlacements.add(new ConfirmedPlacement(pos, System.currentTimeMillis()));
                it.remove();
                if (config.isLogging()) {
                    LOGGER.info("Confirmed TNT cart placement at [{}, {}, {}] (pending={}, confirmed={})",
                            pos.x, pos.y, pos.z, predictions.size(), confirmedPlacements.size());
                }
                break;
            }
        }
    }

    public List<ConfirmedPlacement> getConfirmedPlacements() {
        return confirmedPlacements;
    }

    public static final class ConfirmedPlacement {
        public final Vec3 position;
        public final long confirmTime;

        public ConfirmedPlacement(Vec3 position, long confirmTime) {
            this.position = position;
            this.confirmTime = confirmTime;
        }
    }

    private static final class PredictedPlacement {
        final Vec3 position;
        final long timestamp;

        PredictedPlacement(Vec3 position, long timestamp) {
            this.position = position;
            this.timestamp = timestamp;
        }
    }
}