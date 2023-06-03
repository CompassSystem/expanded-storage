package compasses.expandedstorage.common.client;

import ellemes.expandedstorage.api.client.function.ScreenSizePredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

// todo: make record
@SuppressWarnings("ClassCanBeRecord")
public final class PickButton {
    private final ResourceLocation texture;
    private final Component title;
    private final ScreenSizePredicate warningTest;
    private final List<Component> warningText;

    public PickButton(ResourceLocation texture, Component title, ScreenSizePredicate warningTest, List<Component> warningText) {
        this.texture = texture;
        this.title = title;
        this.warningTest = warningTest;
        this.warningText = warningText;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public Component getTitle() {
        return title;
    }

    public ScreenSizePredicate getWarningTest() {
        return warningTest;
    }

    public List<Component> getWarningText() {
        return warningText;
    }
}
