package name.modid.client;

import name.modid.client.config.TntCartOptimizerConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TntCartOptimizerClient implements ClientModInitializer {
	private static final Logger log = LogManager.getLogger(TntCartOptimizerClient.class);
	private static TntCartOptimizerClient instance;
	private static TntCartOptimizerConfig config;
	private TntCartPlacementPredictor placementPredictor;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		instance = this;
		config = new TntCartOptimizerConfig();
		config.load();

		placementPredictor = new TntCartPlacementPredictor(config);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			placementPredictor.tick(client);
		});

		UseBlockCallback.EVENT.register(this::onUseBlock);
	}

	private InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (placementPredictor == null) {
			return  InteractionResult.PASS;
		}

		ItemStack stack = player.getItemInHand(hand);
		if (stack.isEmpty() || !(stack.getItem() instanceof MinecartItem)) {
			return InteractionResult.PASS;
		}

		BlockPos blockPos = hitResult.getBlockPos();
		Vec3 predicted = new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
		placementPredictor.onPlacementAttempt(predicted);

		if (config.isLogging()) {
			log.info("placementPredictor added predicted {}", predicted);
		}

		return InteractionResult.PASS;
	}

	public static TntCartOptimizerClient getInstance() { return instance; }
	public static TntCartOptimizerConfig getConfig() { return config; }
	public TntCartPlacementPredictor getPlacementPredictor() { return placementPredictor; }
}