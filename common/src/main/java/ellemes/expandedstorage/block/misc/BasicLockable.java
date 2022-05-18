/*
 * Copyright 2021-2022 Ellemes
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
package ellemes.expandedstorage.block.misc;

import ellemes.expandedstorage.block.strategies.Lockable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;

public class BasicLockable implements Lockable {
    LockCode lock = LockCode.NO_LOCK;

    @Override
    public void writeLock(CompoundTag tag) {
        lock.addToTag(tag);
    }

    @Override
    public void readLock(CompoundTag tag) {
        lock = LockCode.fromTag(tag);
    }

    @Override
    public boolean canPlayerOpenLock(ServerPlayer player) {
        return lock == LockCode.NO_LOCK || !player.isSpectator() && lock.unlocksWith(player.getMainHandItem());
    }
}
