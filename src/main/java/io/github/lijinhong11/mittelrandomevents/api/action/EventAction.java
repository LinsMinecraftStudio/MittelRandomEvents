package io.github.lijinhong11.mittelrandomevents.api.action;

import java.util.Map;

/**
 * One configured action belonging to a {@code RandomEvent}.
 *
 * <p>This record intentionally contains data only. It does not execute anything. When a line
 * selects the owning event, {@code RandomEventLine} resolves {@link #type()} through the action
 * type registry and passes this object to the registered action type.
 *
 * @param type the identifier of the registered action type
 * @param parameters the action-specific configuration parameters
 */
public record EventAction(String type, Map<String, Object> parameters) {
    /**
     * Creates an action and protects its parameter map from later structural modification.
     *
     * @param type the registered action type identifier
     * @param parameters action-specific parameters; {@code null} is treated as an empty map
     * @throws IllegalArgumentException if {@code type} is null or blank
     */
    public EventAction {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Action type must not be blank");
        }
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }

    /**
     * Creates an action without parameters.
     *
     * @param type the registered action type identifier
     * @return a parameterless event action
     * @throws IllegalArgumentException if {@code type} is null or blank
     */
    public static EventAction of(String type) {
        return new EventAction(type, Map.of());
    }
}
