package compasses.expandedstorage.thread.client;

import compasses.expandedstorage.common.misc.Utils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;

public class WrappedVanillaKeybind implements Keybinding {

    private final KeyMapping binding;

    public WrappedVanillaKeybind() {
        this.binding = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.ellemes_container_lib.config", Utils.KEY_BIND_KEY, "key.categories.inventory"));
    }

    @Override
    public boolean matches(int keyCode, int scanCode) {
        return binding.matches(keyCode, scanCode) && Screen.hasShiftDown();
    }
}
