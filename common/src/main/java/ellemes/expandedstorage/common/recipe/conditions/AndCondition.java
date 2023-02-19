package ellemes.expandedstorage.common.recipe.conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public class AndCondition implements RecipeCondition {
    private static final ResourceLocation NETWORK_ID = Utils.id("and");
    private final Collection<RecipeCondition> conditions;

    public AndCondition(Collection<RecipeCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean test(Object subject) {
        for (RecipeCondition condition : conditions) {
            if (!condition.test(subject)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ResourceLocation getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeCollection(conditions, (b, condition) -> {
            buffer.writeResourceLocation(condition.getNetworkId());
            condition.writeToBuffer(b);
        });
    }

    @Override
    public JsonElement toJson() {
        JsonArray jsonConditions = new JsonArray();
        for (RecipeCondition condition : conditions) {
            jsonConditions.add(condition.toJson());
        }
        return jsonConditions;
    }
}
