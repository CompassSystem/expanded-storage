package ellemes.expandedstorage.thread.mixin;

import ellemes.expandedstorage.common.block.ChestBlock;
import ellemes.expandedstorage.common.block.MiniStorageBlock;
import org.spongepowered.asm.mixin.Mixin;
import virtuoel.towelette.api.Fluidloggable;

@SuppressWarnings("unused")
@Mixin({ChestBlock.class, MiniStorageBlock.class})
public abstract class ToweletteCompat implements Fluidloggable {
}
