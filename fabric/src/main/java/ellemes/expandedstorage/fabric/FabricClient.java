package ellemes.expandedstorage.fabric;

import ellemes.expandedstorage.thread.ThreadClient;
import net.fabricmc.api.ClientModInitializer;

public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ThreadClient.initialize();
    }
}
