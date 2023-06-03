package compasses.expandedstorage.common.misc;

public interface ClientPlatformHelper {
    boolean isConfigKeyPressed(int keyCode, int scanCode, int modifiers);

    boolean isModLoaded(String modId);

    ConfigWrapper configWrapper();
}
