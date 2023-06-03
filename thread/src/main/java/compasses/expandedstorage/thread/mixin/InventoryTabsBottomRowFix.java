package compasses.expandedstorage.thread.mixin;

import com.kqp.inventorytabs.tabs.render.TabRenderingHints;
import ellemes.expandedstorage.api.client.gui.AbstractScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractScreen.class)
public class InventoryTabsBottomRowFix implements TabRenderingHints {
    @Override
    public int getBottomRowXOffset() {
        AbstractScreen self = (AbstractScreen) (Object) this;
        int inventoryWidth = self.getInventoryWidth();
        if (inventoryWidth <= 9) {
            return 0;
        } else {
            return (inventoryWidth - 9) * 9;
        }
    }
}
