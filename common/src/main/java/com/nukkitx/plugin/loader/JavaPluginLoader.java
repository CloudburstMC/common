package com.nukkitx.plugin.loader;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import com.nukkitx.api.plugin.*;
import com.nukkitx.plugin.SimplePluginContainer;
import com.nukkitx.plugin.SimplePluginDependency;
import com.nukkitx.plugin.loader.java.JavaPluginClassLoader;
import com.nukkitx.plugin.loader.java.JavaPluginDescription;
import com.nukkitx.plugin.loader.java.PluginClassVisitor;
import com.nukkitx.plugin.loader.java.PluginInformation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class JavaPluginLoader implements PluginLoader {
    private static final PathMatcher PATH_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.jar");
    private final Map<Class, Object> dependencies = new HashMap<>();

    private JavaPluginLoader(Map<Class, Object> dependencies) {
        this.dependencies.putAll(dependencies);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    @Override
    public PluginDescription loadPlugin(@Nonnull Path path) throws Exception {
        Objects.requireNonNull(path, "path");

        try (JarInputStream jis = new JarInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            Manifest manifest = jis.getManifest();
            if (manifest == null) {
                throw new IllegalArgumentException("Jar has no manifest");
            }

            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }

                Optional<PluginInformation> optionalInfo = getInformation(jis);

                if (optionalInfo.isPresent()) {
                    PluginInformation information = optionalInfo.get();

                    List<PluginDependency> dependencies = new ArrayList<>();
                    for (Dependency dependency : information.getDependencies()) {
                        dependencies.add(new SimplePluginDependency(dependency.id(), dependency.version(), dependency.optional()));
                    }

                    try {
                        return new JavaPluginDescription(information.getId(), information.getName(), information.getVersion(),
                                information.getAuthors(), information.getDescription(), dependencies, information.getUrl(),
                                path, this, information.getClassName());
                    } catch (NullPointerException e) {
                        throw new IllegalArgumentException("Plugin does not contain the correct information", e);
                    }
                }
            }
        }
        throw new PluginException("No main class found");
    }

    @Nonnull
    @Override
    public PluginContainer createPlugin(@Nonnull PluginDescription description) throws Exception {
        Objects.requireNonNull(description, "description");
        if (!(description instanceof JavaPluginDescription)) {
            throw new IllegalArgumentException("Description provided is not of JavaPluginDescription");
        }

        Path path = description.getPath().orElseThrow(() -> new IllegalArgumentException("No path in plugin description"));

        Logger logger = LoggerFactory.getLogger(description.getId());
        return new SimplePluginContainer(description, instantiate(path, (JavaPluginDescription) description, logger),
                logger);
    }

    @Nonnull
    @Override
    public PathMatcher getPathMatcher() {
        return PATH_MATCHER;
    }

    private Object instantiate(Path path, JavaPluginDescription description, Logger logger) throws MalformedURLException, ClassNotFoundException {
        JavaPluginClassLoader loader = new JavaPluginClassLoader(new URL[]{path.toUri().toURL()});

        String className = description.getClassName().replace('/', '.');
        int last = className.lastIndexOf('.');

        String packageName = last == -1 ? "" : className.substring(0, last);

        Class<?> clazz = loader.loadClass(className);
        Injector injector = new InjectorBuilder()
                .addDefaultHandlers(packageName)
                .create();
        injector.register(PluginDescription.class, description);
        injector.register(Logger.class, logger);
        injector.register(Path.class, path.getParent().resolve(description.getId()).toAbsolutePath());
        dependencies.forEach(injector::register);

        return injector.newInstance(clazz);
    }

    private Optional<PluginInformation> getInformation(JarInputStream jis) throws IOException {
        ClassReader reader = new ClassReader(jis);
        PluginClassVisitor visitor = new PluginClassVisitor();

        reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return visitor.getInformation();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private final Map<Class, Object> dependencies = new HashMap<>();

        public Builder registerDependency(Class clazz, Object instance) {
            Objects.requireNonNull(clazz, "clazz");
            Objects.requireNonNull(instance, "instance");
            if (dependencies.containsKey(clazz)) {
                throw new IllegalArgumentException("Dependency is already registered");
            }

            dependencies.put(clazz, instance);
            return this;
        }

        public JavaPluginLoader build() {
            return new JavaPluginLoader(dependencies);
        }
    }
}
