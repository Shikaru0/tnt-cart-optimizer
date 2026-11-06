package name.modid.client.mixin;

import name.modid.client.TntCartOptimizerClient;
import name.modid.client.TntCartPlacementPredictor;
import name.modid.client.config.TntCartOptimizerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartItem.class)
public class MinecartItemMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("TntCartOptimizer");

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void onUseOn(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
        TntCartOptimizerConfig config = TntCartOptimizerClient.getConfig();
        if (config == null || !config.isEnabled()) {
            return;
        }

        if (!ctx.getLevel().isClientSide()) {
            return;
        }

        BlockPos clickedPos = ctx.getClickedPos();
        BlockState clickedState = ctx.getLevel().getBlockState(clickedPos);
        if (!clickedState.is(BlockTags.RAILS)) {
            return;
        }

        Player player = ctx.getPlayer();
        if (player == null || player.isCreative()) {
            return;
        }

        var predictor = TntCartOptimizerClient.getInstance().getPlacementPredictor();
        if (predictor != null) {
            Vec3 predicted = new Vec3(
                    clickedPos.getX() + 0.5, clickedPos.getY(), clickedPos.getZ() + 0.5);
            predictor.onPlacementAttempt(predicted);
        }

        player.getItemInHand(ctx.getHand()).shrink(1);
        cir.setReturnValue(InteractionResult.SUCCESS);

        if (config.isLogging()) {
            LOGGER.info("[MinecartItem] placement at [{}, {}, {}] (player={}, item={})",
                    clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(),
                    player.getName().getString(),
                    ctx.getItemInHand().getItem().getClass().getSimpleName());
        }
    }
}