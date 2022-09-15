package ellemes.expandedstorage.fabric.mixin;

import ellemes.expandedstorage.common.block.ChestBlock;
import ellemes.expandedstorage.common.block.MiniChestBlock;
import org.spongepowered.asm.mixin.Mixin;
import virtuoel.towelette.api.Fluidloggable;

@SuppressWarnings("unused")
@Mixin({ChestBlock.class, MiniChestBlock.class})
public abstract class ToweletteCompat implements Fluidloggable {
}
