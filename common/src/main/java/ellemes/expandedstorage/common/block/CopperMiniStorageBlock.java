package ellemes.expandedstorage.common.block;

import ellemes.expandedstorage.common.block.misc.CopperBlockHelper;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.Random;

public final class CopperMiniStorageBlock extends MiniStorageBlock implements WeatheringCopper {
    private final WeatheringCopper.WeatherState weatherState;

    public CopperMiniStorageBlock(Properties settings, ResourceLocation blockId, ResourceLocation openingStat, WeatheringCopper.WeatherState weatherState) {
        super(settings, blockId, Utils.COPPER_TIER_ID, openingStat);
        this.weatherState = weatherState;
    }
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return CopperBlockHelper.getNextOxidisedState(state).isPresent();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        WeatheringCopper.super.onRandomTick(state, level, pos, random);
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
