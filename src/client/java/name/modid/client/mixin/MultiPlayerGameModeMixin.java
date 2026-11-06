package name.modid.client.mixin;

import name.modid.client.TntCartOptimizerClient;
import name.modid.client.config.TntCartOptimizerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("TntCartOptimizer");

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void intercept(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        TntCartOptimizerConfig config = TntCartOptimizerClient.getConfig();
        if (config == null || !config.isEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof MinecartItem)) {
            return;
        }

        if (!client.level.getBlockState(client.player.blockPosition()).is(BlockTags.RAILS)) {
            return;
        }

        cir.setReturnValue(InteractionResult.SUCCESS);

        if (config.isLogging()) {
            LOGGER.info("[InteractionManager] cancelled minecart use on rail at player pos [{}, {}, {}] (item={})",
                    client.player.getX(), client.player.getY(), client.player.getZ(),
                    stack.getItem().getClass().getSimpleName());
        }
    }
}