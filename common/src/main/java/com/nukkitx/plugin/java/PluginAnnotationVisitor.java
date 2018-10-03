package com.nukkitx.plugin.java;

import com.nukkitx.api.plugin.Dependency;
import lombok.Getter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

@Getter
public class PluginAnnotationVisitor extends AnnotationVisitor {
    private final String className;
    private final PluginInformation information;
    private String arrayName = null;

    public PluginAnnotationVisitor(String className) {
        super(Opcodes.ASM5);
        this.className = className;
        this.information = new PluginInformation(className);
    }

    @Override
    public void visit(String name, Object value) {
        switch (arrayName) {
            case "dependencies":
                information.getDependencies().add((Dependency) value);
                break;
            case "authors":
                information.getAuthors().add((String) value);
                break;
            default:
                switch (name) {
                    case "id":
                        information.setId((String) value);
                        break;
                    case "version":
                        information.setVersion((String) value);
                        break;
                    case "description":
                        information.setDescription((String) value);
                        break;
                    case "name":
                        information.setName((String) value);
                        break;
                    case "url":
                        information.setUrl((String) value);
                }
        }
    }

    public AnnotationVisitor visitArray(String name) {
        this.arrayName = name;
        return null;
    }
}
