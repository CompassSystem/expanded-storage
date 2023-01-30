package ellemes.expandedstorage.common.mixin;

import ellemes.container_library.api.v3.OpenableInventoryProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class NoUsePacketOnPartialConsume {

    boolean blockSuccessResultMarker;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract InteractionResult performUseItemOn(LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult);

    @Shadow
    protected abstract void startPrediction(ClientLevel clientLevel, PredictiveAction predictiveAction);

    @Inject(
            method = "useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V"
            ),
            cancellable = true
    )
    private void expandedstorage$idk(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        blockSuccessResultMarker = false;
        BlockState state = minecraft.level.getBlockState(hit.getBlockPos());
        boolean isSecondaryUse = (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) && player.isSecondaryUseActive();
        if (!isSecondaryUse && state.getBlock() instanceof OpenableInventoryProvider<?>) {
            InteractionResult result = performUseItemOn(player, hand, hit);
            if (blockSuccessResultMarker) {
                if (result != InteractionResult.CONSUME_PARTIAL) {
                    startPrediction(minecraft.level, (i) -> new ServerboundUseItemOnPacket(hand, hit, i));
                }
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(
            method = "performUseItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at = @At(
                    value = "RETURN",
                    opcode = 1
            )
    )
    private void expandedstorage$idk2(LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
            blockSuccessResultMarker = true;
    }
}
