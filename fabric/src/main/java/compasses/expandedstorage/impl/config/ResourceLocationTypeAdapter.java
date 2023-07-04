package compasses.expandedstorage.impl.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public final class ResourceLocationTypeAdapter extends TypeAdapter<ResourceLocation> {
    @Override
    public void write(JsonWriter writer, ResourceLocation value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        writer.value(value.toString());
    }

    @Override
    public ResourceLocation read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        return new ResourceLocation(reader.nextString());
    }
}
