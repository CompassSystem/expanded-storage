package compasses.expandedstorage.quilt;

import compasses.expandedstorage.thread.ThreadCommonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class QuiltCommonHelper extends ThreadCommonHelper {
    @Override
    protected void sendPacket(ServerPlayer player, ResourceLocation packetId, FriendlyByteBuf buffer) {
        ServerPlayNetworking.send(player, packetId, buffer); // canSend doesn't work :think:
    }
}
