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
package ellemes.expandedstorage.quilt.compat.carrier;

import ellemes.expandedstorage.block.ChestBlock;
import me.steven.carrier.api.CarriableRegistry;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;

public final class CarrierCompat {
    public static void registerChestBlock(ChestBlock block) {
        CarriableRegistry.INSTANCE.register(block.getBlockId(), new CarriableChest(block.getBlockId(), block));
    }

    public static void registerOldChestBlock(AbstractChestBlock block) {
        CarriableRegistry.INSTANCE.register(block.getBlockId(), new CarriableOldChest(block.getBlockId(), block));
    }
}
