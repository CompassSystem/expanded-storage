package compasses.expandedstorage.impl.mixin.common;

import compasses.expandedstorage.impl.recipe.ConversionRecipeManager;
import compasses.expandedstorage.impl.FabricMain;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class SyncRecipesMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(
            method = "reloadResources()V",
            at = @At("TAIL")
    )
    private void expandedstorage$sendResourcesToConnectedPlayers(CallbackInfo ci) {
        FabricMain.sendConversionRecipesToClient(
                ConversionRecipeManager.INSTANCE.getBlockRecipes(),
                ConversionRecipeManager.INSTANCE.getEntityRecipes(),
                server.getPlayerList().getPlayers().toArray(ServerPlayer[]::new)
        );
    }
}
