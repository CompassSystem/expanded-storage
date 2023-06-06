package compasses.expandedstorage.forge;

import compasses.expandedstorage.common.CommonClient;
import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.client.ChestBlockEntityRenderer;
import compasses.expandedstorage.common.client.gui.PageScreen;
import compasses.expandedstorage.common.entity.ChestMinecart;
import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.common.registration.Content;
import compasses.expandedstorage.common.registration.ModItems;
import compasses.expandedstorage.common.registration.NamedValue;
import compasses.expandedstorage.forge.misc.ForgeClientHelper;
import ellemes.expandedstorage.api.client.gui.AbstractScreen;
import ellemes.expandedstorage.api.v3.client.ScreenOpeningApi;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ForgeClient {
    public static void initialize(IEventBus modBus, Content content) {
        CommonClient.initialize(new ForgeClientHelper(modBus));
        ModLoadingContext.get().getActiveContainer().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((client, screen) -> ScreenOpeningApi.createTypeSelectScreen(() -> screen))
        );

        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, (ScreenEvent.Init.Post event) -> {
            if (event.getScreen() instanceof PageScreen screen) {
                screen.addPageButtons();
            }
        });

        modBus.addListener((FMLClientSetupEvent event) -> {
            MenuScreens.register(CommonMain.platformHelper().getScreenHandlerType(), AbstractScreen::createScreen);
            ItemProperties.registerGeneric(Utils.id("sparrow"), CommonClient::hasSparrowProperty);
            ItemProperties.register(ModItems.STORAGE_MUTATOR, Utils.id("tool_mode"), CommonClient::currentMutatorToolMode);
        });

        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(content.getChestBlockEntityType().getValue(), ChestBlockEntityRenderer::new);
        });

        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
            event.registerLayerDefinition(ChestBlockEntityRenderer.SINGLE_LAYER, ChestBlockEntityRenderer::createSingleBodyLayer);
            event.registerLayerDefinition(ChestBlockEntityRenderer.LEFT_LAYER, ChestBlockEntityRenderer::createLeftBodyLayer);
            event.registerLayerDefinition(ChestBlockEntityRenderer.RIGHT_LAYER, ChestBlockEntityRenderer::createRightBodyLayer);
            event.registerLayerDefinition(ChestBlockEntityRenderer.TOP_LAYER, ChestBlockEntityRenderer::createTopBodyLayer);
            event.registerLayerDefinition(ChestBlockEntityRenderer.BOTTOM_LAYER, ChestBlockEntityRenderer::createBottomBodyLayer);
            event.registerLayerDefinition(ChestBlockEntityRenderer.FRONT_LAYER, ChestBlockEntityRenderer::createFrontBodyLayer);
            event.registerLayerDefinition(ChestBlockEntityRenderer.BACK_LAYER, ChestBlockEntityRenderer::createBackBodyLayer);
            event.registerLayerDefinition(ChestBlockEntityRenderer.CUSTOM_LOCK_LAYER, ChestBlockEntityRenderer::createCustomLockLayer);
        });

        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (NamedValue<EntityType<ChestMinecart>> type : content.getChestMinecartEntityTypes()) {
                event.registerEntityRenderer(type.getValue(), context -> new MinecartRenderer<>(context, ModelLayers.CHEST_MINECART));
            }
        });
    }
}
