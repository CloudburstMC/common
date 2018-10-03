package com.nukkitx.plugin.java;

import com.nukkitx.api.plugin.PluginDependency;
import com.nukkitx.api.plugin.PluginLoader;
import com.nukkitx.plugin.SimplePluginDescription;

import java.nio.file.Path;
import java.util.Collection;

public class JavaPluginDescription extends SimplePluginDescription {
    private final String className;

    public JavaPluginDescription(String id, String name, String version, Collection<String> authors, String description,
                                 Collection<PluginDependency> dependencies, String url, Path path, PluginLoader loader,
                                 String className) {
        super(id, name, version, authors, description, dependencies, url, path, loader);
        this.className = className;
    }

    public JavaPluginDescription(JavaPluginDescription description) {
        super(description);
        this.className = description.className;
    }

    public String getClassName() {
        return className;
    }
}
