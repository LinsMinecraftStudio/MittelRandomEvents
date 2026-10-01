package io.github.lijinhong11.mittelrandomevents.api.event;

import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

/**
 * A predicate that determines whether an event may be selected in a runtime context.
 *
 * <p>Conditions are evaluated by {@code RandomEventLine} before weight selection. A condition
 * should not execute the event or mutate unrelated server state.
 */
@FunctionalInterface
public interface EventCondition {
    /**
     * Returns the serialized condition type.
     *
     * @return the type, or {@code null} for a runtime-only condition
     */
    default String type() {
        return null;
    }

    /**
     * @return the direct display name component
     */
    default Component displayName() {
        return Component.text(type() == null ? "condition" : type());
    }

    /**
     * @return the direct description component
     */
    default Component description() {
        return Component.empty();
    }

    /**
     * @return the material used as this condition's GUI icon
     */
    default Material icon() {
        return Material.COMPARATOR;
    }

    /**
     * Returns the parameter names supported by this condition.
     *
     * @return supported parameter names
     */
    default java.util.Collection<String> parameterNames() {
        return java.util.List.of();
    }

    /**
     * Returns the serialized condition parameters.
     *
     * @return immutable condition parameters
     */
    default Map<String, Object> parameters() {
        return Map.of();
    }
    /**
     * Tests this condition against the current event context.
     *
     * @param context the context used for the condition check
     * @return {@code true} when the event is eligible for selection
     */
    boolean test(EventContext context);
}
