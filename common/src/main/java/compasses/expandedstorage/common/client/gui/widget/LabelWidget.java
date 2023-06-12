package compasses.expandedstorage.common.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class LabelWidget extends AbstractWidget {
    public LabelWidget(int x, int y, Component label, Component hint) {
        super(x, y, Minecraft.getInstance().font.width(label.getString()), 20, label);
        if (hint != null) {
            setTooltip(Tooltip.create(hint));
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.drawString(Minecraft.getInstance().font, getMessage(), getX(), getY(), 0xFF_FFFFFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (getTooltip() == null) {
            return null;
        } else {
            return !(isFocused() || isHovered()) ? ComponentPath.leaf(this) : null;
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}
