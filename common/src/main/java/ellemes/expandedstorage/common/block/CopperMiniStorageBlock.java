package ellemes.expandedstorage.common.block;

import ellemes.expandedstorage.common.block.misc.CopperBlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class CopperMiniStorageBlock extends MiniStorageBlock implements WeatheringCopper {
    private final WeatheringCopper.WeatherState weatherState;

    public CopperMiniStorageBlock(Properties settings, ResourceLocation blockId, ResourceLocation blockTier, ResourceLocation openingStat, WeatheringCopper.WeatherState weatherState) {
        super(settings, blockId, blockTier, openingStat);
        this.weatherState = weatherState;
    }
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return CopperBlockHelper.getNextOxidisedState(state).isPresent();
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource source) {
        WeatheringCopper.super.onRandomTick(state, level, pos, source);
    }

    @Override
    public Optional<BlockState> getNext(BlockState state) {
        return CopperBlockHelper.getNextOxidisedState(state);
    }

    @Override
    public WeatheringCopper.WeatherState getAge() {
        return weatherState;
    }

}
