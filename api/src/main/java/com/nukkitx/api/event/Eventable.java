package com.nukkitx.api.event;

public interface Eventable<T extends Event> {

    void onEvent(T event);
}
