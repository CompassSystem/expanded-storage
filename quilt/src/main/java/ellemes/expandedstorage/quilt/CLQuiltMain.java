package ellemes.expandedstorage.quilt;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public final class CLQuiltMain implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        CLThreadMain.initialize();
    }
}
