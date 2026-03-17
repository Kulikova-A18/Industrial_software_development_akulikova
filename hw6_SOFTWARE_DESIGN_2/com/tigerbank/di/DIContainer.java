package com.tigerbank.di;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DIContainer {
    private final Map<Class<?>, Supplier<?>> dependencies = new HashMap<>();
    private final Map<Class<?>, Object> singletons = new HashMap<>();

    public <T> void register(Class<T> type, Supplier<T> supplier) {
        dependencies.put(type, supplier);
    }

    public <T> void registerSingleton(Class<T> type, Supplier<T> supplier) {
        singletons.put(type, supplier.get());
    }

    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {

        if (singletons.containsKey(type)) {
            return (T) singletons.get(type);
        }

        Supplier<?> supplier = dependencies.get(type);
        if (supplier == null) {
            throw new RuntimeException("Зависимость не найдена для типа: " + type.getName());
        }

        T instance = type.cast(supplier.get());

        if (type.isAnnotationPresent(Singleton.class)) {
            singletons.put(type, instance);
        }

        return instance;
    }

    public <T> void registerInstance(Class<T> type, T instance) {
        singletons.put(type, instance);
    }
}