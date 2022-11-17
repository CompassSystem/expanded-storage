package ellemes.expandedstorage.common.block;

import ellemes.expandedstorage.common.block.misc.CopperBlockHelper;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class CopperBarrelBlock extends BarrelBlock implements WeatheringCopper {
    private final WeatherState weatherState;

    public CopperBarrelBlock(Properties settings, ResourceLocation blockId, ResourceLocation openingStat, int slotCount, WeatheringCopper.WeatherState weatherState) {
        super(settings, blockId, Utils.COPPER_TIER_ID, openingStat, slotCount);
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
    public WeatherState getAge() {
        return weatherState;
    }
}
