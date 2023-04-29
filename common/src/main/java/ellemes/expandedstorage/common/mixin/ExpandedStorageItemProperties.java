package ellemes.expandedstorage.common.mixin;

import ellemes.expandedstorage.common.block.MiniStorageBlock;
import ellemes.expandedstorage.common.registration.ModItems;
import ellemes.expandedstorage.common.item.MutationMode;
import ellemes.expandedstorage.common.item.StorageMutator;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemProperties.class)
public abstract class ExpandedStorageItemProperties {
    @Shadow
    private static ClampedItemPropertyFunction registerGeneric(ResourceLocation name, ClampedItemPropertyFunction property) {
        throw new IllegalStateException("Untransformed Shadow.");
    }

    @Shadow
    private static void register(Item item, ResourceLocation name, ClampedItemPropertyFunction property) {
        throw new IllegalStateException("Untransformed Shadow.");
    }

    @Inject(
            method = "<clinit>",
            at = @At("TAIL")
    )
    private static void expandedstorage$addItemProperties(CallbackInfo ci) {
        registerGeneric(Utils.id("sparrow"), (stack, level, entity, i) -> MiniStorageBlock.hasSparrowProperty(stack) ? 1.0f : 0.0f);
        register(ModItems.STORAGE_MUTATOR, Utils.id("tool_mode"), (stack, level, entity, i) -> {
            MutationMode mode = StorageMutator.getMode(stack);
            boolean isSparrow = stack.hasCustomHoverName() && stack.getHoverName().getString().equalsIgnoreCase("sparrow");
            if (mode == MutationMode.SWAP_THEME) {
                if (isSparrow) {
                    return 1.0F;
                }
                return 0.8F;
            } else if (mode == MutationMode.ROTATE) {
                return 0.6F;
            } else if (mode == MutationMode.SPLIT) {
                return 0.4F;
            } else if (mode == MutationMode.MERGE) {
                return 0.2F;
            }
            return 0.0F;
        });
    }
}
