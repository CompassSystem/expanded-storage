package ellemes.container_library.thread;

import ellemes.container_library.CommonMain;
import ellemes.container_library.api.inventory.AbstractHandler;
import ellemes.container_library.thread.wrappers.ThreadNetworkWrapper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;

public class ThreadMain {
    public static void initialize() {
        CommonMain.initialize(
                (handlerType, factory) -> {
                    MenuType<AbstractHandler> type = new ExtendedScreenHandlerType<>(factory::create);
                    return Registry.register(Registry.MENU, handlerType, type);
                },
                new ThreadNetworkWrapper()
        );
    }
}
