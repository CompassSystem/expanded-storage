package compasses.idk_plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.io.FilterReader;
import java.io.Reader;
import java.io.StringReader;

public class JsonNormalizerReader extends FilterReader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public JsonNormalizerReader(Reader reader) {
        super(new StringReader(GSON.toJson(GSON.fromJson(reader, JsonElement.class))));
    }
}
