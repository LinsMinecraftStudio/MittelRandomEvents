package io.github.lijinhong11.mittelrandomevents.api.event;

import io.github.lijinhong11.mittelrandomevents.api.action.ActionType;
import io.github.lijinhong11.mittelrandomevents.api.registry.ActionTypeRegistry;
import io.github.lijinhong11.mittelrandomevents.api.registry.RandomEventRegistry;
import java.util.Collection;
import java.util.Optional;

/**
 * Registry access for reusable events and action types.
 */
public final class DefaultRandomEventManager implements RandomEventManager {
    private final RandomEventRegistry eventRegistry = new RandomEventRegistry();
    private final ActionTypeRegistry actionTypeRegistry = new ActionTypeRegistry();

    @Override
    public RandomEventRegistry eventsRegistry() {
        return eventRegistry;
    }

    @Override
    public ActionTypeRegistry actionTypesRegistry() {
        return actionTypeRegistry;
    }

    @Override
    public void register(RandomEvent event) {
        eventRegistry.register(event);
    }

    @Override
    public void unregister(String id) {
        eventRegistry.unregister(id);
    }

    @Override
    public Optional<RandomEvent> get(String id) {
        return eventRegistry.get(id);
    }

    @Override
    public Collection<RandomEvent> events() {
        return eventRegistry.values();
    }

    @Override
    public void registerActionType(ActionType actionType) {
        actionTypeRegistry.register(actionType);
    }

    @Override
    public void unregisterActionType(String id) {
        actionTypeRegistry.unregister(id);
    }

    @Override
    public Optional<ActionType> getActionType(String id) {
        return actionTypeRegistry.get(id);
    }
}
