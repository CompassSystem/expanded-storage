package ellemes.container_library.thread;

import ellemes.container_library.CommonMain;
import ellemes.container_library.api.inventory.AbstractHandler;
import ellemes.container_library.wrappers.NetworkWrapper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ThreadMain {
    public static void initialize(
            Function<String, Boolean> isModLoaded,
            BiFunction<Boolean, Boolean, NetworkWrapper> networkWrapperFunction
    ) {
        CommonMain.initialize(
                (handlerType, factory) -> {
                    MenuType<AbstractHandler> type = new ExtendedScreenHandlerType<>(factory::create);
                    return Registry.register(BuiltInRegistries.MENU, handlerType, type);
                },
                networkWrapperFunction.apply(isModLoaded.apply("flan"), isModLoaded.apply("ftbchunks"))
        );
    }
}
