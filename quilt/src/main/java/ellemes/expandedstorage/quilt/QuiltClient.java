package ellemes.expandedstorage.quilt;

import ellemes.expandedstorage.thread.ThreadClient;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class QuiltClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        ThreadClient.initialize();
    }
}
