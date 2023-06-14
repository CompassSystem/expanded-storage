package compasses.expandedstorage.fabric;

import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.thread.ThreadCommonHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public class FabricCommonHelper extends ThreadCommonHelper {
    @Override
    protected void sendPacket(ServerPlayer player, ResourceLocation packetId, FriendlyByteBuf buffer) {
        ServerPlayNetworking.send(player, packetId, buffer); // canSend doesn't work :think:
    }

    @Override
    public Path getLocalConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(Utils.MOD_ID);
    }
}
