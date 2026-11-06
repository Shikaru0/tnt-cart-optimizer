package name.modid.client.mixin;

import name.modid.client.TntCartOptimizerClient;
import name.modid.client.TntCartPlacementPredictor;
import name.modid.client.config.TntCartOptimizerConfig;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("TntCartOptimizer");

    @Inject(method = "handleAddEntity", at = @At("HEAD"))
    private void onAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        TntCartOptimizerClient instance = TntCartOptimizerClient.getInstance();
        TntCartOptimizerConfig config = TntCartOptimizerClient.getConfig();
        if (instance == null || config == null || !config.isEnabled()) {
            return;
        }

        if (!"tnt_minecart".equals(BuiltInRegistries.ENTITY_TYPE.getKey(packet.getType()).getPath())) {
            return;
        }

        TntCartPlacementPredictor predictor = instance.getPlacementPredictor();
        if (predictor == null) {
            return;
        }

        Vec3 pos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
        if (config.isLogging()) {
            LOGGER.info("TNT minecart spawn packet at [{}, {}, {}]", pos.x, pos.y, pos.z);
        }
        predictor.onEntitySpawned(pos);
    }
}