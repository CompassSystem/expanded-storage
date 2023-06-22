package compasses.expandedstorage.forge.misc;

import com.mojang.blaze3d.platform.InputConstants;
import compasses.expandedstorage.common.misc.ClientPlatformHelper;
import compasses.expandedstorage.common.misc.Utils;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

public class ForgeClientHelper implements ClientPlatformHelper {
    private final KeyMapping binding = new KeyMapping("key.expandedstorage.open_config_screen", KeyConflictContext.GUI, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, Utils.KEY_BIND_KEY, "key.categories.inventory");

    public ForgeClientHelper(IEventBus modBus) {
        modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(binding));
    }

    @Override
    public boolean isConfigKeyPressed(int keyCode, int scanCode, int modifiers) {
        return binding.matches(keyCode, scanCode);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
