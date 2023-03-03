package ellemes.expandedstorage.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class MossChestBlock extends ChestBlock implements BonemealableBlock {
    public MossChestBlock(Properties settings, ResourceLocation openingStat, int slotCount) {
        super(settings, openingStat, slotCount);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter blockLevel, BlockPos pos, BlockState state, boolean bl) {
        return blockLevel.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, Random random, BlockPos pos, BlockState state) {
        CaveFeatures.MOSS_PATCH_BONEMEAL.value().place(level, level.getChunkSource().getGenerator(), random, pos.above());
    }
}
