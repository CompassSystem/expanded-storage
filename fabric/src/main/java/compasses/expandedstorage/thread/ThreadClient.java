package compasses.expandedstorage.thread;

import com.google.common.base.Suppliers;
import compasses.expandedstorage.common.CommonClient;
import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.block.entity.ChestBlockEntity;
import compasses.expandedstorage.common.client.ChestBlockEntityRenderer;
import compasses.expandedstorage.common.client.gui.AbstractScreen;
import compasses.expandedstorage.common.entity.ChestMinecart;
import compasses.expandedstorage.common.item.ChestMinecartItem;
import compasses.expandedstorage.common.misc.ClientPlatformHelper;
import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.common.recipe.BlockConversionRecipe;
import compasses.expandedstorage.common.recipe.ConversionRecipeManager;
import compasses.expandedstorage.common.recipe.EntityConversionRecipe;
import compasses.expandedstorage.common.registration.ModItems;
import compasses.expandedstorage.common.registration.NamedValue;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ThreadClient {
    public static void initialize(ClientPlatformHelper helper) {
        CommonMain.Initializer initializer = ThreadMain.getInitializeForClient();
        CommonClient.initialize(helper, initializer);

        MenuScreens.register(CommonMain.platformHelper().getScreenHandlerType(), AbstractScreen::createScreen);

        ThreadClient.registerChestBlockEntityRenderer();
        ThreadClient.registerItemRenderers(initializer.chestItems);
        ThreadClient.registerMinecartEntityRenderers(initializer.getChestMinecartEntityTypes());
        ThreadClient.registerMinecartItemRenderers(initializer.getChestMinecartAndTypes());
        ThreadClient.registerInventoryTabsCompat();

        ItemProperties.registerGeneric(Utils.id("sparrow"), CommonClient::hasSparrowProperty);
        ItemProperties.register(ModItems.STORAGE_MUTATOR, Utils.id("tool_mode"), CommonClient::currentMutatorToolMode);
    }

    @SuppressWarnings("unused")
    public static void handleUpdateRecipesPacket(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer) {
        List<BlockConversionRecipe<?>> blockRecipes = new ArrayList<>(buffer.readCollection(ArrayList::new, BlockConversionRecipe::readFromBuffer));
        List<EntityConversionRecipe<?>> entityRecipes = new ArrayList<>(buffer.readCollection(ArrayList::new, EntityConversionRecipe::readFromBuffer));
        client.execute(() -> ConversionRecipeManager.INSTANCE.replaceAllRecipes(blockRecipes, entityRecipes));
    }

    public static void registerChestBlockEntityRenderer() {
        BlockEntityRenderers.register(CommonMain.getChestBlockEntityType(), ChestBlockEntityRenderer::new);
    }

    public static void registerItemRenderers(List<NamedValue<BlockItem>> items) {
        for (NamedValue<BlockItem> item : items) {
            ChestBlockEntity renderEntity = CommonMain.getChestBlockEntityType().create(BlockPos.ZERO, item.getValue().getBlock().defaultBlockState());
            BuiltinItemRendererRegistry.INSTANCE.register(item.getValue(), (itemStack, context, stack, source, light, overlay) -> {
                renderEntity.setCustomName(itemStack.getHoverName());
                Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(renderEntity, stack, source, light, overlay);
            });
        }
        EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.SINGLE_LAYER, ChestBlockEntityRenderer::createSingleBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.LEFT_LAYER, ChestBlockEntityRenderer::createLeftBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.RIGHT_LAYER, ChestBlockEntityRenderer::createRightBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.TOP_LAYER, ChestBlockEntityRenderer::createTopBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.BOTTOM_LAYER, ChestBlockEntityRenderer::createBottomBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.FRONT_LAYER, ChestBlockEntityRenderer::createFrontBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.BACK_LAYER, ChestBlockEntityRenderer::createBackBodyLayer);
    }

    public static void registerMinecartEntityRenderers(List<NamedValue<EntityType<ChestMinecart>>> chestMinecartEntityTypes) {
        for (NamedValue<EntityType<ChestMinecart>> type : chestMinecartEntityTypes) {
            EntityRendererRegistry.register(type.getValue(), context -> new MinecartRenderer<>(context, ModelLayers.CHEST_MINECART));
        }
    }

    public static void registerMinecartItemRenderers(Map<NamedValue<ChestMinecartItem>, NamedValue<EntityType<ChestMinecart>>> chestMinecartAndTypes) {
        chestMinecartAndTypes.forEach((item, type) -> {
            Supplier<ChestMinecart> renderEntity = Suppliers.memoize(() -> type.getValue().create(Minecraft.getInstance().level));
            BuiltinItemRendererRegistry.INSTANCE.register(item.getValue(), (itemStack, transform, stack, source, light, overlay) ->
                    Minecraft.getInstance().getEntityRenderDispatcher().render(renderEntity.get(), 0, 0, 0, 0, 0, stack, source, light));
        });
    }

    public static void registerInventoryTabsCompat() {
        if (CommonClient.platformHelper().isModLoaded("inventorytabs")) {
//                InventoryTabCompat.register();
        }
    }
}
