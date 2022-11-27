package ellemes.container_library.forge;

import ellemes.container_library.CommonMain;
import ellemes.container_library.api.inventory.AbstractHandler;
import ellemes.container_library.forge.wrappers.ForgeNetworkWrapper;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegisterEvent;

public final class ForgeMain {
    public ForgeMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CommonMain.initialize((handlerType, factory) -> {
            MenuType<AbstractHandler> menuType = new MenuType<>((IContainerFactory<AbstractHandler>) factory::create);

            modEventBus.addListener((RegisterEvent event) -> {
                event.register(Registry.MENU_REGISTRY, helper -> {
                    helper.register(handlerType, menuType);
                });
            });

            return menuType;
            }, new ForgeNetworkWrapper());

        if (FMLLoader.getDist() == Dist.CLIENT) {
            ForgeClient.initialize();
        }
    }
}