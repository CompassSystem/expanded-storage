package ellemes.expandedstorage.quilt;

import ellemes.expandedstorage.thread.ThreadClient;
import ellemes.expandedstorage.thread.ThreadMain;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class QuiltClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        ThreadClient.initialize();

        ClientPlayConnectionEvents.INIT.register((_unused_1, _unused_2) -> {
            ClientPlayNetworking.registerReceiver(ThreadMain.UPDATE_RECIPES_ID, (client, handler, buffer, responseSender) -> {
                ThreadClient.handleUpdateRecipesPacket(client, handler, buffer);
            });
        });
    }
}
