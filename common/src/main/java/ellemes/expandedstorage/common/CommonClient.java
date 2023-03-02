package ellemes.expandedstorage.common;

import ellemes.expandedstorage.api.v3.client.ScreenTypeApi;
import ellemes.expandedstorage.common.client.gui.FakePickScreen;
import ellemes.expandedstorage.common.client.gui.PageScreen;
import ellemes.expandedstorage.common.client.gui.ScrollScreen;
import ellemes.expandedstorage.common.client.gui.SingleScreen;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CommonClient {
    public static void initialize() {
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
}