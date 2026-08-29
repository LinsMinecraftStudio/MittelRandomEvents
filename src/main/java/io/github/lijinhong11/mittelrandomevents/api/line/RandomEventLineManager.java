package io.github.lijinhong11.mittelrandomevents.api.line;

import java.util.Collection;
import java.util.Optional;

/**
 * Registry and lifecycle entry point for configured random event lines.
 */
public interface RandomEventLineManager {
    /**
     * Registers a line, replacing an existing line with the same ID.
     *
     * @param line the line to register
     */
    void register(RandomEventLine line);

    /**
     * Removes a line by ID.
     *
     * @param id the line ID
     */
    void unregister(String id);

    /**
     * Finds a line by ID.
     *
     * @param id the line ID
     * @return the line when registered
     */
    Optional<RandomEventLine> get(String id);

    /**
     * Returns all registered lines.
     *
     * @return registered lines in insertion order
     */
    Collection<RandomEventLine> lines();

    /**
     * Removes every registered line.
     */
    void clear();
}
