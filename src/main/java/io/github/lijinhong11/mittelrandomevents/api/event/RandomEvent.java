package io.github.lijinhong11.mittelrandomevents.api.event;

import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittelrandomevents.api.Localized;
import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Material;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Collections;

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
    @Setter
    private List<EventAction> actions;
    @Setter
    @Getter
    private boolean enabled = true;
    private List<EventCondition> conditions = List.of();
    @Getter
    @Setter
    private Material icon = Material.PAPER;
    @Getter
    @Setter
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
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event id must not be blank");
        }

        this.id = id;
        this.actions = List.copyOf(actions);
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
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event id must not be blank");
        }

        this.id = id;
        this.actions = List.copyOf(actions);
        this.displayName = displayName;
    }

    /**
     * Returns the stable event identifier.
     *
     * @return the event identifier
     */
    @Override public String id() {
        return id;
    }

    @Override public Component displayName(org.bukkit.command.CommandSender sender) {
        return displayName;
    }

    @Override public Material icon() {
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
        this.conditions = List.copyOf(conditions);
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

    @Override public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RandomEvent other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }
}
