package ellemes.expandedstorage.common.recipe.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Set;
import java.util.stream.Collectors;

public class IsInTagCondition implements RecipeCondition {
    private static final ResourceLocation NETWORK_ID = Utils.id("in_tag");
    private final TagKey<?> tagKey;
    private Set<Object> values;

    public IsInTagCondition(TagKey<?> tagKey) {
        this.tagKey = tagKey;
    }

    @Override
    public boolean isExactMatch() {
        return false;
    }

    @Override
    public boolean test(Object subject) {
        if (values == null) {
            values = ((HolderSet.Named<Object>) Registry.REGISTRY.get(tagKey.registry().location()).getTag((TagKey) tagKey).orElseThrow()).stream().map(Holder::value).collect(Collectors.toUnmodifiableSet());
        }
        return values.contains(RecipeCondition.unwrap(subject));
    }

    @Override
    public ResourceLocation getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(tagKey.registry().location());
        buffer.writeResourceLocation(tagKey.location());
    }

    private static IsInTagCondition readFromBuffer(FriendlyByteBuf buffer) {
        ResourceLocation registryId = buffer.readResourceLocation();
        ResourceLocation tag = buffer.readResourceLocation();
        Registry<?> registry = Registry.REGISTRY.get(registryId);
        if (registry == null) {
            throw new NullPointerException("Unknown registry: " + registryId);
        }
        return new IsInTagCondition(TagKey.create(registry.key(), tag));
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("tag", tagKey.location().toString());
        return json;
    }

    static {
        RecipeCondition.RECIPE_DESERIALIZERS.put(NETWORK_ID, IsInTagCondition::readFromBuffer);
    }
}
