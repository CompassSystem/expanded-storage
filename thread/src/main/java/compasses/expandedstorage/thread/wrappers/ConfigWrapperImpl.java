package compasses.expandedstorage.thread.wrappers;

import compasses.expandedstorage.common.config.ConfigV0;
import compasses.expandedstorage.common.misc.ConfigWrapper;
import compasses.expandedstorage.thread.config.LegacyFactory;

import java.nio.file.Path;

public final class ConfigWrapperImpl extends ConfigWrapper {
    public ConfigWrapperImpl(Path configPath, Path oldConfigPath) {
        super(configPath, oldConfigPath);
    }

    @Override
    protected ConfigV0 readOldConfig(String configLines, Path oldConfigPath) {
        return this.convertToConfig(configLines, LegacyFactory.INSTANCE, oldConfigPath);
    }
}
