package ellemes.expandedstorage.quilt;

import ellemes.expandedstorage.thread.ThreadClientHelper;
import org.quiltmc.loader.api.QuiltLoader;

public class QuiltClientHelper extends ThreadClientHelper {
    QuiltClientHelper() {
        super(QuiltLoader.getConfigDir());
    }

    @Override
    public boolean isModLoaded(String modId) {
        return QuiltLoader.isModLoaded(modId);
    }
}
