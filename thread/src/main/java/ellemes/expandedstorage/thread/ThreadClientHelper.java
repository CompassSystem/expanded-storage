package ellemes.expandedstorage.thread;

import ellemes.expandedstorage.common.misc.ClientPlatformHelper;
import ellemes.expandedstorage.common.misc.ConfigWrapper;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.thread.client.Keybinding;
import ellemes.expandedstorage.thread.client.WrappedAmecsKeybind;
import ellemes.expandedstorage.thread.client.WrappedVanillaKeybind;
import ellemes.expandedstorage.thread.wrappers.ConfigWrapperImpl;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ThreadClientHelper implements ClientPlatformHelper {
    private final ConfigWrapperImpl configWrapper;
    private final Keybinding binding;

    {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        configWrapper = new ConfigWrapperImpl(configDir.resolve(Utils.CONFIG_PATH), configDir.resolve("ninjaphenix-container-library.json"));

        if (isModLoaded("amecs")) {
            binding = new WrappedAmecsKeybind();
        } else {
            binding = new WrappedVanillaKeybind();
        }
    }

    @Override
    public boolean isConfigKeyPressed(int keyCode, int scanCode, int modifiers) {
        return binding.matches(keyCode, scanCode);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public ConfigWrapper configWrapper() {
        return configWrapper;
    }
}
