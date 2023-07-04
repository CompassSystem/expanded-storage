package compasses.expandedstorage.impl.mixin;

import compasses.expandedstorage.impl.client.gui.PageScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Screen.class, priority = 1100)
public abstract class LastAfterInitCallbackMixin {
    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("TAIL"))
    private void expandedstorage$afterScreenInitialized(CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object) this instanceof PageScreen screen) {
            screen.addPageButtons();
        }
    }

    @Inject(method = "resize(Lnet/minecraft/client/Minecraft;II)V", at = @At("TAIL"))
    private void expandedstorage$afterScreenResized(CallbackInfo ci) {
        //noinspection ConstantValue
        if ((Object) this instanceof PageScreen screen) {
            screen.addPageButtons();
        }
    }
}
