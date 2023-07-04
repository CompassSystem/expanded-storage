package compasses.expandedstorage.impl;

import com.google.common.base.Suppliers;
import compasses.expandedstorage.api.EsChestType;
import compasses.expandedstorage.impl.block.MiniStorageBlock;
import compasses.expandedstorage.impl.block.entity.ChestBlockEntity;
import compasses.expandedstorage.impl.client.ChestBlockEntityRenderer;
import compasses.expandedstorage.impl.client.gui.AbstractScreen;
import compasses.expandedstorage.impl.client.gui.MiniStorageScreen;
import compasses.expandedstorage.impl.client.gui.PageScreen;
import compasses.expandedstorage.impl.client.gui.ScrollScreen;
import compasses.expandedstorage.impl.client.gui.SingleScreen;
import compasses.expandedstorage.impl.entity.ChestMinecart;
import compasses.expandedstorage.impl.client.helpers.ScreenTypeApi;
import compasses.expandedstorage.impl.item.ChestMinecartItem;
import compasses.expandedstorage.impl.item.MutationMode;
import compasses.expandedstorage.impl.item.StorageMutator;
import compasses.expandedstorage.impl.misc.Utils;
import compasses.expandedstorage.impl.recipe.BlockConversionRecipe;
import compasses.expandedstorage.impl.recipe.ConversionRecipeManager;
import compasses.expandedstorage.impl.recipe.EntityConversionRecipe;
import compasses.expandedstorage.impl.registration.ModItems;
import compasses.expandedstorage.impl.registration.NamedValue;
import compasses.expandedstorage.impl.client.Keybinding;
import compasses.expandedstorage.impl.client.WrappedAmecsKeybind;
import compasses.expandedstorage.impl.client.WrappedVanillaKeybind;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FabricClient implements ClientModInitializer {
    private static final Map<ResourceLocation, ResourceLocation[]> CHEST_TEXTURES = new HashMap<>();
    private static final FabricLoader LOADER = FabricLoader.getInstance();
    private static final Keybinding BINDING;

    static {
        if (FabricClient.isModLoaded("amecs")) {
            BINDING = new WrappedAmecsKeybind();
        } else {
            BINDING = new WrappedVanillaKeybind();
        }
    }

    @Override
    public void onInitializeClient() {
        CommonMain.Initializer initializer = FabricMain.getInitializeForClient();

        initializer.chestBlocks.forEach(block -> {
            String blockId = block.getName().getPath();
            FabricClient.declareChestTextures(block.getName(),
                    Utils.id("entity/chest/" + blockId + "_single"),
                    Utils.id("entity/chest/" + blockId + "_left"),
                    Utils.id("entity/chest/" + blockId + "_right"),
                    Utils.id("entity/chest/" + blockId + "_top"),
                    Utils.id("entity/chest/" + blockId + "_bottom"),
                    Utils.id("entity/chest/" + blockId + "_front"),
                    Utils.id("entity/chest/" + blockId + "_back")
            );
        });

        ScreenTypeApi.registerScreenButton(Utils.PAGINATED_SCREEN_TYPE,
                Utils.id("textures/gui/page_button.png"),
                Component.translatable("screen.ellemes_container_lib.page_screen")
        );
        ScreenTypeApi.registerScreenButton(Utils.SCROLLABLE_SCREEN_TYPE,
                Utils.id("textures/gui/scroll_button.png"),
                Component.translatable("screen.ellemes_container_lib.scroll_screen")
        );
        ScreenTypeApi.registerScreenButton(Utils.SINGLE_SCREEN_TYPE,
                Utils.id("textures/gui/single_button.png"),
                Component.translatable("screen.ellemes_container_lib.single_screen"),
                (scaledWidth, scaledHeight) -> scaledWidth < 370 || scaledHeight < 386, // Smallest possible resolution a double netherite chest fits on.
                List.of(
                        Component.translatable("screen.ellemes_container_lib.off_screen_warning_1").withStyle(ChatFormatting.GRAY),
                        Component.translatable("screen.ellemes_container_lib.off_screen_warning_2").withStyle(ChatFormatting.GRAY)
                )
        );

        ScreenTypeApi.registerScreenType(Utils.PAGINATED_SCREEN_TYPE, PageScreen::new);
        ScreenTypeApi.registerScreenType(Utils.SCROLLABLE_SCREEN_TYPE, ScrollScreen::new);
        ScreenTypeApi.registerScreenType(Utils.SINGLE_SCREEN_TYPE, SingleScreen::new);
        ScreenTypeApi.registerScreenType(Utils.MINI_STORAGE_SCREEN_TYPE, MiniStorageScreen::new);

        // todo: these settings leave no room for rei/jei should we take those into consideration for minimum screen width
        ScreenTypeApi.registerDefaultScreenSize(Utils.PAGINATED_SCREEN_TYPE, PageScreen::retrieveScreenSize);
        ScreenTypeApi.registerDefaultScreenSize(Utils.SCROLLABLE_SCREEN_TYPE, ScrollScreen::retrieveScreenSize);
        ScreenTypeApi.registerDefaultScreenSize(Utils.SINGLE_SCREEN_TYPE, SingleScreen::retrieveScreenSize);
        ScreenTypeApi.registerDefaultScreenSize(Utils.MINI_STORAGE_SCREEN_TYPE, MiniStorageScreen::retrieveScreenSize);

        ScreenTypeApi.setPrefersSingleScreen(Utils.PAGINATED_SCREEN_TYPE);
        ScreenTypeApi.setPrefersSingleScreen(Utils.SCROLLABLE_SCREEN_TYPE);

        MenuScreens.register(FabricMain.getScreenHandlerType(), AbstractScreen::createScreen);

        FabricClient.registerChestBlockEntityRenderer();
        FabricClient.registerItemRenderers(initializer.chestItems);
        FabricClient.registerMinecartEntityRenderers(initializer.getChestMinecartEntityTypes());
        FabricClient.registerMinecartItemRenderers(initializer.getChestMinecartAndTypes());
        FabricClient.registerInventoryTabsCompat();

        ItemProperties.registerGeneric(Utils.id("sparrow"), FabricClient::hasSparrowProperty);
        ItemProperties.register(ModItems.STORAGE_MUTATOR, Utils.id("tool_mode"), FabricClient::currentMutatorToolMode);

        ClientPlayConnectionEvents.INIT.register((_listener, _client) -> {
            ClientPlayNetworking.registerReceiver(FabricMain.UPDATE_RECIPES_ID, (client, listener, buffer, responseSender) -> {
                FabricClient.handleUpdateRecipesPacket(client, listener, buffer);
            });
        });
    }

    @SuppressWarnings("unused")
    public static float hasSparrowProperty(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int i) {
        return MiniStorageBlock.hasSparrowProperty(stack) ? 1.0f : 0.0f;
    }

    @SuppressWarnings("unused")
    public static float currentMutatorToolMode(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int i) {
        MutationMode mode = StorageMutator.getMode(stack);
        boolean isSparrow = stack.hasCustomHoverName() && stack.getHoverName().getString().equalsIgnoreCase("sparrow");
        if (mode == MutationMode.SWAP_THEME) {
            if (isSparrow) {
                return 1.0F;
            }
            return 0.8F;
        } else if (mode == MutationMode.ROTATE) {
            return 0.6F;
        } else if (mode == MutationMode.SPLIT) {
            return 0.4F;
        } else if (mode == MutationMode.MERGE) {
            return 0.2F;
        }
        return 0.0F;
    }

    public static void declareChestTextures(ResourceLocation block, ResourceLocation singleTexture, ResourceLocation leftTexture, ResourceLocation rightTexture, ResourceLocation topTexture, ResourceLocation bottomTexture, ResourceLocation frontTexture, ResourceLocation backTexture) {
        if (!FabricClient.CHEST_TEXTURES.containsKey(block)) {
            ResourceLocation[] collection = {topTexture, bottomTexture, frontTexture, backTexture, leftTexture, rightTexture, singleTexture};
            FabricClient.CHEST_TEXTURES.put(block, collection);
        } else {
            throw new IllegalArgumentException("Tried registering chest textures for \"" + block + "\" which already has textures.");
        }
    }

    public static ResourceLocation getChestTexture(ResourceLocation block, EsChestType chestType) {
        if (FabricClient.CHEST_TEXTURES.containsKey(block)) {
            return FabricClient.CHEST_TEXTURES.get(block)[chestType.ordinal()];
        }
        return MissingTextureAtlasSprite.getLocation();
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

            BuiltinItemRendererRegistry.INSTANCE.register(item.getValue(), (itemStack, transform, stack, source, light, overlay) -> {
                Minecraft.getInstance().getEntityRenderDispatcher().render(renderEntity.get(), 0, 0, 0, 0, 0, stack, source, light);
            });
        });
    }

    public static void registerInventoryTabsCompat() {
        if (FabricClient.isModLoaded("inventorytabs")) {
//                InventoryTabCompat.register();
        }
    }

    public static boolean isModLoaded(String id) {
        return LOADER.isModLoaded(id);
    }

    public static boolean isConfigKeyPressed(int keyCode, int scanCode, int modifiers) {
        return BINDING.matches(keyCode, scanCode);
    }
}
