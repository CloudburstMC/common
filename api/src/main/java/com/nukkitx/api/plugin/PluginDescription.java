package com.nukkitx.api.plugin;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public interface PluginDescription {
    /**
     * The ID for this plugin. This should be an alphanumeric name. Slashes are also allowed.
     *
     * @return the ID for this plugin
     */
    @Nonnull
    String getId();

    @Nonnull
    String getName();

    @Nonnull
    Optional<String> getDescription();

    /**
     * The path where the plugin is located on the file system.
     *
     * @return the path of this plugin
     */
    @Nonnull
    Optional<Path> getPath();

    /**
     * The author of this plugin.
     *
     * @return the plugin's author
     */
    @Nonnull
    Collection<String> getAuthors();

    /**
     * The version of this plugin.
     *
     * @return the version of this plugin
     */
    @Nonnull
    String getVersion();

    /**
     * The array of plugin IDs that this plugin requires in order to function fully.
     *
     * @return the dependencies
     */
    @Nonnull
    Collection<PluginDependency> getDependencies();

    /**
     * Plugin's website specified in the plugin.yml.
     *
     * @return website url
     */
    @Nonnull
    Optional<String> getUrl();

    @Nonnull
    PluginLoader getPluginLoader();
}
