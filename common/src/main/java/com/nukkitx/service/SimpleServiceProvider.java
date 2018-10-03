package com.nukkitx.service;

import com.nukkitx.api.service.ServicePriority;
import com.nukkitx.api.service.ServiceProvider;
import lombok.Value;

@Value
public class SimpleServiceProvider<T> implements ServiceProvider<T> {
    private final Object plugin;
    private final ServicePriority priority;
    private final Class<T> service;
    private final T provider;

    @Override
    public int compareTo(ServiceProvider<T> that) {
        return that.getPriority().ordinal() - this.priority.ordinal();
    }
}
