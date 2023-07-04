package compasses.expandedstorage.common.client;

public interface Keybinding {
    boolean matches(int keyCode, int scanCode);
}
