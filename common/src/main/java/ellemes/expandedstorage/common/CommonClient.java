package ellemes.expandedstorage.common;

import ellemes.expandedstorage.common.block.MiniStorageBlock;
import ellemes.expandedstorage.common.item.MutationMode;
import ellemes.expandedstorage.common.item.StorageMutator;
import ellemes.expandedstorage.common.v3.client.ScreenTypeApi;
import ellemes.expandedstorage.common.client.gui.FakePickScreen;
import ellemes.expandedstorage.common.client.gui.PageScreen;
import ellemes.expandedstorage.common.client.gui.ScrollScreen;
import ellemes.expandedstorage.common.client.gui.SingleScreen;
import ellemes.expandedstorage.common.misc.ClientPlatformHelper;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommonClient {
    private static ClientPlatformHelper platformHelper;

    public static void initialize(ClientPlatformHelper helper) {
        platformHelper = helper;
        ScreenTypeApi.registerScreenButton(Utils.PAGE_SCREEN_TYPE,
                Utils.id("textures/gui/page_button.png"),
                Component.translatable("screen.ellemes_container_lib.page_screen"));
        ScreenTypeApi.registerScreenButton(Utils.SCROLL_SCREEN_TYPE,
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

        ScreenTypeApi.registerScreenType(Utils.UNSET_SCREEN_TYPE, FakePickScreen::new);
        ScreenTypeApi.registerScreenType(Utils.PAGE_SCREEN_TYPE, PageScreen::new);
        ScreenTypeApi.registerScreenType(Utils.SCROLL_SCREEN_TYPE, ScrollScreen::new);
        ScreenTypeApi.registerScreenType(Utils.SINGLE_SCREEN_TYPE, SingleScreen::new);

        // todo: these settings leave no room for rei/jei should we take those into consideration for minimum screen width
        ScreenTypeApi.registerDefaultScreenSize(Utils.UNSET_SCREEN_TYPE, FakePickScreen::retrieveScreenSize);
        ScreenTypeApi.registerDefaultScreenSize(Utils.PAGE_SCREEN_TYPE, PageScreen::retrieveScreenSize);
        ScreenTypeApi.registerDefaultScreenSize(Utils.SCROLL_SCREEN_TYPE, ScrollScreen::retrieveScreenSize);
        ScreenTypeApi.registerDefaultScreenSize(Utils.SINGLE_SCREEN_TYPE, SingleScreen::retrieveScreenSize);

        ScreenTypeApi.setPrefersSingleScreen(Utils.PAGE_SCREEN_TYPE);
        ScreenTypeApi.setPrefersSingleScreen(Utils.SCROLL_SCREEN_TYPE);
    }

    public static float hasSparrowProperty(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int i) {
        return MiniStorageBlock.hasSparrowProperty(stack) ? 1.0f : 0.0f;
    }

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

    public static ClientPlatformHelper platformHelper() {
        return platformHelper;
    }
}
