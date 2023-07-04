package compasses.expandedstorage.impl.config.client;

import com.google.gson.annotations.SerializedName;
import compasses.expandedstorage.impl.config.Config;
import net.minecraft.resources.ResourceLocation;

public class ClientConfigV0 implements Config {
    @SerializedName("scrolling_unrestricted")
    private boolean scrollingUnrestricted = false;

    @SerializedName("defaults")
    private Defaults defaults = new Defaults();

    public boolean isScrollingUnrestricted() {
        return scrollingUnrestricted;
    }

    public boolean prefersSmallerScreens() {
        return false;
    }

    private static class Defaults {
        private ResourceLocation screenType = null;
    }

    public void setDefaultScreenType(ResourceLocation screenType) {
        defaults.screenType = screenType;
        ClientConfigManager.saveConfig();
    }

    public ResourceLocation getDefaultScreenType() {
        return defaults.screenType;
    }

    @Override
    public int getVersion() {
        return 0;
    }
}
