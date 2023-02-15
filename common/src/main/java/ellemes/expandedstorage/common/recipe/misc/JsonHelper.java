package ellemes.expandedstorage.common.recipe.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;

public class JsonHelper {
    private JsonHelper() {
        throw new IllegalStateException("Tried to instantiate a helper class");
    }

    public static void checkHasEntry(JsonObject object, String name) {
        if (!object.has(name)) {
            throw new JsonSyntaxException("Missing " + name + " entry");
        }
    }

    public static JsonObject getJsonObject(JsonObject object, String name) {
        checkHasEntry(object, name);
        if (!object.get(name).isJsonObject()) {
            throw new JsonSyntaxException(name + " entry must be an Object");
        }
        return object.getAsJsonObject(name);
    }

    public static ResourceLocation getJsonResourceLocation(JsonObject object, String name) {
        checkHasEntry(object, name);
        ResourceLocation resourceLocation = ResourceLocation.tryParse(getJsonString(object, name));
        if (resourceLocation == null) {
            throw new JsonSyntaxException(name + " entry must be a valid ResourceLocation");
        }
        return resourceLocation;
    }

    public static String getJsonString(JsonObject object, String name) {
        checkHasEntry(object, name);
        if (!object.get(name).isJsonPrimitive() || !object.get(name).getAsJsonPrimitive().isString()) {
            throw new JsonSyntaxException(name + " entry must be a String");
        }
        return object.getAsJsonPrimitive(name).getAsString();
    }

    public static JsonArray getJsonArray(JsonObject object, String name) {
        checkHasEntry(object, name);
        if (!object.get(name).isJsonArray()) {
            throw new JsonSyntaxException(name + " entry must be an Array");
        }

        return object.getAsJsonArray(name);
    }
}
