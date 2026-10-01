package io.github.lijinhong11.mittelrandomevents.builtin;

import io.github.lijinhong11.mittelrandomevents.api.event.EventCondition;
import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import java.util.Map;

/**
 * Conditions backed by optional placeholder provider plugins.
 */
public final class OptionalBuiltInEventCondition {
    private OptionalBuiltInEventCondition() {}

    /**
     * PlaceholderAPI-backed conditions.
     */
    public static final class PlaceholderApi {
        private PlaceholderApi() {}

        /**
         * Matches a PlaceholderAPI value for at least one selected player.
         *
         * @param placeholder the placeholder expression
         * @param expected the expected value
         * @return the condition
         */
        public static EventCondition equals(String placeholder, String expected) {
            return configured(
                    "placeholder_player_equals", placeholder, expected, context -> context.getSelectedPlayers().stream()
                            .map(player -> PlaceholderApiResolver.resolve(placeholder, player, context))
                            .anyMatch(expected::equals));
        }

        /**
         * Matches a server-level PlaceholderAPI value.
         *
         * @param placeholder the placeholder expression
         * @param expected the expected value
         * @return the condition
         */
        public static EventCondition serverEquals(String placeholder, String expected) {
            return configured(
                    "placeholder_server_equals", placeholder, expected, context -> PlaceholderApiResolver.resolve(
                                    placeholder, context)
                            .equals(expected));
        }
    }

    /**
     * MiniPlaceholders-backed conditions for selected players.
     */
    public static final class MiniPlaceholders {
        private MiniPlaceholders() {}

        /**
         * Matches a MiniPlaceholder value for at least one selected player.
         *
         * @param placeholder the MiniMessage placeholder expression
         * @param expected the expected serialized value
         * @return the condition
         */
        public static EventCondition equals(String placeholder, String expected) {
            return configured(
                    "mini_placeholder_player_equals",
                    placeholder,
                    expected,
                    context -> context.getSelectedPlayers().stream()
                            .map(player -> MiniPlaceholdersResolver.resolve(placeholder, player, context))
                            .anyMatch(expected::equals));
        }
    }

    private static EventCondition configured(
            String type, String placeholder, String expected, EventCondition delegate) {
        return new EventCondition() {
            @Override
            public String type() {
                return type;
            }

            @Override
            public Map<String, Object> parameters() {
                return Map.of("placeholder", placeholder, "expected", expected);
            }

            @Override
            public boolean test(EventContext context) {
                return delegate.test(context);
            }
        };
    }
}
