package ellemes.expandedstorage.common.mixin;

import com.mojang.datafixers.DataFixerBuilder;
import ellemes.expandedstorage.common.fixer.DataFixerUtils;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataFixers.class)
public abstract class DataFixerEntrypoint {
    @Inject(
            method = "addFixers(Lcom/mojang/datafixers/DataFixerBuilder;)V",
            at = @At(value = "TAIL", remap = false)
    )
    private static void expandedstorage_registerFixers(DataFixerBuilder builder, CallbackInfo ci) {
        DataFixerUtils.register1_17DataFixer(builder, 2707, 1);
        DataFixerUtils.register1_18DataFixer(builder, 2852, 1);
    }
}
