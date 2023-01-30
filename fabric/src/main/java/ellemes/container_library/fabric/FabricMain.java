package ellemes.container_library.fabric;

import ellemes.container_library.thread.ThreadMain;
import net.fabricmc.api.ModInitializer;

public final class FabricMain implements ModInitializer {
    @Override
    public void onInitialize() {
        ThreadMain.initialize();
    }
}
