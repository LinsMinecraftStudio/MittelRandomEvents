package io.github.lijinhong11.mittelrandomevents.api.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Default insertion-ordered registry implementation.
 *
 * <p>The resolver supplied to the constructor determines the ID used as the map key. Registration
 * of an existing ID replaces the previous value.
 *
 * @param <T> the registered value type
 */
public class DefaultRegistry<T> implements Registry<T> {
    private final Map<String, T> values = new LinkedHashMap<>();
    private final java.util.function.Function<T, String> idResolver;

    /**
     * Creates a registry with an ID resolver.
     *
     * @param idResolver function used to obtain the ID of each value
     * @throws NullPointerException if the resolver is null
     */
    public DefaultRegistry(java.util.function.Function<T, String> idResolver) {
        this.idResolver = idResolver;
    }

    @Override
    /**
     * Registers or replaces a value.
     *
     * @param value the value to register
     * @throws IllegalArgumentException if the value is null or its resolved ID is null or blank
     */
    public void register(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Registry value must not be null");
        }
        String id = idResolver.apply(value);
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Registry id must not be blank");
        }
        values.put(id, value);
    }

    @Override
    /**
     * Removes a value by ID.
     *
     * @param id the value ID
     */
    public void unregister(String id) {
        values.remove(id);
    }

    @Override
    /**
     * Looks up a value by ID.
     *
     * @param id the value ID
     * @return the value when registered
     */
    public Optional<T> get(String id) {
        return Optional.ofNullable(values.get(id));
    }

    @Override
    /**
     * Returns an unmodifiable view of the registered values.
     *
     * @return registered values in insertion order
     */
    public Collection<T> values() {
        return Collections.unmodifiableCollection(values.values());
    }

    @Override
    /**
     * Removes all registered values.
     */
    public void clear() {
        values.clear();
    }
}
