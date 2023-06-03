package compasses.expandedstorage.quilt;

import compasses.expandedstorage.common.block.BarrelBlock;
import compasses.expandedstorage.common.registration.NamedValue;
import compasses.expandedstorage.thread.ThreadClient;
import compasses.expandedstorage.thread.ThreadMain;
import net.minecraft.client.renderer.RenderType;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

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
    }
}
