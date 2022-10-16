package ellemes.container_library.forge;

import ellemes.container_library.CommonClient;
import ellemes.container_library.CommonMain;
import ellemes.container_library.Utils;
import ellemes.container_library.client.gui.PageScreen;
import ellemes.container_library.client.gui.PickScreen;
import ellemes.container_library.forge.client.ForgeKeyHandler;
import ellemes.container_library.forge.wrappers.ConfigWrapperImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import ninjaphenix.container_library.api.client.gui.AbstractScreen;

import java.nio.file.Path;

public class ForgeClient {
    public static void initialize() {
        Path configDir = FMLPaths.CONFIGDIR.get();

        CommonClient.initialize(ConfigWrapperImpl::new, configDir.resolve(Utils.CONFIG_PATH), configDir.resolve(Utils.FORGE_LEGACY_CONFIG_PATH),
                new ForgeKeyHandler(), ModList.get()::isLoaded);

        ModLoadingContext.get().getActiveContainer().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((client, screen) -> new PickScreen(() -> screen))
        );

        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, (ScreenEvent.InitScreenEvent.Post event) -> {
            if (event.getScreen() instanceof PageScreen screen) {
                screen.addPageButtons();
            }
        });

        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, (InputEvent.ClickInputEvent event) -> {
            if (event.isUseItem() && event.getHand() == InteractionHand.MAIN_HAND) {
                Minecraft client = Minecraft.getInstance();
                Player player = client.player;
                ClientLevel world = client.level;
                if (CommonClient.tryOpenSpectatorInventory(world, player, client.hitResult, event.getHand())) {
                    event.setCanceled(true);
                }
            }
        });

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener((FMLClientSetupEvent event) -> MenuScreens.register(CommonMain.getScreenHandlerType(), AbstractScreen::createScreen));
    }

}
