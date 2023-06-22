package compasses.expandedstorage.fabric;

import compasses.expandedstorage.thread.ThreadClientHelper;
import net.fabricmc.loader.api.FabricLoader;

public class FabricClientHelper extends ThreadClientHelper {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
