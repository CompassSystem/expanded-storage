package ellemes.expandedstorage.quilt;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class CLQuiltClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        CLThreadClient.initialize(QuiltLoader.getConfigDir(), QuiltLoader::isModLoaded);
    }
}
