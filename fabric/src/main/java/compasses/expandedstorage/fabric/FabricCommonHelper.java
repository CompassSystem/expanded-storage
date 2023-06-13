package compasses.expandedstorage.fabric;

import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.thread.ThreadCommonHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricCommonHelper extends ThreadCommonHelper {
    @Override
    protected void sendPacket(ServerPlayer player, ResourceLocation packetId, FriendlyByteBuf buffer) {
        ServerPlayNetworking.send(player, packetId, buffer); // canSend doesn't work :think:
    }

    @Override
    public boolean platformMarkerExists() {
        return Files.exists(FabricLoader.getInstance().getConfigDir().resolve(Utils.MOD_ID + "/announcement_marker.txt"));
    }

    @Override
    public void createInvalidPlatformMarker() {
        Path folder = FabricLoader.getInstance().getConfigDir().resolve(Utils.MOD_ID);
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            return;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(folder.resolve("announcement_marker.txt"))) {
            writer.write("0");
        } catch (IOException ignored) {

        }
    }
}
