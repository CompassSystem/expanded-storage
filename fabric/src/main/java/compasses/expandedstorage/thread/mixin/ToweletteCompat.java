package compasses.expandedstorage.thread.mixin;

import compasses.expandedstorage.common.block.ChestBlock;
import compasses.expandedstorage.common.block.MiniStorageBlock;
import org.spongepowered.asm.mixin.Mixin;
import virtuoel.towelette.api.Fluidloggable;

@SuppressWarnings("unused")
@Mixin({ChestBlock.class, MiniStorageBlock.class})
public abstract class ToweletteCompat implements Fluidloggable {

}
