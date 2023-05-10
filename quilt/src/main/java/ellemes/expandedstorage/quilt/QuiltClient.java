package ellemes.expandedstorage.quilt;

import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.thread.ThreadClient;
import ellemes.expandedstorage.thread.ThreadMain;
import net.minecraft.server.packs.PackType;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QuiltClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        ThreadClient.initialize(new QuiltClientHelper());

        ClientPlayConnectionEvents.INIT.register((_unused_1, _unused_2) -> {
            ClientPlayNetworking.registerReceiver(ThreadMain.UPDATE_RECIPES_ID, (client, handler, buffer, responseSender) -> {
                ThreadClient.handleUpdateRecipesPacket(client, handler, buffer);
            });
        });

        Path resourcesRoot = QuiltLoader.getGameDir().resolve("expandedstorage");
        Utils.textureSaveRoot = createGuiResourcesFolders(resourcesRoot);
        if (Utils.textureSaveRoot != null) {
            ResourceLoader loader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
            loader.getRegisterDefaultResourcePackEvent().register(context -> {
                context.addResourcePack(loader.newFileSystemResourcePack(Utils.id("gui_textures"), mod, resourcesRoot.resolve("resources"), ResourcePackActivationType.ALWAYS_ENABLED));
            });
        }
    }

    private Path createGuiResourcesFolders(Path root) {
        try {
            Path textureSaveRoot = root.resolve("resources/assets/expandedstorage/textures/gui/container/");
            Files.createDirectories(textureSaveRoot);
            Path mcmeta = root.resolve("resources/pack.mcmeta");
            if (!Files.exists(mcmeta)) {
                try (BufferedWriter writer = Files.newBufferedWriter(root.resolve("resources/pack.mcmeta"))) {
                    writer.write("{ \"pack\": { \"description\": \"Expanded Storage gui resources\", \"pack_format\": 9 } }");
                }
            }
            return textureSaveRoot;
        } catch (IOException e) {
            return null;
        }
    }
}
