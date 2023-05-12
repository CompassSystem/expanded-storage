package compasses.expandedstorage.thread.client;

import compasses.expandedstorage.common.client.gui.AbstractScreen;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.renderer.Rect2i;

public final class EmiCompat implements EmiPlugin {
    private static Bounds asEmiRect(Rect2i rect) {
        return new Bounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    @Override
    public void register(EmiRegistry emiRegistry) {
        emiRegistry.addGenericExclusionArea((screen, consumer) -> {
            if (screen instanceof AbstractScreen aScreen) {
                aScreen.getExclusionZones().stream().map(EmiCompat::asEmiRect).forEach(consumer);
            }
        });
    }
}
