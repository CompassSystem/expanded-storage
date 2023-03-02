package ellemes.expandedstorage.fabric;

import ellemes.expandedstorage.thread.ThreadClientHelper;
import net.fabricmc.loader.api.FabricLoader;

public class FabricClientHelper extends ThreadClientHelper {
    FabricClientHelper() {
        super(FabricLoader.getInstance().getConfigDir());
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
