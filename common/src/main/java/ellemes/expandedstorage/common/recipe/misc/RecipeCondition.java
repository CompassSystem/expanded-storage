package ellemes.expandedstorage.common.recipe.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ellemes.expandedstorage.common.misc.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This uhhhhh system is uhhhhhhhhh very uhhhhhhhhh yes...
 * Is it over-engineer, probably not going to be used and eventually be removed? Yes!
 * <p>
 * todo: recipe condition for checking if input has specific properties
 * todo: conditions check against wrong thing. e.g. input is entity, compares entity_type,
 *  input is blockstate, compares block
 */
public interface RecipeCondition<T> {
    Map<ResourceLocation, Function<FriendlyByteBuf, RecipeCondition<?>>> RECIPE_DESERIALIZERS = new HashMap<>();
    RecipeCondition<BlockState> IS_WOODEN_CHEST = new IsInstanceOfCondition<>(ChestBlock.class);
    RecipeCondition<BlockState> IS_WOODEN_BARREL = new IsInstanceOfCondition<>(BarrelBlock.class);

    private static <T> RecipeCondition<?> tryReadGenericCondition(JsonElement condition, Registry<T> registry) {
        if (condition.isJsonObject()) {
            JsonObject object = condition.getAsJsonObject();
            if (object.has("tag")) {
                TagKey<T> tag = TagKey.create(registry.key(), JsonHelper.getJsonResourceLocation(object, "tag"));
                return new IsInTagCondition<>(tag);
            } else if (object.has("id")) {
                return new IsRegistryObject<>(registry, JsonHelper.getJsonResourceLocation(object, "id"));
            }
        } else if (condition.isJsonArray()) {
            JsonArray conditions = condition.getAsJsonArray();
            RecipeCondition[] recipeConditions = new RecipeCondition[conditions.size()];
            Function<JsonElement, RecipeCondition<?>> function = registry == Registry.BLOCK ? RecipeCondition::readBlockCondition : RecipeCondition::readEntityCondition;
            for (int i = 0; i < conditions.size(); i++) {
                recipeConditions[i] = function.apply(conditions.get(i));
            }
            return new AndCondition<>(Arrays.asList(recipeConditions));
        } else {
            throw new JsonSyntaxException("condition must be an Object or an Array.");
        }
        return null;
    }

    static RecipeCondition<?> readBlockCondition(JsonElement condition) {
        RecipeCondition<?> generic = tryReadGenericCondition(condition, Registry.BLOCK);
        if (generic != null) {
            return generic;
        }
        if (condition.isJsonObject()) {
            JsonObject recipeCondition = condition.getAsJsonObject();
            if (recipeCondition.has("condition")) {
                ResourceLocation conditionId = JsonHelper.getJsonResourceLocation(recipeCondition, "condition");
                if (conditionId.toString().equals("expandedstorage:is_wooden_chest")) {
                    return RecipeCondition.IS_WOODEN_CHEST;
                } else if (conditionId.toString().equals("expandedstorage:is_wooden_barrel")) {
                    return RecipeCondition.IS_WOODEN_BARREL;
                } else {
                    throw new IllegalArgumentException("condition with id " + conditionId + " doesn't exist.");
                }
            }
        }
        throw new JsonSyntaxException("Unknown recipe condition");
    }

    static RecipeCondition<?> readEntityCondition(JsonElement condition) {
        RecipeCondition<?> generic = tryReadGenericCondition(condition, Registry.ENTITY_TYPE);
        if (generic != null) {
            return generic;
        }
        throw new JsonSyntaxException("Unknown recipe condition");
    }

    boolean test(T subject);

    ResourceLocation getNetworkId();

    void writeToBuffer(FriendlyByteBuf buffer);

    static <T> RecipeCondition<T> readFromBuffer(FriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        return (RecipeCondition<T>) RECIPE_DESERIALIZERS.get(id).apply(buffer);
    }
}

class IsInTagCondition<T> implements RecipeCondition<T> {
    private static final ResourceLocation NETWORK_ID = Utils.id("in_tag");
    private final TagKey<T> tagKey;
    private Set<T> values;

    public IsInTagCondition(TagKey<T> tagKey) {
        this.tagKey = tagKey;
    }

    @Override
    public boolean test(T subject) {
        if (values == null) {
            values = ((Registry<T>) Registry.REGISTRY.get(tagKey.registry().location())).getTag(tagKey).orElseThrow().stream().map(Holder::value).collect(Collectors.toUnmodifiableSet());
        }
        return values.contains(subject);
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

    private static <T> IsInTagCondition<T> readFromBuffer(FriendlyByteBuf buffer) {
        ResourceLocation registryId = buffer.readResourceLocation();
        ResourceLocation tag = buffer.readResourceLocation();
        Registry<T> registry = (Registry<T>) Registry.REGISTRY.get(registryId);
        if (registry == null) {
            throw new NullPointerException("Unknown registry: " + registryId);
        }
        return new IsInTagCondition<>(TagKey.create(registry.key(), tag));
    }

    static {
        RecipeCondition.RECIPE_DESERIALIZERS.put(NETWORK_ID, IsInTagCondition::readFromBuffer);
    }
}

class IsInstanceOfCondition<T> implements RecipeCondition<T> {
    private static final ResourceLocation NETWORK_ID = Utils.id("is_instance");
    private final Class<?> clazz;

    public IsInstanceOfCondition(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean test(T subject) {
        return clazz.isAssignableFrom(subject.getClass());
    }

    @Override
    public ResourceLocation getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(clazz.getName());
    }

    private static <T> IsInstanceOfCondition<T> readFromBuffer(FriendlyByteBuf buffer) {
        String className = buffer.readUtf();
        try {
            Class<T> clazz = (Class<T>) Class.forName(className);
            return new IsInstanceOfCondition<>(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static {
        RecipeCondition.RECIPE_DESERIALIZERS.put(NETWORK_ID, IsInstanceOfCondition::readFromBuffer);
    }
}

class IsRegistryObject<T> implements RecipeCondition<T> {
    private static final ResourceLocation NETWORK_ID = Utils.id("is_registry_object");
    private final T value;
    private final ResourceLocation registry;
    private final ResourceLocation objectId;

    public IsRegistryObject(Registry<T> registry, ResourceLocation id) {
        this.value = registry.get(id);
        this.registry = registry.key().location();
        this.objectId = id;
    }

    @Override
    public boolean test(T subject) {
        return subject == value;
    }

    @Override
    public ResourceLocation getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(registry);
        buffer.writeResourceLocation(objectId);
    }

    private static <T> IsRegistryObject<T> readFromBuffer(FriendlyByteBuf buffer) {
        ResourceLocation registryId = buffer.readResourceLocation();
        ResourceLocation objectId = buffer.readResourceLocation();
        Registry<T> registry = (Registry<T>) Registry.REGISTRY.get(registryId);
        if (registry == null) {
            throw new NullPointerException("Unknown registry: " + registryId);
        }
        return new IsRegistryObject<>(registry, objectId);
    }

    static {
        RecipeCondition.RECIPE_DESERIALIZERS.put(NETWORK_ID, IsRegistryObject::readFromBuffer);
    }
}

class BlockToBlockStateCondition implements RecipeCondition<BlockState> {
    private static final ResourceLocation NETWORK_ID = Utils.id("block_condition_wrapper");
    private final RecipeCondition<Block> baseCondition;

    public BlockToBlockStateCondition(RecipeCondition<Block> base) {
        this.baseCondition = base;
    }

    @Override
    public boolean test(BlockState subject) {
        return baseCondition.test(subject.getBlock());
    }

    @Override
    public ResourceLocation getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(baseCondition.getNetworkId());
        baseCondition.writeToBuffer(buffer);
    }

    private static BlockToBlockStateCondition readFromBuffer(FriendlyByteBuf buffer) {
        RecipeCondition<Block> baseCondition = RecipeCondition.readFromBuffer(buffer);
        return new BlockToBlockStateCondition(baseCondition);
    }

    static {
        RecipeCondition.RECIPE_DESERIALIZERS.put(NETWORK_ID, BlockToBlockStateCondition::readFromBuffer);
    }
}

class AndCondition<T> implements RecipeCondition<T> {
    private static final ResourceLocation NETWORK_ID = Utils.id("and");
    private final Collection<RecipeCondition<T>> conditions;

    public AndCondition(Collection<RecipeCondition<T>> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean test(T subject) {
        for (RecipeCondition<T> condition : conditions) {
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
}
