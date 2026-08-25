package io.github.lijinhong11.mittelrandomevents.api.registry;

import io.github.lijinhong11.mittelrandomevents.api.action.ActionType;

/** Registry containing built-in and externally provided {@link ActionType} implementations. */
public final class ActionTypeRegistry extends DefaultRegistry<ActionType> {
    /**
     * Creates an action type registry that uses {@link ActionType#id()} as its key.
     */
    public ActionTypeRegistry() {
        super(ActionType::id);
    }
}
