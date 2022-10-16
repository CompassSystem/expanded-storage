package ellemes.container_library.thread;

import ellemes.container_library.CommonMain;
import ellemes.container_library.wrappers.NetworkWrapper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import ellemes.container_library.api.inventory.AbstractHandler;

import java.util.function.Function;

public class ThreadMain {
    public static void initialize(
            Function<String, Boolean> isModLoaded,
            Function<Boolean, NetworkWrapper> networkWrapperFunction
    ) {
        CommonMain.initialize(
                (handlerType, factory) -> {
                    MenuType<AbstractHandler> type = new ExtendedScreenHandlerType<>(factory::create);
                    return Registry.register(Registry.MENU, handlerType, type);
                },
                networkWrapperFunction.apply(isModLoaded.apply("flan"))
        );
    }
}
