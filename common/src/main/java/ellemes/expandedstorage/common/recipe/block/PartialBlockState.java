package ellemes.expandedstorage.common.recipe.block;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;

public class PartialBlockState<T extends Block> {
    private final T block;
    private final Map<Property<?>, ?> properties;

    public PartialBlockState(T block, Map<Property<?>, ?> properties) {
        this.block = block;
        this.properties = properties;
    }

    public static PartialBlockState<?> readFromJson(JsonObject object) {
        return null;
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
}
