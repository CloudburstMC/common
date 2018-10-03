package com.nukkitx.plugin;

import com.nukkitx.api.plugin.PluginContainer;
import com.nukkitx.api.plugin.PluginDependency;
import com.nukkitx.api.plugin.PluginDescription;
import com.nukkitx.api.plugin.PluginLoader;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

public class SimplePluginContainer extends SimplePluginDescription implements PluginContainer {
    private final Object plugin;
    private final Logger logger;

    public SimplePluginContainer(String id, String name, String version, Collection<String> authors, String description,
                                 Collection<PluginDependency> dependencies, String url, Path path, PluginLoader loader, Object plugin,
                                 Logger logger) {
        super(id, name, version, authors, description, dependencies, url, path, loader);
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public SimplePluginContainer(PluginDescription description, Object plugin, Logger logger) {
        super(description);
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Nonnull
    @Override
    public Object getPlugin() {
        return plugin;
    }

    @Nonnull
    @Override
    public Logger getLogger() {
        return logger;
    }
}
