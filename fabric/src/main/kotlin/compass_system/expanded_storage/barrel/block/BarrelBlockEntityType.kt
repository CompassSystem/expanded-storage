package compass_system.expanded_storage.barrel.block

import com.mojang.datafixers.types.Type
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class BarrelBlockEntityType(type: Type<*>?) : BlockEntityType<BarrelBlockEntity>(null, setOf(), type) {
    override fun isValid(state: BlockState) = state.block is BarrelBlock

    override fun create(pos: BlockPos, state: BlockState) = BarrelBlockEntity(this, pos, state)
}