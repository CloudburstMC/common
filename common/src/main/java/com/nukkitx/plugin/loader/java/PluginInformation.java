package com.nukkitx.plugin.loader.java;

import com.nukkitx.api.plugin.Dependency;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PluginInformation {
    private final String className;
    private final List<String> authors = new ArrayList<>();
    private final List<Dependency> dependencies = new ArrayList<>();
    private String id;
    private String name;
    private String version;
    private String url;
    private String description;

    public String getName() {
        if (name == null) {
            return id;
        }
        return name;
    }
}
