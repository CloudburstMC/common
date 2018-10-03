package com.nukkitx.api.plugin;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

@ParametersAreNonnullByDefault
public interface PluginLoader {

    @Nonnull
    PluginDescription loadPlugin(Path path) throws Exception;

    @Nonnull
    PluginContainer createPlugin(PluginDescription description) throws Exception;

    @Nonnull
    PathMatcher getPathMatcher();
}
