package compasses.expandedstorage.common;

import compasses.expandedstorage.api.EsChestType;
import compasses.expandedstorage.common.block.MiniStorageBlock;
import compasses.expandedstorage.common.client.gui.MiniStorageScreen;
import compasses.expandedstorage.common.client.gui.PageScreen;
import compasses.expandedstorage.common.client.gui.ScrollScreen;
import compasses.expandedstorage.common.client.gui.SingleScreen;
import compasses.expandedstorage.common.helpers.client.ScreenTypeApi;
import compasses.expandedstorage.common.item.MutationMode;
import compasses.expandedstorage.common.item.StorageMutator;
import compasses.expandedstorage.common.misc.ClientPlatformHelper;
import compasses.expandedstorage.common.misc.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonClient {
    private static final Map<ResourceLocation, ResourceLocation[]> CHEST_TEXTURES = new HashMap<>();
    private static ClientPlatformHelper platformHelper;

    public static void initialize(ClientPlatformHelper helper, CommonMain.Initializer initializer) {
        platformHelper = helper;

        initializer.chestBlocks.forEach(block -> {
            String blockId = block.getName().getPath();
            CommonClient.declareChestTextures(block.getName(),
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
                Component.translatable("screen.ellemes_container_lib.page_screen"));
        ScreenTypeApi.registerScreenButton(Utils.SCROLLABLE_SCREEN_TYPE,
                Utils.id("textures/gui/scroll_button.png"),
                Component.translatable("screen.ellemes_container_lib.scroll_screen"));
        ScreenTypeApi.registerScreenButton(Utils.SINGLE_SCREEN_TYPE,
                Utils.id("textures/gui/single_button.png"),
                Component.translatable("screen.ellemes_container_lib.single_screen"),
                (scaledWidth, scaledHeight) -> scaledWidth < 370 || scaledHeight < 386, // Smallest possible resolution a double netherite chest fits on.
                List.of(
                        Component.translatable("screen.ellemes_container_lib.off_screen_warning_1").withStyle(ChatFormatting.GRAY),
                        Component.translatable("screen.ellemes_container_lib.off_screen_warning_2").withStyle(ChatFormatting.GRAY)
                ));

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
        if (!CommonClient.CHEST_TEXTURES.containsKey(block)) {
            ResourceLocation[] collection = {topTexture, bottomTexture, frontTexture, backTexture, leftTexture, rightTexture, singleTexture};
            CommonClient.CHEST_TEXTURES.put(block, collection);
        } else {
            throw new IllegalArgumentException("Tried registering chest textures for \"" + block + "\" which already has textures.");
        }
    }

    public static ResourceLocation getChestTexture(ResourceLocation block, EsChestType chestType) {
        if (CommonClient.CHEST_TEXTURES.containsKey(block)) {
            return CommonClient.CHEST_TEXTURES.get(block)[chestType.ordinal()];
        }
        return MissingTextureAtlasSprite.getLocation();
    }

    public static ClientPlatformHelper platformHelper() {
        return platformHelper;
    }
}
