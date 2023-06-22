package compasses.expandedstorage.thread;

import compasses.expandedstorage.common.misc.ClientPlatformHelper;
import compasses.expandedstorage.thread.client.Keybinding;
import compasses.expandedstorage.thread.client.WrappedAmecsKeybind;
import compasses.expandedstorage.thread.client.WrappedVanillaKeybind;

public abstract class ThreadClientHelper implements ClientPlatformHelper {
    private final Keybinding binding;

    protected ThreadClientHelper() {
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
}
