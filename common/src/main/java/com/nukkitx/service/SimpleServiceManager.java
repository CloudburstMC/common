package com.nukkitx.service;

import com.nukkitx.api.service.ServiceManager;
import com.nukkitx.api.service.ServicePriority;
import com.nukkitx.api.service.ServiceProvider;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimpleServiceManager implements ServiceManager {
    private final ConcurrentMap<Class<?>, List<ServiceProvider<?>>> serviceProviders = new ConcurrentHashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Override
    public <T> boolean register(@Nonnull Class<T> service, @Nonnull T provider, @Nonnull Object plugin, @Nonnull ServicePriority priority) {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(priority, "priority");

        return provide(service, provider, plugin, priority);
    }

    protected <T> boolean provide(Class<T> service, T provider, Object plugin, ServicePriority priority) {
        List<ServiceProvider<?>> list = serviceProviders.computeIfAbsent(service, k -> new CopyOnWriteArrayList<>());

        SimpleServiceProvider<T> registered = new SimpleServiceProvider<>(plugin, priority, service, provider);

        int position = Collections.binarySearch(list, registered);

        if (position > -1) return false;

        list.add(-(position + 1), registered);

        return true;
    }

    @Nonnull
    @Override
    public List<ServiceProvider<?>> cancel(@Nonnull Object plugin) {
        Objects.requireNonNull(plugin, "plugin");

        List<ServiceProvider<?>> cancelled = new ArrayList<>();

        Iterator<ServiceProvider<?>> iterator;
        ServiceProvider<?> provider;

        for (List<ServiceProvider<?>> providers : serviceProviders.values()) {
            iterator = providers.iterator();

            while (iterator.hasNext()) {
                provider = iterator.next();

                if (provider.getPlugin() == plugin) {
                    iterator.remove();
                    cancelled.add(provider);
                }
            }
        }

        return cancelled;
    }

    @Nonnull
    @Override
    public <T> Optional<ServiceProvider<T>> cancel(@Nonnull Class<T> service, @Nonnull T provider) {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(provider, "provider");

        ServiceProvider<T> cancelled = null;

        List<ServiceProvider<?>> providers = serviceProviders.get(service);
        if (providers != null) {
            Iterator<ServiceProvider<?>> iterator = providers.iterator();
            ServiceProvider<?> next;

            while (iterator.hasNext() && cancelled == null) {
                next = iterator.next();
                if (next.getProvider() == provider) {
                    iterator.remove();
                    cancelled = (ServiceProvider<T>) next;
                }
            }
        }

        return Optional.ofNullable(cancelled);
    }

    @Nonnull
    @Override
    public <T> Optional<ServiceProvider<T>> getProvider(@Nonnull Class<T> service) {
        List<ServiceProvider<?>> providers = serviceProviders.get(service);
        if (providers == null || providers.isEmpty()) return Optional.empty();
        return Optional.of((ServiceProvider<T>) providers.get(0));
    }

    @Nonnull
    @Override
    public Collection<Class<?>> getRegisteredServices() {
        rwLock.readLock().lock();
        try {
            return Collections.unmodifiableCollection(serviceProviders.keySet());
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
