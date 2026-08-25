package io.github.lijinhong11.mittelrandomevents.api.event;

import io.github.lijinhong11.mittelrandomevents.api.action.ActionType;
import io.github.lijinhong11.mittelrandomevents.api.registry.ActionTypeRegistry;
import io.github.lijinhong11.mittelrandomevents.api.registry.RandomEventRegistry;
import java.util.Collection;
import java.util.Optional;

/**
 * Public entry point for the random event and action type registries.
 *
 * <p>The manager owns registration and lookup. It does not schedule or execute events; event
 * lines and the plugin task layer own those responsibilities.
 */
public interface RandomEventManager {
    /**
     * Returns the registry of reusable random events.
     *
     * @return the event registry
     */
    RandomEventRegistry eventsRegistry();

    /**
     * Returns the registry of action type implementations.
     *
     * @return the action type registry
     */
    ActionTypeRegistry actionTypesRegistry();

    /**
     * Registers an event through the event registry.
     *
     * @param event the event to register
     */
    void register(RandomEvent event);

    /**
     * Removes an event by ID.
     *
     * @param id the event ID
     */
    void unregister(String id);

    /**
     * Finds an event by ID.
     *
     * @param id the event ID
     * @return the event when registered
     */
    Optional<RandomEvent> get(String id);

    /**
     * Returns all registered events.
     *
     * @return registered events in registry order
     */
    Collection<RandomEvent> events();

    /**
     * Registers an externally supplied action type.
     *
     * @param actionType the action type to register
     */
    void registerActionType(ActionType actionType);

    /**
     * Removes an action type by ID.
     *
     * @param id the action type ID
     */
    void unregisterActionType(String id);

    /**
     * Finds an action type by ID.
     *
     * @param id the action type ID
     * @return the action type when registered
     */
    Optional<ActionType> getActionType(String id);
}
