package ellemes.expandedstorage.thread.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ellemes.expandedstorage.common.v3.client.ScreenOpeningApi;

public final class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return returnToScreen -> ScreenOpeningApi.createTypeSelectScreen(() -> returnToScreen);
    }
}
