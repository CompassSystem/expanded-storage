package compasses.expandedstorage.impl.client;

import org.lwjgl.glfw.GLFW;

public interface Keybinding {
    int KEY_BIND_KEY = GLFW.GLFW_KEY_G;

    boolean matches(int keyCode, int scanCode);
}
