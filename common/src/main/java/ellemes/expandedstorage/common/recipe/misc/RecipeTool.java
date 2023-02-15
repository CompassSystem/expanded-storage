package ellemes.expandedstorage.common.recipe.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ellemes.expandedstorage.common.item.StorageConversionKit;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract sealed class RecipeTool permits RecipeTool.UpgradeTool, RecipeTool.MutatorTool {
    private final ResourceLocation toolId;

    private RecipeTool(ResourceLocation toolId) {
        this.toolId = toolId;
    }

    public boolean isMatchFor(ItemStack tool) {
        return tool.getItem().builtInRegistryHolder().is(toolId);
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(toolId);
    }

    public static final class UpgradeTool extends RecipeTool {
        private UpgradeTool(ResourceLocation toolId) {
            super(toolId);
        }
    }

    public static final class MutatorTool extends RecipeTool {
        private final String requiredName;

        private MutatorTool(String requiredName) {
            super(Utils.id("storage_mutator"));
            this.requiredName = requiredName;
        }

        @Override
        public boolean isMatchFor(ItemStack tool) {
            boolean isNameMatch = true;
            if (requiredName != null) {
                isNameMatch = requiredName.equals(tool.getHoverName().getString());
            }
            return isNameMatch && super.isMatchFor(tool);
        }

        @Override
        public void writeToBuffer(FriendlyByteBuf buffer) {
            super.writeToBuffer(buffer);
            buffer.writeNullable(requiredName, FriendlyByteBuf::writeUtf);
        }
    }

    public static RecipeTool fromJsonObject(JsonObject object) {
        ResourceLocation toolId = JsonHelper.getJsonResourceLocation(object, "id");
        if (toolId.toString().equals("expandedstorage:storage_mutator")) {
            if (object.has("name")) {
                JsonElement name = object.get("name");
                if (name.isJsonPrimitive() && name.getAsJsonPrimitive().isString()) {
                    return new MutatorTool(name.getAsString());
                } else {
                    throw new JsonSyntaxException("Tool's name entry must be a string");
                }
            } else {
                return new MutatorTool(null);
            }
        } else if (Registry.ITEM.get(toolId) instanceof StorageConversionKit) {
            return new UpgradeTool(toolId);
        } else {
            throw new IllegalArgumentException("Tool id supplied is not a conversion kit or the storage mutator.");
        }
    }

    public static RecipeTool fromNetworkBuffer(FriendlyByteBuf buffer) {
        ResourceLocation toolId = buffer.readResourceLocation();
        if (toolId.toString().equals("expandedstorage:storage_mutator")) {
            String name = buffer.readNullable(FriendlyByteBuf::readUtf);
            return new MutatorTool(name);
        } else if (Registry.ITEM.get(toolId) instanceof StorageConversionKit) {
            return new UpgradeTool(toolId);
        } else {
            throw new IllegalArgumentException("Invalid tool id sent by the server.");
        }
    }
}
