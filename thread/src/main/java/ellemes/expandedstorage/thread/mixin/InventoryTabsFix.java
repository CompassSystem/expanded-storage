package ellemes.expandedstorage.thread.mixin;

import com.kqp.inventorytabs.api.TabProviderRegistry;
import ellemes.expandedstorage.common.registration.ModBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TabProviderRegistry.class)
public class InventoryTabsFix {

    @Inject(
            method= "init(Ljava/lang/String;)V",
            at = @At("TAIL"),
            remap = false
    )
    private static void expandedstorage$afterInit(String configMsg, CallbackInfo ci) {
        ModBlocks.all().forEach(TabProviderRegistry::removeSimpleBlock);
    }
}
