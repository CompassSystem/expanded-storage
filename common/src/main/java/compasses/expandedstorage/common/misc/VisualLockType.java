package compasses.expandedstorage.common.misc;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum VisualLockType implements StringRepresentable {
    NONE,
    GOLD,
    DIAMOND;

    @NotNull
    @Override
    public String getSerializedName() {
        return switch (this) {
            case NONE -> "none";
            case GOLD -> "gold";
            case DIAMOND -> "diamond";
        };
    }
}
