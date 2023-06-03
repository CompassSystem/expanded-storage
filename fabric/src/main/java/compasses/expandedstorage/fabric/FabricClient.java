package compasses.expandedstorage.fabric;

import compasses.expandedstorage.thread.ThreadClient;
import compasses.expandedstorage.thread.ThreadMain;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ThreadClient.initialize(new FabricClientHelper());

        ClientPlayConnectionEvents.INIT.register((_unused_1, _unused_2) -> {
            ClientPlayNetworking.registerReceiver(ThreadMain.UPDATE_RECIPES_ID, (client, handler, buffer, responseSender) -> {
                ThreadClient.handleUpdateRecipesPacket(client, handler, buffer);
            });
        });
    }
}
