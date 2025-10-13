package io.github.dracosomething.util;

import java.util.Map;

public class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> Pair<K, V> ofEntry(Map.Entry<K, V> entry) {
        return new Pair<>(entry.getKey(), entry.getValue());
    }

    public V getValue() {
        return value;
    }

    public K getKey() {
        return key;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
