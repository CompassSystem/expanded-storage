package ellemes.expandedstorage.thread.wrappers;

import ellemes.expandedstorage.common.config.ConfigV0;
import ellemes.expandedstorage.common.misc.ConfigWrapper;
import ellemes.expandedstorage.thread.config.LegacyFactory;

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
