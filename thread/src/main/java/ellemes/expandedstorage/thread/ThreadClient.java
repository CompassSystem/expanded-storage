package ellemes.expandedstorage.thread;

import ellemes.expandedstorage.api.client.gui.AbstractScreen;
import ellemes.expandedstorage.common.CommonClient;
import ellemes.expandedstorage.common.misc.PlatformHelper;
import net.minecraft.client.gui.screens.MenuScreens;

public class ThreadClient {
    public static void initialize() {
        CommonClient.initialize();
        MenuScreens.register(PlatformHelper.instance().getScreenHandlerType(), AbstractScreen::createScreen);
    }
}
