package compasses.expandedstorage.common.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import compasses.expandedstorage.common.helpers.client.ScreenOpeningApi;

public final class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return returnToScreen -> ScreenOpeningApi.createTypeSelectScreen(() -> returnToScreen);
    }
}
