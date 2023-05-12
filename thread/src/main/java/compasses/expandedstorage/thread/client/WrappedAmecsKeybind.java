package compasses.expandedstorage.thread.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import compasses.expandedstorage.common.misc.Utils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class WrappedAmecsKeybind implements Keybinding {

    private final KeyMapping binding;

    public WrappedAmecsKeybind() {
        this.binding = KeyBindingHelper.registerKeyBinding(new AmecsKeyBinding(Utils.containerId("config"), InputConstants.Type.KEYSYM, Utils.KEY_BIND_KEY, "key.categories.inventory", new KeyModifiers().setShift(true)));
    }

    @Override
    public boolean matches(int keyCode, int scanCode) {
        return binding.matches(keyCode, scanCode);
    }
}
