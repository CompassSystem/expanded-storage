package ellemes.expandedstorage.common.recipe.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class IsInstanceOfCondition implements RecipeCondition {
    private static final ResourceLocation NETWORK_ID = Utils.id("is_instance");
    private final Class<?> clazz;

    public IsInstanceOfCondition(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean isExactMatch() {
        return false;
    }

    @Override
    public boolean test(Object subject) {
        return clazz.isAssignableFrom(RecipeCondition.unwrap(subject).getClass());
    }

    @Override
    public ResourceLocation getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(clazz.getName());
    }

    private static <T> IsInstanceOfCondition readFromBuffer(FriendlyByteBuf buffer) {
        String className = buffer.readUtf();
        try {
            Class<T> clazz = (Class<T>) Class.forName(className);
            return new IsInstanceOfCondition(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public JsonElement toJson(@Nullable JsonObject object) {
        if (object != null) {
            writeToJsonObject(object);
            return null;
        } else {
            JsonObject jsonObject = new JsonObject();
            writeToJsonObject(jsonObject);
            return jsonObject;
        }
    }

    private void writeToJsonObject(JsonObject object) {
        String conditionName = null;
        if (this == RecipeCondition.IS_WOODEN_BARREL) {
            conditionName = "expandedstorage:is_wooden_barrel";
        } else if (this == RecipeCondition.IS_WOODEN_CHEST) {
            conditionName = "expandedstorage:is_wooden_chest";
        }

        if (conditionName != null) {
            object.addProperty("condition", conditionName);
        } else {
            throw new IllegalStateException("Cannot serialize this instance of to json");
        }
    }

    static {
        RecipeCondition.RECIPE_DESERIALIZERS.put(NETWORK_ID, IsInstanceOfCondition::readFromBuffer);
    }
}
