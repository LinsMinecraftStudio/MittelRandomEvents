package io.github.lijinhong11.mittelrandomevents.api.line;

import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittellib.utils.random.WeightedRandomMap;
import io.github.lijinhong11.mittelrandomevents.api.Localized;
import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
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

    private int intervalSeconds;
    private String cron;
    private final List<RandomEvent> events;
    private final WeightedRandomMap<String> weights = new WeightedRandomMap<>();
    private RandomEvent currentEvent;
    private EventContext currentContext;
    private long currentEventEndsAtMillis;
    private long nextExecutionAtMillis;

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
        if (!id.matches("[a-z0-9_-]+")) {
            throw new IllegalArgumentException("Line id must match [a-z0-9_-]+");
        }

        if (intervalSeconds < 0) {
            throw new IllegalArgumentException("Interval must not be negative");
        }

        this.id = id;
        this.displayNameFunction = sender -> ComponentUtils.text(id);
        this.intervalSeconds = intervalSeconds;
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
     * @param cron the Unix Cron expression
     * @param events the events initially available to this line
     */
    public RandomEventLine(@NotNull String id, @NotNull String cron, Collection<RandomEvent> events) {
        this(id, 0, events);
        setCron(cron);
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
            String id,
            @NotNull Function<@Nullable CommandSender, Component> displayNameFunction,
            int intervalSeconds,
            Collection<RandomEvent> events) {
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

    /** Updates the fixed interval used when this line has no Cron expression. */
    public void setIntervalSeconds(int intervalSeconds) {
        if (intervalSeconds < 0) {
            throw new IllegalArgumentException("Interval must not be negative");
        }
        this.intervalSeconds = intervalSeconds;
    }

    /** @return the configured Cron expression, or {@code null} for interval scheduling */
    public String cron() {
        return cron;
    }

    /**
     * Sets the Unix five-field Cron expression. Setting {@code null} disables Cron scheduling.
     * The expression is validated when the task is scheduled by MittelLib.
     */
    public void setCron(String cron) {
        this.cron = cron == null ? null : Objects.requireNonNull(cron, "cron").trim();
        if (this.cron != null && this.cron.isBlank()) {
            throw new IllegalArgumentException("Cron expression must not be blank");
        }
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
        if (intervalSeconds > 0) {
            nextExecutionAtMillis = now + lineIntervalMillis();
        }
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

    public long remainingNextEventSeconds() {
        if (nextExecutionAtMillis <= 0L) return 0L;
        return Math.max(0L, (nextExecutionAtMillis - System.currentTimeMillis() + 999L) / 1000L);
    }

    private long lineIntervalMillis() {
        return intervalSeconds > 0 ? intervalSeconds * 1000L : 0L;
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
