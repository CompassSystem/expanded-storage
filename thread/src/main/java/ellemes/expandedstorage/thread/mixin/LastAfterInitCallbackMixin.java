package ellemes.expandedstorage.thread.mixin;

import ellemes.expandedstorage.common.client.gui.PageScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Screen.class, priority = 1100)
public abstract class LastAfterInitCallbackMixin {
    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("TAIL"))
    private void afterInit(CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object) this instanceof PageScreen screen) {
            screen.addPageButtons();
        }
    }
}
