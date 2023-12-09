package compass_system.expanded_storage.client.mixin;

import compass_system.expanded_storage.ExpandedStorageClient;
import compass_system.expanded_storage.barrel.block.BarrelBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TerrainParticle.class)
public abstract class FixBreakParticleMixin extends TextureSheetParticle {
    protected FixBreakParticleMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
            at = @At("TAIL")
    )
    private void renderBreakingParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockState state, BlockPos pos, CallbackInfo ci) {
        if (state.getBlock() instanceof BarrelBlock block) {
            Block baseBlock = (Block) level.getBlockEntityRenderData(pos);

            if (baseBlock == null) {
                baseBlock = Blocks.BARREL;
            }

            setSprite(ExpandedStorageClient.INSTANCE.getModelPlugin().getBarrelBreakingParticle(baseBlock.builtInRegistryHolder().key().location(), state, block.getTextureId()));
        }
    }
}
