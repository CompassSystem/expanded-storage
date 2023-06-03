package compasses.expandedstorage.fabric;

import compasses.expandedstorage.common.block.BarrelBlock;
import compasses.expandedstorage.common.registration.NamedValue;
import compasses.expandedstorage.thread.ThreadClient;
import compasses.expandedstorage.thread.ThreadMain;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.RenderType;

public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ThreadClient.initialize(new FabricClientHelper(), content -> {
            for (NamedValue<BarrelBlock> block : content.getBarrelBlocks()) {
                BlockRenderLayerMap.INSTANCE.putBlock(block.getValue(), RenderType.cutoutMipped());
            }
        });

        ClientPlayConnectionEvents.INIT.register((_listener, _client) -> {
            ClientPlayNetworking.registerReceiver(ThreadMain.UPDATE_RECIPES_ID, (client, listener, buffer, responseSender) -> {
                ThreadClient.handleUpdateRecipesPacket(client, listener, buffer);
            });
        });
    }
}
