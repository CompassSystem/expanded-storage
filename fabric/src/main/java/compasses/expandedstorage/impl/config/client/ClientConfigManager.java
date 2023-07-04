package compasses.expandedstorage.impl.config.client;

import compasses.expandedstorage.impl.config.common.CommonConfigManager;
import compasses.expandedstorage.impl.config.internal.InternalConfig;
import compasses.expandedstorage.impl.misc.Utils;

import java.nio.file.Files;
import java.nio.file.Path;

public class ClientConfigManager {

    private static ClientConfigV0 config;

    public static ClientConfigV0 getClientConfig() {
        if (config != null) {
            return config;
        }

        InternalConfig internalConfig = CommonConfigManager.getInternalConfig();

        Path configPath = internalConfig.shouldUseGlobalConfig() ? CommonConfigManager.getGlobalConfigPathOrNull() : CommonConfigManager.getLocalConfigPath();

        if (configPath == null) {
            config = new ClientConfigV0();
        } else {
            Path filePath = configPath.resolve("client_config.json");
            if (Files.notExists(filePath)) {
                config = new ClientConfigV0();
                CommonConfigManager.saveFile(filePath, config, config -> {});
                CommonConfigManager.getInternalConfig();
            } else if (internalConfig.getConfigVersion() == Utils.CURRENT_CONFIG_VERSION) {
                config = CommonConfigManager.loadFile(filePath, ClientConfigV0.class, ClientConfigV0::new);
            } else {
                throw new IllegalStateException("Trying to load unsupported config version: " + internalConfig.getConfigVersion());
            }
        }

        return config;
    }

    public static void saveConfig() {
        Path configPath = CommonConfigManager.getInternalConfig().shouldUseGlobalConfig() ? CommonConfigManager.getGlobalConfigPathOrNull() : CommonConfigManager.getLocalConfigPath();

        if (configPath != null) {
            Path filePath = configPath.resolve("client_config.json");
            CommonConfigManager.saveFile(filePath, config, config -> {});
        }
    }
}
