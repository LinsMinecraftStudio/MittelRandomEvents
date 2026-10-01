package io.github.lijinhong11.mittelrandomevents.api.action;

import io.github.lijinhong11.mittelrandomevents.api.event.EventCondition;
import io.github.lijinhong11.mittelrandomevents.api.event.ParameterContainer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * One configured action belonging to a {@code RandomEvent}.
 *
 * <p>This class intentionally contains data only. It does not execute anything. When a line
 * selects the owning event, {@code RandomEventLine} resolves {@link #type()} through the action
 * type registry and passes this object to the registered action type.
 *
 * @param type the identifier of the registered action type
 * @param parameters the mutable action-specific parameter container
 */
public final class EventAction {
    private final String type;
    private final ParameterContainer parameters;
    private List<EventCondition> conditions;

    public EventAction(String type, Map<String, ?> parameters) {
        this(type, new ParameterContainer(parameters), List.of());
    }

    public EventAction(String type, ParameterContainer parameters) {
        this(type, parameters, List.of());
    }
    /**
     * Creates an action from a configuration map.
     *
     * @param type the registered action type identifier
     * @param parameters action-specific parameters; {@code null} is treated as an empty container
     * @throws IllegalArgumentException if {@code type} is null or blank
     */
    public EventAction(String type, ParameterContainer parameters, List<EventCondition> conditions) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Action type must not be blank");
        }
        this.type = type;
        parameters = parameters == null ? new ParameterContainer() : parameters;
        this.parameters = parameters;
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
        this.conditions = conditions;
    }

    public String type() {
        return type;
    }

    public ParameterContainer parameters() {
        return parameters;
    }

    public List<EventCondition> conditions() {
        return conditions;
    }

    /**
     * Creates an action without parameters.
     *
     * @param type the registered action type identifier
     * @return a parameterless event action
     * @throws IllegalArgumentException if {@code type} is null or blank
     */
    public static EventAction of(String type) {
        return new EventAction(type, new ParameterContainer(), List.of());
    }

    public void addCondition(EventCondition condition) {
        java.util.ArrayList<EventCondition> updated = new java.util.ArrayList<>(conditions);
        updated.add(java.util.Objects.requireNonNull(condition, "condition"));
        conditions = List.copyOf(updated);
    }

    public boolean removeCondition(EventCondition condition) {
        java.util.ArrayList<EventCondition> updated = new java.util.ArrayList<>(conditions);
        if (!updated.remove(condition)) return false;
        conditions = List.copyOf(updated);
        return true;
    }

    public void setConditions(Collection<? extends EventCondition> conditions) {
        this.conditions = List.copyOf(conditions);
    }
}
