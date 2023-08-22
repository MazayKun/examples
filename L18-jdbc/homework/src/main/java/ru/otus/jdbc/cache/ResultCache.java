package ru.otus.jdbc.cache;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class ResultCache<V> {

    private final Map<Long, SoftReference<Optional<V>>> cache = new LinkedHashMap<>();
    private final int capacity;

    public ResultCache(int capacity) {
        this.capacity = capacity;
    }

    public Optional<V> addIfAbsent(Long key, LongFunction<Optional<V>> dataExtractor) {
        SoftReference<Optional<V>> softValue = cache.remove(key);
        if(softValue == null) {
            Optional<V> value = dataExtractor.apply(key);
            softValue = new SoftReference<>(value);
            if(cache.size() == capacity) {
                cache.remove(cache.keySet().iterator().next());
            }
            cache.put(key, softValue);
            return value;
        }
        Optional<V> value = softValue.get();
        if(value == null) {
            value = dataExtractor.apply(key);
            softValue = new SoftReference<>(value);
        }
        cache.put(key, softValue);
        return value;
    }

    public void updateIfExist(Long key, V value) {
        SoftReference<Optional<V>> softValue = cache.remove(key);
        if(softValue != null) {
            cache.put(key, new SoftReference<>(Optional.of(value)));
        }
    }
}
