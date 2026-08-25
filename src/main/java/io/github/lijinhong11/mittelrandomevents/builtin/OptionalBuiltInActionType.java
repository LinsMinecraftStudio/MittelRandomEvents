package io.github.lijinhong11.mittelrandomevents.builtin;

import io.github.lijinhong11.mittelrandomevents.api.action.ActionType;
import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;

import java.util.List;

/** Optional built-in action types grouped by their external provider plugin. */
public final class OptionalBuiltInActionType {
    private OptionalBuiltInActionType() {}

    /**
     * Registers only the optional action types whose provider plugins are enabled.
     *
     * @param manager the manager receiving the registrations
     * @param placeholderApiEnabled whether PlaceholderAPI is enabled
     * @param miniPlaceholdersEnabled whether MiniPlaceholders is enabled
     */
    public static void registerAvailable(
            io.github.lijinhong11.mittelrandomevents.api.event.RandomEventManager manager,
            boolean placeholderApiEnabled,
            boolean miniPlaceholdersEnabled) {
        if (placeholderApiEnabled) {
            manager.registerActionType(PlaceholderApi.RESOLVE_PLACEHOLDER);
        }
        if (miniPlaceholdersEnabled) {
            manager.registerActionType(MiniPlaceholders.RESOLVE_PLACEHOLDER);
        }
    }

    /** PlaceholderAPI-backed action types. */
    public enum PlaceholderApi implements ActionType {
        RESOLVE_PLACEHOLDER("resolve_placeholder") {
            @Override
            public void execute(EventAction action, EventContext context) {
                String value = String.valueOf(action.parameters().getOrDefault("value", ""));
                List<String> resolved = context.getSelectedPlayers().stream()
                        .map(player -> PlaceholderApiResolver.resolve(value, player, context))
                        .toList();
                context.set("resolved_values", resolved);
            }
        };

        private final String id;

        PlaceholderApi(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }
    }

    /** MiniPlaceholders-backed action types. */
    public enum MiniPlaceholders implements ActionType {
        RESOLVE_PLACEHOLDER("mini_resolve_placeholder") {
            @Override
            public void execute(EventAction action, EventContext context) {
                String value = String.valueOf(action.parameters().getOrDefault("value", ""));
                List<String> resolved = context.getSelectedPlayers().stream()
                        .map(player -> MiniPlaceholdersResolver.resolve(value, player, context))
                        .toList();
                context.set("resolved_values", resolved);
            }
        };

        private final String id;

        MiniPlaceholders(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }
    }
}
