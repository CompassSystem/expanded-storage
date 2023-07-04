package compasses.expandedstorage.common.config.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import compasses.expandedstorage.common.config.ResourceLocationTypeAdapter;
import compasses.expandedstorage.common.config.internal.InternalConfig;
import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.common.FabricMain;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommonConfigManager {
    static boolean ioErrorsOccurred = false;

    private static InternalConfig internalConfig;

    public static InternalConfig getInternalConfig() {
        if (internalConfig != null) {
            return internalConfig;
        }

        Path configPath = getLocalConfigPath();

        if (configPath == null) {
            internalConfig = new InternalConfig();
        } else {
            Path filePath = configPath.resolve("internal_config.json");
            if (Files.notExists(filePath)) {
                internalConfig = new InternalConfig();
                saveFile(filePath, internalConfig, config -> {});
            } else {
                internalConfig = loadFile(filePath, InternalConfig.class, InternalConfig::new);
            }
        }

        return internalConfig;
    }

    private static void printIoError(IOException exception) {
        if (!ioErrorsOccurred) {
            Utils.LOGGER.error("Failed to interact with the file system, configs will not load or save: ", exception);
            ioErrorsOccurred = true;
        }
    }

    public static <T> T loadFile(Path filePath, Class<T> configClass, Supplier<T> defaultSupplier) {
        Gson gson = createGsonInstance();
        T returnValue;
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            returnValue = gson.fromJson(reader, configClass);
        } catch (IOException exception) {
            printIoError(exception);
            returnValue = defaultSupplier.get();
        }

        return returnValue;
    }

    public static <T> boolean saveFile(Path filePath, T config, Consumer<T> successCallback) {
        Gson gson = createGsonInstance();

        try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            gson.toJson(config, writer);

            successCallback.accept(config);
            return true;
        } catch (IOException exception) {
            printIoError(exception);
        }

        return false;
    }

    private static Path getGlobalConfigPath() {
        Path minecraftPath = Path.of(System.getProperty("user.home"));
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows")) {
            minecraftPath = minecraftPath.resolve("AppData/Roaming/.minecraft/");
        } else if (os.startsWith("Mac")) {
            minecraftPath = minecraftPath.resolve("Library/Application Support/minecraft/");
        } else { // Assume Linux
            minecraftPath = minecraftPath.resolve(".minecraft/");
        }

        return minecraftPath.resolve(Utils.MOD_ID + "_global_config/");
    }

    public static Path getGlobalConfigPathOrNull() {
        Path configPath = getGlobalConfigPath();

        if (Files.notExists(configPath)) {
            try {
                Files.createDirectories(configPath);
            } catch (IOException exception) {
                printIoError(exception);
                return null;
            }
        }

        return configPath;
    }

    public static Path getLocalConfigPath() {
        Path configPath = FabricMain.getLocalConfigPath();

        if (Files.notExists(configPath)) {
            try {
                Files.createDirectories(configPath);
            } catch (IOException exception) {
                printIoError(exception);
                return null;
            }
        }

        return configPath;
    }

    private static Gson createGsonInstance() {
        return new GsonBuilder()
                .registerTypeAdapter(ResourceLocation.class, new ResourceLocationTypeAdapter())
                .setPrettyPrinting()
                .create();
    }

    public static void saveInternalConfig() {
        Path configPath = getLocalConfigPath();

        if (configPath != null) {
            Path filePath = configPath.resolve("internal_config.json");
            saveFile(filePath, internalConfig, config -> {});
        }
    }
}
