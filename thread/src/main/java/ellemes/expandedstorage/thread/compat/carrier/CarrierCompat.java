package ellemes.expandedstorage.thread.compat.carrier;

import ellemes.expandedstorage.common.block.ChestBlock;
import ellemes.expandedstorage.common.block.OpenableBlock;
import me.steven.carrier.api.CarriableRegistry;

public final class CarrierCompat {
    public static void registerChestBlock(ChestBlock block) {
        CarriableRegistry.INSTANCE.register(block.getBlockId(), new CarriableChest(block.getBlockId(), block));
    }

    public static void registerOpenableBlock(OpenableBlock block) {
        CarriableRegistry.INSTANCE.register(block.getBlockId(), new CarriableBlock(block.getBlockId(), block));
    }
}
