package compasses.expandedstorage.quilt;

import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.block.misc.CopperBlockHelper;
import compasses.expandedstorage.common.registration.Content;
import compasses.expandedstorage.common.registration.ContentConsumer;
import compasses.expandedstorage.thread.ThreadCommonHelper;
import compasses.expandedstorage.thread.ThreadMain;
import net.fabricmc.api.EnvType;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.Version;
import org.quiltmc.loader.api.VersionFormatException;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.quiltmc.qsl.block.content.registry.api.ReversibleBlockEntry;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;

public final class QuiltMain implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        boolean isCarrierCompatEnabled = QuiltLoader.getModContainer("carrier").map(it -> {
            try {
                Version.Semantic version = Version.Semantic.of(it.metadata().version().raw());
                return version.compareTo(Version.Semantic.of(new int[]{1, 8, 0}, "", "")) > 0;
            } catch (VersionFormatException e) {
                System.err.println("Carrier compat broke, cannot parse mod version.");
                return false;
            }
        }).orElse(false);

        boolean isClient = MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT;
        ThreadMain.constructContent(new QuiltCommonHelper(), QuiltLoader.isModLoaded("htm"), isClient,
                ((ContentConsumer) ThreadMain::registerContent)
                        .andThenIf(isCarrierCompatEnabled, ThreadMain::registerCarrierCompat)
                        .andThen(this::registerWaxedContent), "Quilt", "Quilt"
        );

        ServerLifecycleEvents.STOPPED.register(server -> {
            ((ThreadCommonHelper) CommonMain.platformHelper()).setServerInstance(null);
        });
    }

    private void registerWaxedContent(Content content) {
        CopperBlockHelper.dewaxing().forEach((waxed, dewaxed) -> {
            BlockContentRegistries.WAXABLE.put(dewaxed, new ReversibleBlockEntry(waxed, true));
        });
        CopperBlockHelper.oxidisation().forEach((before, next) -> {
            BlockContentRegistries.OXIDIZABLE.put(before, new ReversibleBlockEntry(next, true));
        });
    }
}
