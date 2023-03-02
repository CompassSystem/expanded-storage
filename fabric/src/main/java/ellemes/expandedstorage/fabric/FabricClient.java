package ellemes.expandedstorage.fabric;

import ellemes.expandedstorage.thread.ThreadClient;
import ellemes.expandedstorage.thread.ThreadMain;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ThreadClient.initialize();

        ClientPlayConnectionEvents.INIT.register((_unused_1, _unused_2) -> {
            ClientPlayNetworking.registerReceiver(ThreadMain.UPDATE_RECIPES_ID, (client, handler, buffer, responseSender) -> {
                ThreadClient.handleUpdateRecipesPacket(client, handler, buffer);
            });
        });
    }
}
