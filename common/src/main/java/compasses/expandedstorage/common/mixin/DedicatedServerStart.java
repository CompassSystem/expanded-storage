package compasses.expandedstorage.common.mixin;

import compasses.expandedstorage.common.CommonMain;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(DedicatedServer.class)
public class DedicatedServerStart {
    @Inject(
            method = "initServer()Z",
            at = @At("RETURN")
    )
    private void expandedStorage$onServerStarted(CallbackInfoReturnable<Boolean> cir) {
        String modTargetPlatform = CommonMain.getModTargetPlatform();
        String userPlatform = CommonMain.getUserPlatform();
        if (!Objects.equals(modTargetPlatform, userPlatform) && CommonMain.platformHelper().platformMarkerExists()) {
            System.out.printf("[Expanded Storage] Please install the %2$s release of this mod, this hasn't been tested for %1$s so there may be issues which support will not be provided for otherwise.%n", modTargetPlatform, userPlatform);
            CommonMain.platformHelper().createInvalidPlatformMarker();
        }
    }
}
