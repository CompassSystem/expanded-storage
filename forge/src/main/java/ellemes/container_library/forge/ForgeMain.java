package ellemes.container_library.forge;

import ellemes.container_library.CommonMain;
import ellemes.container_library.api.inventory.AbstractHandler;
import ellemes.container_library.forge.wrappers.ForgeNetworkWrapper;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;

public final class ForgeMain {
    public ForgeMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CommonMain.initialize((handlerType, factory) -> {
            MenuType<AbstractHandler> menuType = new MenuType<>((IContainerFactory<AbstractHandler>) factory::create);
            menuType.setRegistryName(handlerType);

            modEventBus.addGenericListener(MenuType.class, (RegistryEvent.Register<MenuType<?>> event) -> {
                IForgeRegistry<MenuType<?>> registry = event.getRegistry();
                registry.registerAll(menuType);
            });

            return menuType;
            }, new ForgeNetworkWrapper(ModList.get().isLoaded("ftbchunks")));

        if (FMLLoader.getDist() == Dist.CLIENT) {
            ForgeClient.initialize();
        }
    }
}
