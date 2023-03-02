package ellemes.expandedstorage.common.recipe.misc;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PartialBlockState<T extends Block> {
    private final T block;
    private final Map<Property<?>, ?> properties;

    public PartialBlockState(T block) {
        this(block, Map.of());
    }

    public PartialBlockState(T block, Map<Property<?>, ?> properties) {
        this.block = block;
        this.properties = properties;
    }

    public static PartialBlockState<?> readFromJson(JsonObject object) {
        ResourceLocation blockId = JsonHelper.getJsonResourceLocation(object, "id");
        if (blockId.toString().equals("minecraft:air")) {
            return null;
        }
        Optional<Block> block = Registry.BLOCK.getOptional(blockId);
        if (block.isEmpty()) {
            throw new IllegalArgumentException("Block id refers to unregistered block");
        }
        Map<String, Property<?>> propertyLookup = block.get().defaultBlockState().getProperties().stream()
                                                       .map(it -> Map.entry(it.getName(), it))
                                                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (object.has("state")) {
            JsonObject properties = object.getAsJsonObject("state");
            Map.Entry[] stateProperties = new Map.Entry[properties.size()];
            int index = 0;

            for (Map.Entry<String, JsonElement> propertyEntry : properties.entrySet()) {
                if (!propertyLookup.containsKey(propertyEntry.getKey())) {
                    throw new IllegalArgumentException("Block does not contain property with name: " + propertyEntry.getKey());
                }
                Property<?> property = propertyLookup.get(propertyEntry.getKey());
                String propertyValue = JsonHelper.toString(property.getName(), propertyEntry.getValue());
                Optional<?> value = property.getValue(propertyValue);
                if (value.isEmpty()) {
                    throw new IllegalStateException("Property " + property.getName() + " doesn't contain value " + propertyValue);
                }
                stateProperties[index] = Map.entry(property, value.get());
                index++;
            }
            return new PartialBlockState<>(block.get(), Map.ofEntries(stateProperties));
        }
        return new PartialBlockState<>(block.get(), Map.of());
    }

    public T getBlock() {
        return block;
    }

    public boolean matches(BlockState state) {
        if (state.getBlock() != block) {
            return false;
        }

        for (Map.Entry<Property<?>, ?> property : properties.entrySet()) {
            if (!state.hasProperty(property.getKey())) {
                return false;
            }
            if (!state.getValue(property.getKey()).equals(property.getValue())) {
                return false;
            }
        }
        return true;
    }

    // I hate generics.
    public <K extends Comparable<K>, V extends K> BlockState transform(BlockState state) {
        for (Map.Entry<Property<?>, ?> entry : properties.entrySet()) {
            state = state.setValue((Property<K>) entry.getKey(), (V) entry.getValue());
        }
        return state;
    }

    public static <T extends Block> PartialBlockState<T> of(T block) {
        return new PartialBlockState<>(block, Map.of());
    }

    public static <T extends Block> PartialBlockState<T> of(T block, Map<Property<?>, ?> properties) {
        return new PartialBlockState<>(block, properties);
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(Registry.BLOCK.getKey(block));
        buffer.writeInt(properties.size());
        for (Map.Entry<Property<?>, ?> property : properties.entrySet()) {
            buffer.writeUtf(property.getKey().getName());
            buffer.writeUtf(property.getValue().toString());
        }
    }

    public static PartialBlockState<?> readFromBuffer(FriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        Block block = Registry.BLOCK.get(id);
        int mapSize = buffer.readInt();
        if (mapSize == 0) {
            return PartialBlockState.of(block);
        }
        Map<Property<?>, Object> properties = Maps.newHashMapWithExpectedSize(mapSize);
        for (int i = 0; i < mapSize; i++) {
            Property<?> key = block.getStateDefinition().getProperty(buffer.readUtf());
            Object value = key.getValue(buffer.readUtf());
            properties.put(key, value);
        }
        return PartialBlockState.of(block, properties);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", block.builtInRegistryHolder().key().location().toString());
        if (!properties.isEmpty()) {
            JsonObject jsonProperties = new JsonObject();
            for (Map.Entry<Property<?>, ?> property : properties.entrySet()) {
                jsonProperties.addProperty(property.getKey().getName(), property.getValue().toString());
            }
            json.add("state", jsonProperties);
        }
        return json;
    }
}
