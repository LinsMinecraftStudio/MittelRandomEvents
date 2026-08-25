package io.github.lijinhong11.mittelrandomevents.api.registry;

import java.util.Collection;
import java.util.Optional;

/**
 * A public registry of objects identified by a stable string ID.
 *
 * <p>Registries preserve insertion order when returning {@link #values()}. Registering another
 * object with an existing ID replaces the previous object. Implementations should document their
 * thread-safety guarantees when they are used by asynchronous integrations.
 *
 * @param <T> the registered object type
 */
public interface Registry<T> {
    /**
     * Registers a value under the ID provided by the implementation's ID resolver.
     *
     * @param value the value to register
     * @throws IllegalArgumentException if the value or its ID is invalid
     */
    void register(T value);

    /**
     * Removes the value registered under an ID.
     *
     * @param id the ID to remove
     */
    void unregister(String id);

    /**
     * Finds a value by ID.
     *
     * @param id the ID to look up
     * @return the registered value, or an empty optional when no value is registered
     */
    Optional<T> get(String id);

    /**
     * Returns all registered values.
     *
     * @return an unmodifiable collection of registered values
     */
    Collection<T> values();

    /**
     * Checks whether an ID is registered.
     *
     * @param id the ID to check
     * @return {@code true} when a value is registered under the ID
     */
    default boolean contains(String id) {
        return get(id).isPresent();
    }

    /**
     * Removes every value from this registry.
     */
    void clear();
}
