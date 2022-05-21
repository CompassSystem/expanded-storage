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
package ellemes.expandedstorage.client;

import ellemes.expandedstorage.api.EsChestType;
import net.minecraft.resources.ResourceLocation;

public final class TextureCollection {
    private final ResourceLocation single;
    private final ResourceLocation left;
    private final ResourceLocation right;
    private final ResourceLocation top;
    private final ResourceLocation bottom;
    private final ResourceLocation front;
    private final ResourceLocation back;

    public TextureCollection(ResourceLocation single, ResourceLocation left, ResourceLocation right,
                             ResourceLocation top, ResourceLocation bottom, ResourceLocation front, ResourceLocation back) {
        this.single = single;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.front = front;
        this.back = back;
    }

    public ResourceLocation getTexture(EsChestType type) {
        if (type == EsChestType.TOP) {
            return top;
        } else if (type == EsChestType.BOTTOM) {
            return bottom;
        } else if (type == EsChestType.FRONT) {
            return front;
        } else if (type == EsChestType.BACK) {
            return back;
        } else if (type == EsChestType.LEFT) {
            return left;
        } else if (type == EsChestType.RIGHT) {
            return right;
        } else if (type == EsChestType.SINGLE) {
            return single;
        }
        throw new IllegalArgumentException("TextureCollection#getTexture received an unknown EsChestType.");
    }
}
