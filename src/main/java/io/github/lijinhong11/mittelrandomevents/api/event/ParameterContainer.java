package io.github.lijinhong11.mittelrandomevents.api.event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Mutable, typed-at-the-call-site parameters shared by actions and an event context.
 *
 * <p>The container keeps configuration and runtime values in one representation while still
 * exposing a snapshot for YAML serialization. Values are intentionally untyped because different
 * action types own different parameter schemas.
 */
public final class ParameterContainer {
    private final Map<String, Object> values;

    public ParameterContainer() {
        this.values = new LinkedHashMap<>();
    }

    public ParameterContainer(Map<String, ?> values) {
        this();
        if (values != null) {
            values.forEach(this::set);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) values.get(Objects.requireNonNull(key, "key"));
    }

    public <T> T getOrDefault(String key, T defaultValue) {
        T value = get(key);
        return value == null ? defaultValue : value;
    }

    public void set(String key, Object value) {
        values.put(Objects.requireNonNull(key, "key"), value);
    }

    public Object remove(String key) {
        return values.remove(Objects.requireNonNull(key, "key"));
    }

    public boolean contains(String key) {
        return values.containsKey(Objects.requireNonNull(key, "key"));
    }

    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
