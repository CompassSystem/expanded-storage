package compass_system.expanded_storage.barrel.block

import compass_system.expanded_storage._core.block.StorageBlockEntity
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class BarrelBlockEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState
) : StorageBlockEntity(type, pos, state), RenderDataBlockEntity {

}