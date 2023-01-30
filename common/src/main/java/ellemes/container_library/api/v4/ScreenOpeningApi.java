package ellemes.container_library.api.v4;

import ellemes.container_library.CommonClient;
import ellemes.container_library.api.client.gui.AbstractScreen;
import ellemes.container_library.api.v3.OpenableInventoryProvider;
import ellemes.container_library.client.gui.PickScreen;
import net.minecraft.client.Minecraft;

public class ScreenOpeningApi {
    private ScreenOpeningApi() {
        throw new IllegalStateException("ScreenOpeningApi should not be instantiated.");
    }

    public static boolean ensureScreenTypeSet(OpenableInventoryProvider<?> provider) {
        if (provider.getForcedScreenType() == null && !AbstractScreen.isScreenTypeDeclared(CommonClient.getConfigWrapper().getPreferredScreenType())) {
            Minecraft.getInstance().setScreen(new PickScreen(ScreenOpeningApi::useItem));
            return false;
        }
        return true;
    }

    private static void useItem() {
        Minecraft.getInstance().startUseItem();
    }
}
