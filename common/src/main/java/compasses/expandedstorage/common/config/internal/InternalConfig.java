package compasses.expandedstorage.common.config.internal;

import com.google.gson.annotations.SerializedName;
import compasses.expandedstorage.common.config.common.CommonConfigManager;
import compasses.expandedstorage.common.misc.Utils;
import net.minecraft.SharedConstants;

public class InternalConfig {
    @SerializedName("_README")
    private String warning = "This file should not be edited by the user.";

    @SerializedName("config_version")
    private int configVersion = Utils.CURRENT_CONFIG_VERSION;

    @SerializedName("shown_wrong_platform_version")
    private String shownWrongPlatformVersion = null;

    public boolean showPlatformWarning() {
        String currentVersion = SharedConstants.getCurrentVersion().getName();
        if (shownWrongPlatformVersion == null || !shownWrongPlatformVersion.equals(currentVersion)) {
            shownWrongPlatformVersion = currentVersion;
            CommonConfigManager.saveInternalConfig();
            return true;
        }

        return false;
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(int version) {
        configVersion = version;
        CommonConfigManager.saveInternalConfig();
    }

}
