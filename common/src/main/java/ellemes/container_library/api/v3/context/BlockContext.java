package ellemes.container_library.api.v3.context;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

/**
 * @apiNote Please use v2 api for now, not yet stable.
 */
@ApiStatus.Experimental
public final class BlockContext extends BaseContext {
    private final BlockPos pos;

    public BlockContext(ServerLevel world, ServerPlayer player, BlockPos pos) {
        super(world, player);
        this.pos = pos;
    }

    public BlockPos getBlockPos() {
        return pos;
    }
}
