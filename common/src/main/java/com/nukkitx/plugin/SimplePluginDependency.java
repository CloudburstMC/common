package com.nukkitx.plugin;

import com.nukkitx.api.plugin.PluginDependency;
import lombok.NonNull;
import lombok.Value;

@Value
public class SimplePluginDependency implements PluginDependency {
    @NonNull
    private final String id;
    @NonNull
    private final String version;
    private final boolean optional;
}
