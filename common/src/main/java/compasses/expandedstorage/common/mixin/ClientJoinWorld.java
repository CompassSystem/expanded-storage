package compasses.expandedstorage.common.mixin;

import compasses.expandedstorage.common.CommonMain;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPacketListener.class)
public class ClientJoinWorld {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "handleLogin",
            at = @At("TAIL")
    )
    private void expandedstorage$onJoinWorld(ClientboundLoginPacket packet, CallbackInfo ci) {
        String modTargetPlatform = CommonMain.getModTargetPlatform();
        String userPlatform = CommonMain.getUserPlatform();
        if (!Objects.equals(modTargetPlatform, userPlatform) && !CommonMain.platformHelper().platformMarkerExists()) {
            minecraft.player.sendSystemMessage(Component.translatable("text.expandedstorage.prefix", Component.translatable("text.expandedstorage.wrong_loader_warning", CommonMain.getModTargetPlatform(), CommonMain.getUserPlatform()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_AQUA));
            CommonMain.platformHelper().createInvalidPlatformMarker();
        }
    }
}
