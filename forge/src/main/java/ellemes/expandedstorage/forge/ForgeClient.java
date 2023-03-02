package ellemes.expandedstorage.forge;

import ellemes.expandedstorage.api.client.gui.AbstractScreen;
import ellemes.expandedstorage.api.v3.client.ScreenOpeningApi;
import ellemes.expandedstorage.common.CommonClient;
import ellemes.expandedstorage.common.CommonMain;
import ellemes.expandedstorage.common.client.ChestBlockEntityRenderer;
import ellemes.expandedstorage.common.client.gui.PageScreen;
import ellemes.expandedstorage.common.entity.ChestMinecart;
import ellemes.expandedstorage.common.registration.Content;
import ellemes.expandedstorage.common.registration.NamedValue;
import ellemes.expandedstorage.forge.misc.ForgePlatformHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.stream.Collectors;

public class ForgeClient {
    public static void initialize(IEventBus modBus, Content content) {
        CommonClient.initialize();
        ModLoadingContext.get().getActiveContainer().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((client, screen) -> ScreenOpeningApi.createTypeSelectScreen(() -> screen))
        );

        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, (ScreenEvent.Init.Post event) -> {
            if (event.getScreen() instanceof PageScreen screen) {
                screen.addPageButtons();
            }
        });

        ForgePlatformHelper.instance().clientHelper().init(modBus);

        modBus.addListener((FMLClientSetupEvent event) -> MenuScreens.register(ForgePlatformHelper.instance().getScreenHandlerType(), AbstractScreen::createScreen));

        modBus.addListener((TextureStitchEvent.Pre event) -> {
            if (!event.getAtlas().location().equals(Sheets.CHEST_SHEET)) {
                return;
            }
            for (ResourceLocation texture : CommonMain.getChestTextures(content.getChestBlocks().stream().map(NamedValue::getName).collect(Collectors.toList()))) {
                event.addSprite(texture);
            }
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
        });

        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (NamedValue<EntityType<ChestMinecart>> type : content.getChestMinecartEntityTypes()) {
                event.registerEntityRenderer(type.getValue(), context -> new MinecartRenderer<>(context, ModelLayers.CHEST_MINECART));
            }
        });
    }
}
