package io.github.lijinhong11.mittelrandomevents.api.event;

import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittelrandomevents.api.Localized;
import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Data belonging to one random event.
 *
 * <p>This class deliberately does not execute event logic. It only stores the event identifier,
 * its configured actions, its enabled state and its selection conditions. A
 * {@code RandomEventLine} owns event selection and resolves each action through the registered
 * {@link io.github.lijinhong11.mittelrandomevents.api.action.ActionType} implementations.
 */
public class RandomEvent implements Localized {
    private final String id;
    private List<EventAction> actions;
    private List<EventAction> endActions;

    @Setter
    @Getter
    private boolean enabled = true;

    private List<EventCondition> conditions = List.of();

    @Getter
    @Setter
    private Material icon = Material.PAPER;

    @Getter
    @Setter
    private @NotNull Component displayName;

    /**
     * Creates a no-op event that can be inserted into a line as a weighted empty result.
     *
     * @param id the stable identifier of the empty event
     * @return an event with no actions
     */
    public static RandomEvent empty(String id) {
        return new RandomEvent(id, Collections.emptyList());
    }

    /**
     * Creates a random event.
     *
     * @param id the stable identifier of the event
     * @param actions the data-only actions that the line will execute when this event is selected
     * @throws IllegalArgumentException if the identifier is null or blank
     * @throws NullPointerException if the action collection or one of its actions is null
     */
    public RandomEvent(String id, Collection<? extends EventAction> actions) {
        this(id, actions, List.of());
    }

    public RandomEvent(
            String id, Collection<? extends EventAction> actions, Collection<? extends EventAction> endActions) {
        validateId(id);

        this.id = id;
        this.actions = List.copyOf(actions);
        this.endActions = List.copyOf(endActions);
        this.displayName = ComponentUtils.text(id);
    }

    /**
     * Creates a random event.
     *
     * @param id the stable identifier of the event
     * @param displayName the display name of the event
     * @param actions the data-only actions that the line will execute when this event is selected
     * @throws IllegalArgumentException if the identifier is null or blank
     * @throws NullPointerException if the action collection or one of its actions is null
     */
    public RandomEvent(String id, Component displayName, Collection<? extends EventAction> actions) {
        this(id, displayName, actions, List.of());
    }

    public RandomEvent(
            String id,
            Component displayName,
            Collection<? extends EventAction> actions,
            Collection<? extends EventAction> endActions) {
        validateId(id);

        this.id = id;
        this.actions = List.copyOf(actions);
        this.endActions = List.copyOf(endActions);
        this.displayName = displayName;
    }

    /**
     * Returns the stable event identifier.
     *
     * @return the event identifier
     */
    @Override
    public String id() {
        return id;
    }

    @Override
    public Component displayName(org.bukkit.command.CommandSender sender) {
        return displayName;
    }

    @Override
    public Material icon() {
        return icon;
    }

    /**
     * Returns the immutable action list in execution order.
     *
     * @return the configured actions
     */
    public List<EventAction> actions() {
        return actions;
    }

    public List<EventAction> endActions() {
        return endActions;
    }

    public void setActions(Collection<? extends EventAction> actions) {
        this.actions = List.copyOf(actions);
    }

    public void setEndActions(Collection<? extends EventAction> actions) {
        this.endActions = List.copyOf(actions);
    }

    public void addAction(EventAction action) {
        List<EventAction> updated = new java.util.ArrayList<>(actions);
        updated.add(Objects.requireNonNull(action, "action"));
        actions = List.copyOf(updated);
    }

    public void addEndAction(EventAction action) {
        List<EventAction> updated = new java.util.ArrayList<>(endActions);
        updated.add(Objects.requireNonNull(action, "action"));
        endActions = List.copyOf(updated);
    }

    public void removeAction(int index) {
        List<EventAction> updated = new java.util.ArrayList<>(actions);
        updated.remove(index);
        actions = List.copyOf(updated);
    }

    public boolean removeAction(EventAction action) {
        List<EventAction> updated = new java.util.ArrayList<>(actions);
        if (!updated.remove(action)) {
            return false;
        }
        actions = List.copyOf(updated);
        return true;
    }

    public boolean removeEndAction(EventAction action) {
        List<EventAction> updated = new java.util.ArrayList<>(endActions);
        if (!updated.remove(action)) return false;
        endActions = List.copyOf(updated);
        return true;
    }

    public void removeCondition(int index) {
        List<EventCondition> updated = new java.util.ArrayList<>(conditions);
        updated.remove(index);
        conditions = List.copyOf(updated);
    }

    public void addCondition(EventCondition condition) {
        List<EventCondition> updated = new java.util.ArrayList<>(conditions);
        updated.add(Objects.requireNonNull(condition, "condition"));
        setConditions(updated);
    }

    public void setCondition(int index, EventCondition condition) {
        List<EventCondition> updated = new java.util.ArrayList<>(conditions);
        updated.set(index, Objects.requireNonNull(condition, "condition"));
        setConditions(updated);
    }

    /**
     * Returns the immutable conditions used during event selection.
     *
     * @return the event conditions
     */
    public List<EventCondition> conditions() {
        return conditions;
    }

    /**
     * Replaces the event conditions.
     *
     * @param conditions the conditions that must all pass before selection
     * @throws NullPointerException if the collection or one of its conditions is null
     */
    public void setConditions(Collection<EventCondition> conditions) {
        List<EventCondition> copied = List.copyOf(conditions);
        for (EventCondition condition : copied) {
            if (condition.type() == null || condition.type().isBlank()) {
                throw new IllegalArgumentException("Event conditions must be serializable");
            }
        }
        this.conditions = copied;
    }

    /**
     * Checks whether this event can currently be selected.
     *
     * @param context the runtime context used by the conditions
     * @return {@code true} when the event is enabled and all conditions pass
     */
    public boolean isCompatible(EventContext context) {
        return enabled && conditions.stream().allMatch(condition -> condition.test(context));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RandomEvent other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static void validateId(String id) {
        if (id == null || !id.matches("[a-z0-9_-]+")) {
            throw new IllegalArgumentException("Event id must match [a-z0-9_-]+");
        }
    }
}
