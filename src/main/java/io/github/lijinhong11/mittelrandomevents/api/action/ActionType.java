package io.github.lijinhong11.mittelrandomevents.api.action;

import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

/**
 * Describes one executable action type.
 *
 * <p>An action type is the extension point used by other plugins to add their own event
 * behavior. The type does not belong to one specific {@code RandomEvent}; it is registered once
 * and can then be referenced by many {@link EventAction} instances.
 *
 * <p>Implementations should keep {@link #id()} stable. Configuration files refer to this value,
 * so changing it is a breaking configuration change.
 *
 * <p>Actions belonging to one event are executed in their configured order. Every action receives
 * the same {@link EventContext} instance, so an action may update the context for later actions.
 * For example, a {@code select_players} action may call
 * {@link EventContext#setSelectedPlayers(java.util.Collection)}, after which a following action
 * can use {@link EventContext#getSelectedPlayers()}.
 */
public interface ActionType {
    /**
     * Returns the unique identifier used to find this action type in the action type registry.
     *
     * @return the non-blank action type identifier
     */
    String id();

    /** @return the direct display name component */
    default Component displayName() { return Component.text(id()); }

    /** @return the direct description component */
    default Component description() { return Component.empty(); }

    /** @return the material used as this action type's GUI icon */
    default Material icon() { return Material.COMMAND_BLOCK; }

    /**
     * Returns the parameter names supported by this action type.
     *
     * <p>This metadata is intended for configuration UIs and validation. It does not replace
     * runtime validation inside {@link #execute(EventAction, EventContext)}.
     *
     * @return supported parameter names
     */
    default java.util.Collection<String> parameterNames() {
        return java.util.List.of();
    }

    /**
     * Executes one configured action.
     *
     * <p>The {@code action} contains the type identifier and arbitrary configuration parameters.
     * The {@code context} provides the runtime environment of the event, including selected
     * players, selected worlds, shared data and the event manager.
     *
     * @param action the configured action instance to execute
     * @param context the runtime context in which the action is executed
     * @throws RuntimeException if the action cannot be executed
     */
    void execute(EventAction action, EventContext context);
}
