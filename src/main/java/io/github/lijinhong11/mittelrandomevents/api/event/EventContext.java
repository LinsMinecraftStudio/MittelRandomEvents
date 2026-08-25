package io.github.lijinhong11.mittelrandomevents.api.event;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Random;

/**
 * Runtime data supplied to conditions and action types while a random event is being processed.
 *
 * <p>A context belongs to one execution environment. Implementations may create a new context
 * for each line execution so that elapsed time and selected entities remain current.
 */
public interface EventContext {
    /**
     * Returns the manager that owns the event and action type registries.
     *
     * @return the current random event manager
     */
    RandomEventManager manager();

    /**
     * Returns the source of randomness used by event selection.
     *
     * @return the random number generator for this context
     */
    Random getRandom();

    /**
     * Returns the players selected for the current execution.
     *
     * @return the selected players, never {@code null}
     */
    Collection<Player> getSelectedPlayers();

    /**
     * Replaces the players selected for the current action chain.
     *
     * <p>The next action executed by the same {@code RandomEventLine} observes this collection.
     * Implementations should copy the supplied collection when the context owns its state.
     *
     * @param players the new selected players
     */
    void setSelectedPlayers(Collection<? extends Player> players);

    /**
     * Returns the worlds selected for the current execution.
     *
     * @return the selected worlds, never {@code null}
     */
    Collection<World> getSelectedWorlds();

    /**
     * Replaces the worlds selected for the current action chain.
     *
     * <p>The next action executed by the same {@code RandomEventLine} observes this collection.
     *
     * @param worlds the new selected worlds
     */
    void setSelectedWorlds(Collection<World> worlds);

    /**
     * Returns the elapsed time associated with the current execution.
     *
     * @return elapsed time in milliseconds
     */
    long elapsedMillis();

    /**
     * Reads a value from the context's shared runtime data.
     *
     * @param key the data key
     * @param <T> the expected value type
     * @return the stored value, or {@code null} when no value is stored for the key
     */
    <T> T get(String key);

    /**
     * Stores a value in the context's shared runtime data.
     *
     * @param key the data key
     * @param value the value to store
     * @param <T> the value type
     */
    <T> void set(String key, T value);
}
