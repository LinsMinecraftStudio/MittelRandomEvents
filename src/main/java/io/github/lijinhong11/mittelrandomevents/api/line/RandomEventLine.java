package io.github.lijinhong11.mittelrandomevents.api.line;

import io.github.lijinhong11.mittellib.utils.random.WeightedRandomMap;
import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A timed collection of random events.
 *
 * <p>Each execution selects at most one compatible event from this line. The line owns event
 * weights and resolves every selected {@code EventAction} through the manager in the supplied
 * context. Scheduling the line itself is the responsibility of {@code TaskMaker}.
 */
public class RandomEventLine {
    @Getter
    private final @NotNull String id;

    private String cron;
    private final List<RandomEvent> events;
    private final WeightedRandomMap<String> weights = new WeightedRandomMap<>();
    private RandomEvent currentEvent;
    private EventContext currentContext;
    private long currentEventEndsAtMillis;

    @Getter
    @Setter
    private Material icon = Material.PAPER;

    /**
     * Creates an event line without automatic scheduling until a Cron expression is set.
     *
     * @param id the stable line identifier
     * @param events the events initially available to this line
     * @throws IllegalArgumentException if the ID is invalid
     * @throws NullPointerException if the event collection is null
     */
    public RandomEventLine(@NotNull String id, Collection<RandomEvent> events) {
        if (!id.matches("[a-z0-9_-]+")) {
            throw new IllegalArgumentException("Line id must match [a-z0-9_-]+");
        }

        this.id = id;
        this.events = new ArrayList<>(Objects.requireNonNull(events, "events"));
        for (RandomEvent event : this.events) {
            Objects.requireNonNull(event, "events must not contain null");
            if (weights.containsKey(event.id())) {
                throw new IllegalArgumentException("Duplicate event id in line: " + event.id());
            }
            weights.put(event.id(), 1.0D);
        }
    }

    /**
     * Creates a line triggered by a Unix five-field Cron expression.
     *
     * @param id the stable line identifier
     * @param cron the Unix Cron expression, or {@code null} to disable automatic scheduling
     * @param events the events initially available to this line
     */
    public RandomEventLine(@NotNull String id, @Nullable String cron, Collection<RandomEvent> events) {
        this(id, events);
        setCron(cron);
    }

    /** @return the configured Cron expression, or {@code null} when automatic scheduling is disabled */
    public String cron() {
        return cron;
    }

    /**
     * Sets the Unix five-field Cron expression. Setting {@code null} disables Cron scheduling.
     * The expression is validated when the task is scheduled by MittelLib.
     */
    public void setCron(String cron) {
        String expression = cron == null ? null : cron.trim();
        if (expression != null && expression.isBlank()) {
            throw new IllegalArgumentException("Cron expression must not be blank");
        }
        this.cron = expression;
    }

    /**
     * Returns the stable line identifier.
     *
     * @return the line identifier
     */
    public String id() {
        return id;
    }

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
        Objects.requireNonNull(event, "event");
        if (events.stream().anyMatch(existing -> existing.id().equals(event.id()))) {
            throw new IllegalArgumentException("Event id is already registered in this line: " + event.id());
        }
        events.add(event);
        weights.put(event.id(), 1.0D);
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
        weights.remove(event.id());
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
        if (weight == 0.0D) {
            weights.remove(event.id());
        } else {
            weights.put(event.id(), weight);
        }
    }

    /**
     * Returns the weight assigned to an event in this line.
     *
     * @param event the event to inspect
     * @return the configured weight, or zero when the event is not attached
     */
    public double weightOf(RandomEvent event) {
        return weights.getWeight(event.id());
    }

    /**
     * Selects one compatible event using the weights stored by this line.
     *
     * @param context the runtime context used for conditions and randomness
     * @return the selected event, or empty when no eligible event has a positive weight
     */
    public Optional<RandomEvent> select(EventContext context) {
        Objects.requireNonNull(context, "context");
        List<RandomEvent> candidates = events.stream()
                .filter(event -> event.isCompatible(context))
                .filter(event -> weightOf(event) > 0.0D)
                .toList();
        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        WeightedRandomMap<String> candidatesById = new WeightedRandomMap<>();
        for (RandomEvent event : candidates) {
            candidatesById.put(event.id(), weightOf(event));
        }
        String selectedId = candidatesById.randomOne();
        return candidates.stream()
                .filter(event -> event.id().equals(selectedId))
                .findFirst();
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
        Objects.requireNonNull(context, "context");
        long now = System.currentTimeMillis();
        if (currentEvent != null) {
            if (now < currentEventEndsAtMillis) {
                return Optional.of(currentEvent);
            }
            currentContext.resetExecutionControl();
            executeActions(currentEvent.endActions(), currentContext);
            currentEvent = null;
            currentContext = null;
            currentEventEndsAtMillis = 0L;
            return Optional.empty();
        }
        Optional<RandomEvent> selected = select(context);
        selected.ifPresent(event -> {
            currentEvent = event;
            currentContext = context;
            executeActions(event.actions(), context);
            context.resetExecutionControl();
            if (event.durationSeconds() <= 0) {
                executeActions(event.endActions(), context);
                currentEvent = null;
                currentContext = null;
            } else {
                currentEventEndsAtMillis = now + event.durationSeconds() * 1000L;
            }
        });
        return selected;
    }

    public RandomEvent currentEvent() {
        return currentEvent;
    }

    private void executeActions(List<EventAction> actions, EventContext context) {
        for (EventAction action : actions) {
            if (context.isStopped()) {
                break;
            }
            if (context.consumeSkipNext()) {
                continue;
            }
            if (!action.conditions().stream().allMatch(condition -> condition.test(context))) {
                continue;
            }
            execute(action, context);
        }
    }

    private void execute(EventAction action, EventContext context) {
        // All actions receive this same context, allowing earlier actions to prepare later ones.
        context.manager()
                .getActionType(action.type())
                .ifPresentOrElse(actionType -> actionType.execute(action, context), () -> throwUnknownAction(action));
    }

    private void throwUnknownAction(EventAction action) {
        throw new IllegalArgumentException("Unknown action type: " + action.type());
    }
}
