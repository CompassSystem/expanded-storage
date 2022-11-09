package ellemes.expandedstorage.quilt;

import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.network.chat.Component;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;

public class QuiltClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        ResourceLoader.registerBuiltinResourcePack(Utils.id("legacy_textures"), mod, ResourcePackActivationType.NORMAL, Component.literal("ES Legacy Textures"));
    }
}
