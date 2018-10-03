package com.nukkitx.api.plugin;

import org.slf4j.Logger;

import javax.annotation.Nonnull;

public interface PluginContainer extends PluginDescription {
    @Nonnull
    Object getPlugin();

    @Nonnull
    Logger getLogger();
}
