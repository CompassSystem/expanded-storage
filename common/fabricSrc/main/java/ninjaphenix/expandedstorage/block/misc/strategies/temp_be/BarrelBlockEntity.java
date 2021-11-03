/**
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
package ninjaphenix.expandedstorage.block.misc.strategies.temp_be;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import ninjaphenix.container_library.api.inventory.AbstractHandler;
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.misc.strategies.ItemAccess;
import ninjaphenix.expandedstorage.block.misc.strategies.Lockable;
import ninjaphenix.expandedstorage.block.misc.strategies.Nameable;
import ninjaphenix.expandedstorage.block.misc.strategies.Observable;
import ninjaphenix.expandedstorage.block.misc.strategies.temp_be.extendable.ExposedInventoryBlockEntity;

public class BarrelBlockEntity extends ExposedInventoryBlockEntity {
    private final DefaultedList<ItemStack> inventory;
    private final ViewerCountManager manager = new ViewerCountManager() {
        @Override
        protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
            BarrelBlockEntity.playSound(world, state, pos, SoundEvents.BLOCK_BARREL_OPEN);
            BarrelBlockEntity.updateBlockState(world, state, pos, true);
        }

        @Override
        protected void onContainerClose(World world, BlockPos pos, BlockState state) {
            BarrelBlockEntity.playSound(world, state, pos, SoundEvents.BLOCK_BARREL_CLOSE);
            BarrelBlockEntity.updateBlockState(world, state, pos, false);
        }

        @Override
        protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldCount, int newCount) {

        }

        @Override
        protected boolean isPlayerViewing(PlayerEntity player) {
            return BarrelBlockEntity.this.getObservable().isViewedBy(player);
        }
    };

    public BarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Identifier blockId,
                             ItemAccess access, Lockable lockable) {
        super(type, pos, state, blockId);
        this.setItemAccess(access);
        this.setLock(lockable);
        this.setName(new Nameable.Mutable(((BarrelBlock) state.getBlock()).getInventoryTitle()));
        this.setObservable(new Observable() {
            @Override
            public void playerStartViewing(PlayerEntity player) {
                BlockEntity self = BarrelBlockEntity.this;
                manager.openContainer(player, self.getWorld(), self.getPos(), self.getCachedState());
            }

            @Override
            public void playerStopViewing(PlayerEntity player) {
                BlockEntity self = BarrelBlockEntity.this;
                manager.closeContainer(player, self.getWorld(), self.getPos(), self.getCachedState());
            }

            @Override
            public boolean isViewedBy(PlayerEntity player) { // no need for this?
                return player.currentScreenHandler instanceof AbstractHandler handler && handler.getInventory() == BarrelBlockEntity.this;
            }
        });
        inventory = DefaultedList.ofSize(((BarrelBlock) state.getBlock()).getSlotCount(), ItemStack.EMPTY);
    }

    private static void playSound(World world, BlockState state, BlockPos pos, SoundEvent sound) {
        Vec3i facingVector = state.get(Properties.FACING).getVector();
        double X = pos.getX() + 0.5D + facingVector.getX() / 2.0D;
        double Y = pos.getY() + 0.5D + facingVector.getY() / 2.0D;
        double Z = pos.getZ() + 0.5D + facingVector.getZ() / 2.0D;
        world.playSound(null, X, Y, Z, sound, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
    }

    private static void updateBlockState(World world, BlockState state, BlockPos pos, boolean open) {
        world.setBlockState(pos, state.with(Properties.OPEN, open), Block.NOTIFY_ALL);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (stack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = Inventories.splitStack(inventory, slot, amount);
        if (!stack.isEmpty()) this.markDirty();
        return stack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (stack.getCount() > this.getMaxCountPerStack()) stack.setCount(this.getMaxCountPerStack());
        inventory.set(slot, stack);
        this.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        Inventories.readNbt(tag, inventory);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        Inventories.writeNbt(tag, inventory);
        return tag;
    }

    @Override
    public void onOpen(PlayerEntity player) {
        if (player.isSpectator()) return;
        this.getObservable().playerStartViewing(player);
    }

    @Override
    public void onClose(PlayerEntity player) {
        if (player.isSpectator()) return;
        this.getObservable().playerStopViewing(player);
    }

    public final void updateViewerCount(ServerWorld world, BlockPos pos, BlockState state) {
        manager.updateViewerCount(world, pos, state);
    }
}