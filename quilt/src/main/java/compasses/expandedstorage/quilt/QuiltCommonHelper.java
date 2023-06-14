package compasses.expandedstorage.quilt;

import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.thread.ThreadCommonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.nio.file.Path;

public class QuiltCommonHelper extends ThreadCommonHelper {
    @Override
    protected void sendPacket(ServerPlayer player, ResourceLocation packetId, FriendlyByteBuf buffer) {
        ServerPlayNetworking.send(player, packetId, buffer); // canSend doesn't work :think:
    }

    @Override
    public Path getLocalConfigPath() {
        return QuiltLoader.getConfigDir().resolve(Utils.MOD_ID);
    }
}
