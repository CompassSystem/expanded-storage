/*
 * Copyright 2021 NinjaPhenix
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ninjaphenix.expandedstorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ninjaphenix.expandedstorage.Common;
import org.jetbrains.annotations.Nullable;

public final class BarrelBlock extends OpenableBlock {
    public BarrelBlock(Settings settings, Identifier blockId, Identifier tierId, Identifier openingStat, int slotCount) {
        super(settings, blockId, tierId, openingStat, slotCount);
        this.setDefaultState(this.getDefaultState().with(Properties.FACING, Direction.UP).with(Properties.OPEN, false));
    }

    @Override
    public Identifier getBlockType() {
        return Common.BARREL_BLOCK_TYPE;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(Properties.FACING, context.getPlayerLookDirection().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.FACING, Properties.OPEN);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Common.createBarrelBlockEntity(pos, state);
    }
}
