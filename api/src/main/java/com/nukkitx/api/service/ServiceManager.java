package com.nukkitx.api.service;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public interface ServiceManager {

    /**
     * Register an object as a service's provider.
     *
     * @param <T>      the service
     * @param service  the service class
     * @param provider the service provider
     * @param plugin   the plugin
     * @param priority the priority
     * @return {@code true}, or {@code false} only if {@code provider} already registered
     */
    <T> boolean register(Class<T> service, T provider, Object plugin, ServicePriority priority);

    /**
     * Cancel service's provider(s) offered by this plugin.
     *
     * @param plugin the plugin
     * @return an unmodifiable {@link List}
     */
    @Nonnull
    List<ServiceProvider<?>> cancel(Object plugin);

    /**
     * Cancel a service's provider.
     *
     * @param <T>      the service
     * @param service  the service class
     * @param provider the provider
     * @return the cancelled {@link ServiceProvider}, or {@code null} if not
     * any provider cancelled
     */
    @Nonnull
    <T> Optional<ServiceProvider<T>> cancel(Class<T> service, T provider);

    /**
     * Return the service's provider.
     *
     * @param <T>     the service
     * @param service the target service class
     * @return a {@link ServiceProvider} registered highest priority, or
     * {@code null} if not exists
     */
    @Nonnull
    <T> Optional<ServiceProvider<T>> getProvider(Class<T> service);

    /**
     * Gets all registered services.
     *
     * @return an unmodifiable {@link Collection} containing all registered services
     */
    @Nonnull
    Collection<Class<?>> getRegisteredServices();
}
