package io.github.lijinhong11.mittelrandomevents.api.event;

import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import java.util.Collection;
import java.util.Random;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
     * Returns the mutable parameter container shared by the current action chain.
     *
     * <p>Action configuration and runtime values use the same container abstraction. An action may
     * read its own {@link EventAction#parameters()}
     * and write values to this context container for following actions.
     */
    ParameterContainer parameters();

    /** Stops the remaining actions in the current event action chain. */
    void stop();

    /** Marks the next action in the current event action chain to be skipped. */
    void skipNext();

    /** Returns whether the current event action chain has been stopped. */
    boolean isStopped();

    /**
     * Consumes the pending skip marker for the next action.
     *
     * @return {@code true} when the next action should be skipped
     */
    boolean consumeSkipNext();

    /** Resets action control state before starting another action phase. */
    void resetExecutionControl();
}
