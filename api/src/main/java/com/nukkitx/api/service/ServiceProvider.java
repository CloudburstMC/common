package com.nukkitx.api.service;

public interface ServiceProvider<T> extends Comparable<ServiceProvider<T>> {

    Class<T> getService();

    Object getPlugin();

    T getProvider();

    ServicePriority getPriority();
}
