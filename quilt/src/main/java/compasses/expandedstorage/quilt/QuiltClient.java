package compasses.expandedstorage.quilt;

import compasses.expandedstorage.common.block.BarrelBlock;
import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.common.registration.NamedValue;
import compasses.expandedstorage.thread.ThreadClient;
import compasses.expandedstorage.thread.ThreadMain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.packs.PackType;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
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
        ThreadClient.initialize(new QuiltClientHelper(), content -> {
            for (NamedValue<BarrelBlock> block : content.getBarrelBlocks()) {
                BlockRenderLayerMap.put(RenderType.cutoutMipped(), block.getValue());
            }
        });

        ClientPlayConnectionEvents.INIT.register((_listener, _client) -> {
            ClientPlayNetworking.registerReceiver(ThreadMain.UPDATE_RECIPES_ID, (client, listener, buffer, responseSender) -> {
                ThreadClient.handleUpdateRecipesPacket(client, listener, buffer);
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
