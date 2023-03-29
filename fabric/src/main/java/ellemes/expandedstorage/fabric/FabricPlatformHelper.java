package ellemes.expandedstorage.fabric;

import ellemes.expandedstorage.thread.ThreadPlatformHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformHelper extends ThreadPlatformHelper {
    @Override
    protected void sendPacket(ServerPlayer player, ResourceLocation packetId, FriendlyByteBuf buffer) {
        ServerPlayNetworking.send(player, packetId, buffer); // canSend doesn't work :think:
    }
}
