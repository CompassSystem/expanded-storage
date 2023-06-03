package compasses.expandedstorage.forge.mixin;

import ellemes.expandedstorage.api.client.gui.AbstractScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Screen.class)
public abstract class QuarkRepositionButtonsMixin {
    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("TAIL"))
    private void afterInit(CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object) this instanceof AbstractScreen screen) {
            for (GuiEventListener child : screen.children()) {
                if (child instanceof Button button) {
                    if (button.getClass().getName().startsWith("vazkii.quark") && button.getY() == screen.getGuiTop() + 5) {
                        button.setX(button.getX() + ((screen.getInventoryWidth() - 9) * 18));
                    }
                }
            }
        }
    }
}