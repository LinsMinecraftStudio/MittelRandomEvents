package io.github.lijinhong11.mittelrandomevents.api.line;

import io.github.lijinhong11.mittelrandomevents.api.Localized;
import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

/**
 * A timed collection of random events.
 *
 * <p>Each execution selects at most one compatible event from this line. The line owns event
 * weights and resolves every selected {@code EventAction} through the manager in the supplied
 * context. Scheduling the line itself is the responsibility of {@code TaskMaker}.
 */
public class RandomEventLine implements Localized {
    @Getter
    private final @NotNull String id;
    private final int intervalSeconds;
    private final List<RandomEvent> events;
    private final Map<RandomEvent, Double> weights = new HashMap<>();
    @Getter
    @Setter
    private Material icon = Material.PAPER;
    @Getter
    @Setter
    private @NotNull Function<@Nullable CommandSender, Component> displayNameFunction;

    /**
     * Creates an event line.
     *
     * @param id the stable line identifier
     * @param intervalSeconds the interval used by the scheduler, in seconds; zero disables this line
     * @param events the events initially available to this line
     * @throws IllegalArgumentException if the ID is blank or the interval is negative
     * @throws NullPointerException if the event collection is null
     */
    public RandomEventLine(@NotNull String id, int intervalSeconds, Collection<RandomEvent> events) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("Line id must not be blank");
        }

        if (intervalSeconds < 0) {
            throw new IllegalArgumentException("Interval must not be negative");
        }

        this.id = id;
        this.displayNameFunction = _ -> ComponentUtils.text(id);
        this.intervalSeconds = intervalSeconds;
        this.events = new ArrayList<>(events);
        for (RandomEvent event : this.events) {
            weights.put(event, 1.0D);
        }
    }

    /**
     * Creates a line with a direct display name component.
     *
     * @param id the stable line identifier
     * @param displayNameFunction the component function stored with the line
     * @param intervalSeconds the execution interval; zero disables the line
     * @param events the events attached to the line
     */
    public RandomEventLine(
            String id, @NotNull Function<@Nullable CommandSender, Component> displayNameFunction, int intervalSeconds, Collection<RandomEvent> events) {
        this(id, intervalSeconds, events);
        this.displayNameFunction = displayNameFunction;
    }

    /**
     * Returns the scheduler interval.
     *
     * @return interval in seconds; zero means that this line is disabled
     */
    public int intervalSeconds() {
        return intervalSeconds;
    }

    /**
     * Returns the stable line identifier.
     *
     * @return the line identifier
     */
    @Override
    public String id() {
        return id;
    }

    @Override
    public Component displayName(@Nullable CommandSender sender) {
        return displayNameFunction.apply(sender);
    }

    @Override
    public Material icon() {
        return icon;
    }

    /**
     * Returns the events currently attached to this line.
     *
     * @return an unmodifiable event list
     */
    public List<RandomEvent> events() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Adds an event with a default weight of {@code 1.0} when it has no existing line weight.
     *
     * @param event the event to add
     */
    public void addEvent(RandomEvent event) {
        events.add(event);
        weights.putIfAbsent(event, 1.0D);
    }

    /**
     * Adds a weighted no-op event to this line.
     *
     * <p>The returned event participates in selection exactly like any other event. When it is
     * selected, its empty action list makes the line perform no action.
     *
     * @param id the ID used to identify the empty event
     */
    public RandomEvent addEmptyEvent(String id) {
        RandomEvent event = RandomEvent.empty(id);
        addEvent(event);
        return event;
    }

    /**
     * Removes an event and its line-specific weight.
     *
     * @param event the event to remove
     */
    public void removeEvent(RandomEvent event) {
        events.remove(event);
        weights.remove(event);
    }

    /**
     * Sets the event weight for this line only.
     *
     * @param event an event already attached to this line
     * @param weight a finite, non-negative selection weight; zero disables selection in this line
     * @throws IllegalArgumentException if the event is not attached or the weight is invalid
     */
    public void setWeight(RandomEvent event, double weight) {
        if (!events.contains(event)) {
            throw new IllegalArgumentException("Event is not registered in this line: " + event.id());
        }
        if (weight < 0.0D || !Double.isFinite(weight)) {
            throw new IllegalArgumentException("Weight must be finite and non-negative");
        }
        weights.put(event, weight);
    }

    /**
     * Returns the weight assigned to an event in this line.
     *
     * @param event the event to inspect
     * @return the configured weight, or zero when the event is not attached
     */
    public double weightOf(RandomEvent event) {
        return weights.getOrDefault(event, 0.0D);
    }

    /**
     * Selects one compatible event using the weights stored by this line.
     *
     * @param context the runtime context used for conditions and randomness
     * @return the selected event, or empty when no eligible event has a positive weight
     */
    public Optional<RandomEvent> select(EventContext context) {
        List<RandomEvent> candidates = events.stream()
                .filter(event -> event.isCompatible(context))
                .filter(event -> weightOf(event) > 0.0D)
                .toList();
        double totalWeight = candidates.stream().mapToDouble(this::weightOf).sum();
        if (totalWeight <= 0.0D) {
            return Optional.empty();
        }

        double selected = context.getRandom().nextDouble() * totalWeight;
        for (RandomEvent event : candidates) {
            selected -= weightOf(event);
            if (selected < 0.0D) {
                return Optional.of(event);
            }
        }
        return Optional.empty();
    }

    /**
     * Selects and executes one event.
     *
     * @param context the runtime context passed to action types
     * @return the event that was selected and executed, or empty when no event was eligible
     * @throws IllegalArgumentException if a selected action references an unknown action type
     * @throws RuntimeException if an action type fails during execution
     */
    public Optional<RandomEvent> execute(EventContext context) {
        Optional<RandomEvent> selected = select(context);
        selected.ifPresent(event -> event.actions().forEach(action -> execute(action, context)));
        return selected;
    }

    private void execute(EventAction action, EventContext context) {
        // All actions receive this same context, allowing earlier actions to prepare later ones.
        context.manager().getActionType(action.type()).ifPresentOrElse(
                actionType -> actionType.execute(action, context),
                () -> throwUnknownAction(action));
    }

    private void throwUnknownAction(EventAction action) {
        throw new IllegalArgumentException("Unknown action type: " + action.type());
    }

    /**
     * Checks whether a line should execute at an elapsed second value.
     *
     * @param elapsedSeconds elapsed time in seconds
     * @return {@code true} for a positive exact multiple of the line interval
     */
    public boolean shouldExecute(long elapsedSeconds) {
        return intervalSeconds > 0 && elapsedSeconds > 0 && elapsedSeconds % intervalSeconds == 0;
    }
}
