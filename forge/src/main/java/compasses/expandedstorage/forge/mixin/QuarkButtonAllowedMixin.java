package compasses.expandedstorage.forge.mixin;

import compasses.expandedstorage.common.client.gui.PageScreen;
import compasses.expandedstorage.common.client.gui.ScrollScreen;
import compasses.expandedstorage.common.client.gui.SingleScreen;
import org.spongepowered.asm.mixin.Mixin;
import vazkii.quark.api.IQuarkButtonAllowed;

@Mixin({PageScreen.class, SingleScreen.class, ScrollScreen.class})
public class QuarkButtonAllowedMixin implements IQuarkButtonAllowed {

}
