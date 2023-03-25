package ellemes.expandedstorage.common.mixin;

import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemProperties.class)
public abstract class MiniStorageItemProperty {
    @Shadow
    private static ClampedItemPropertyFunction registerGeneric(ResourceLocation name, ClampedItemPropertyFunction property) {
        throw new IllegalStateException("Untransformed Shadow.");
    }

    @Inject(
            method = "<clinit>",
            at = @At("TAIL")
    )
    private static void expandedstorage$addItemProperties(CallbackInfo ci) {
        registerGeneric(Utils.id("sparrow"), (stack, level, entity, i) -> {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                Tag sparrowTag = tag.get("sparrow");
                if (sparrowTag != null && sparrowTag.getId() == Tag.TAG_BYTE) {
                    return ((NumericTag) sparrowTag).getAsByte() != 0 ? 1.0f : 0.0f;
                }
            }
            return 0.0f;
        });
    }
}
