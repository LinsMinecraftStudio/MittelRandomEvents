package io.github.lijinhong11.mittelrandomevents.api;

import io.github.lijinhong11.mittelrandomevents.MittelRandomEvents;
import io.github.lijinhong11.mittelrandomevents.api.action.ActionType;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLine;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLineManager;
import io.github.lijinhong11.mittelrandomevents.context.DefaultEventContext;

import java.util.Optional;
import java.util.function.Supplier;
import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;

/**
 * Stable convenience API for integrations with other plugins.
 *
 * <p>For example, an external plugin can register its own action and event line without accessing
 * data managers or Paper scheduler details:
 *
 * <pre>{@code
 * RandomEventsAPI.registerActionType(myAction);
 * RandomEventsAPI.registerEvent(myEvent);
 * RandomEventsAPI.registerLine(myLine);
 * }</pre>
 *
 * <p>All methods must be called after MittelRandomEvents has enabled. The registered line is
 * immediately scheduled unless its interval is zero.
 */
public final class MittelRandomEventsAPI {
    private MittelRandomEventsAPI() {}

    /**
     * Returns the currently enabled plugin instance.
     *
     * @return the plugin instance
     * @throws IllegalStateException if MittelRandomEvents is not enabled
     */
    private static MittelRandomEvents plugin() {
        MittelRandomEvents plugin = MittelRandomEvents.getInstance();
        if (plugin == null || !plugin.isEnabled()) {
            throw new IllegalStateException("MittelRandomEvents is not enabled");
        }
        return plugin;
    }

    /**
     * Registers an action type provided by another plugin.
     *
     * @param actionType the action type to register
     */
    public static void registerActionType(ActionType actionType) {
        plugin().getEventManager().registerActionType(actionType);
    }

    /**
     * Removes an externally registered action type.
     *
     * @param id the action type identifier
     */
    public static void unregisterActionType(String id) {
        plugin().getEventManager().unregisterActionType(id);
    }

    /**
     * Registers an event for use by event lines.
     *
     * @param event the event to register
     */
    public static void registerEvent(RandomEvent event) {
        plugin().getEventManager().register(event);
    }

    /**
     * Removes an event from the event registry.
     *
     * <p>Existing lines keep their object reference until they are rebuilt or reloaded.
     *
     * @param id the event identifier
     */
    public static void unregisterEvent(String id) {
        plugin().getEventManager().unregister(id);
    }

    /**
     * Finds an event by ID.
     *
     * @param id the event identifier
     * @return the event when registered
     */
    public static Optional<RandomEvent> event(String id) {
        return plugin().getEventManager().get(id);
    }

    /**
     * Registers and starts an event line using the default event context.
     *
     * @param line the line to register and schedule
     */
    public static void registerLine(RandomEventLine line) {
        registerLine(line, () -> new DefaultEventContext(plugin().getEventManager()));
    }

    /**
     * Registers and starts an event line with a custom context factory.
     *
     * <p>This overload is intended for integrations that need to provide custom initial context
     * data. The supplier is called once for every scheduled line execution.
     *
     * @param line the line to register and schedule
     * @param contextSupplier the factory for execution contexts
     */
    public static void registerLine(
            RandomEventLine line, Supplier<? extends EventContext> contextSupplier) {
        MittelRandomEvents plugin = plugin();
        RandomEventLineManager manager = plugin.getLineManager();
        manager.get(line.id()).ifPresent(existing -> plugin.getTaskMaker().cancelLine(existing.id()));
        manager.register(line);
        plugin.getTaskMaker().startLine(line, contextSupplier);
    }

    /**
     * Stops and removes an event line.
     *
     * @param id the line identifier
     */
    public static void unregisterLine(String id) {
        MittelRandomEvents plugin = plugin();
        plugin.getTaskMaker().cancelLine(id);
        plugin.getLineManager().unregister(id);
    }

    /**
     * Finds an event line by ID.
     *
     * @param id the line identifier
     * @return the line when registered
     */
    public static Optional<RandomEventLine> line(String id) {
        return plugin().getLineManager().get(id);
    }

    /**
     * Reloads both YAML data files and restarts all configured lines.
     */
    public static void reload() {
        plugin().reloadData();
    }
}
