package pl.greywarden.tools.listener;

public interface EventListener<T> {
    void handleEvent(T event);
}
