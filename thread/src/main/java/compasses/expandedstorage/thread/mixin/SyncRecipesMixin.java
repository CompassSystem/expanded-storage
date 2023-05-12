package compasses.expandedstorage.thread.mixin;

import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.recipe.ConversionRecipeManager;
import compasses.expandedstorage.thread.ThreadCommonPlatformHelper;
import net.minecraft.core.LayeredRegistryAccess;
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
    private void expandedstorage$setServerInstance(MinecraftServer minecraftServer, LayeredRegistryAccess<?> registryAccess, PlayerDataStorage storage, int maxPlayers, CallbackInfo ci) {
        ((ThreadCommonPlatformHelper) CommonMain.platformHelper()).setServerInstance(minecraftServer);
    }

    @Inject(
            method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V"
            )
    )

    private void expandedstorage$sendResourcesToNewPlayer(Connection connection, ServerPlayer player, CallbackInfo ci) {
        CommonMain.platformHelper().sendConversionRecipesToClient(player, ConversionRecipeManager.INSTANCE.getBlockRecipes(), ConversionRecipeManager.INSTANCE.getEntityRecipes());
    }

    @Inject(
            method = "reloadResources()V",
            at = @At("TAIL")
    )
    private void expandedstorage$sendResourcesToConnectedPlayers(CallbackInfo ci) {
        CommonMain.platformHelper().sendConversionRecipesToClient(null, ConversionRecipeManager.INSTANCE.getBlockRecipes(), ConversionRecipeManager.INSTANCE.getEntityRecipes());
    }
}
