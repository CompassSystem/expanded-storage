package compasses.expandedstorage.common.config.client;

import com.google.gson.annotations.SerializedName;
import compasses.expandedstorage.common.config.Config;

public class ClientConfig implements Config {
    @SerializedName("restrictive_scrolling")
    private boolean restrictiveScrolling = false;

    @Override
    public int getVersion() {
        return 0;
    }
}
