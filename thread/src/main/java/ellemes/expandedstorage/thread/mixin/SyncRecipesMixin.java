package ellemes.expandedstorage.thread.mixin;

import ellemes.expandedstorage.common.misc.PlatformHelper;
import ellemes.expandedstorage.common.recipe.ConversionRecipeManager;
import ellemes.expandedstorage.thread.ThreadPlatformHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class SyncRecipesMixin {
    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void setServerInstance(MinecraftServer minecraftServer, RegistryAccess.Frozen frozen, PlayerDataStorage storage, int maxPlayers, CallbackInfo ci) {
        ThreadPlatformHelper.instance().setServerInstance(minecraftServer);
    }

    @Inject(
            method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V"
            )
    )

    private void sendResourcesToNewPlayer(Connection connection, ServerPlayer player, CallbackInfo ci) {
        PlatformHelper.instance().sendConversionRecipesToClient(player, ConversionRecipeManager.INSTANCE.getBlockRecipes(), ConversionRecipeManager.INSTANCE.getEntityRecipes());
    }

    @Inject(
            method = "reloadResources()V",
            at = @At("TAIL")
    )
    private void sendResourcesToConnectedPlayers(CallbackInfo ci) {
        PlatformHelper.instance().sendConversionRecipesToClient(null, ConversionRecipeManager.INSTANCE.getBlockRecipes(), ConversionRecipeManager.INSTANCE.getEntityRecipes());
    }
}
