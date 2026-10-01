package io.github.lijinhong11.mittelrandomevents.builtin;

import io.github.lijinhong11.mittelrandomevents.api.event.EventCondition;
import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import java.util.Collection;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Factory methods for conditions shipped with MittelRandomEvents.
 */
public final class BuiltInEventCondition {
    private BuiltInEventCondition() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates a condition requiring at least the given number of selected players.
     *
     * @param minimum the minimum player count
     * @return the player count condition
     * @throws IllegalArgumentException if minimum is negative
     */
    public static EventCondition minimumPlayers(int minimum) {
        return players(minimum, Integer.MAX_VALUE);
    }

    /**
     * Creates a condition requiring the selected player count to be within an inclusive range.
     *
     * @param minimum the minimum selected player count
     * @param maximum the maximum selected player count
     * @return the player count condition
     * @throws IllegalArgumentException if the range is invalid
     */
    public static EventCondition players(int minimum, int maximum) {
        validateRange(minimum, maximum, "players");
        return configured(
                "players",
                Map.of("min", minimum, "max", maximum),
                context -> inRange(context.getSelectedPlayers().size(), minimum, maximum));
    }

    /**
     * Creates a condition requiring at least the given number of selected worlds.
     *
     * @param minimum the minimum world count
     * @return the world count condition
     * @throws IllegalArgumentException if minimum is negative
     */
    public static EventCondition minimumWorlds(int minimum) {
        return worlds(minimum, Integer.MAX_VALUE);
    }

    /**
     * Creates a condition requiring the selected world count to be within an inclusive range.
     *
     * @param minimum the minimum selected world count
     * @param maximum the maximum selected world count
     * @return the world count condition
     * @throws IllegalArgumentException if the range is invalid
     */
    public static EventCondition worlds(int minimum, int maximum) {
        validateRange(minimum, maximum, "worlds");
        return configured(
                "worlds",
                Map.of("min", minimum, "max", maximum),
                context -> inRange(context.getSelectedWorlds().size(), minimum, maximum));
    }

    /**
     * Creates a condition requiring at least one selected player with a permission.
     *
     * @param permission the permission node
     * @return the permission condition
     * @throws IllegalArgumentException if permission is null or blank
     */
    public static EventCondition playerPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            throw new IllegalArgumentException("Permission must not be blank");
        }
        return configured(
                "player_permission", Map.of("permission", permission), context -> context.getSelectedPlayers().stream()
                        .anyMatch(player -> player.hasPermission(permission)));
    }

    /**
     * Creates a condition requiring a selected world with the given name.
     *
     * @param worldName the world name
     * @return the world condition
     * @throws IllegalArgumentException if worldName is null or blank
     */
    public static EventCondition world(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            throw new IllegalArgumentException("World name must not be blank");
        }
        return configured("world", Map.of("name", worldName), context -> context.getSelectedWorlds().stream()
                .map(World::getName)
                .anyMatch(worldName::equals));
    }

    /**
     * Creates a condition requiring a selected player's name to contain text.
     */
    public static EventCondition playerNameContains(String text) {
        requireText(text, "Player name text");
        return configured(
                "player_name_contains", Map.of("value", text), context -> context.getSelectedPlayers().stream()
                        .anyMatch(player -> player.getName().contains(text)));
    }

    /**
     * Creates a condition requiring a selected world's name to contain text.
     *
     * @param text text to find in a world name
     * @return the world name condition
     */
    public static EventCondition worldNameContains(String text) {
        requireText(text, "World name text");
        return configured("world_name_contains", Map.of("value", text), context -> context.getSelectedWorlds().stream()
                .anyMatch(world -> world.getName().contains(text)));
    }

    /**
     * Creates a condition requiring at least a number of online players on the server.
     *
     * @param minimum minimum online player count
     * @return the server player count condition
     */
    public static EventCondition serverOnlinePlayersAtLeast(int minimum) {
        return serverOnlinePlayers(minimum, Integer.MAX_VALUE);
    }

    /**
     * Creates a condition requiring the server online player count to be within an inclusive range.
     *
     * @param minimum the minimum online player count
     * @param maximum the maximum online player count
     * @return the server player count condition
     * @throws IllegalArgumentException if the range is invalid
     */
    public static EventCondition serverOnlinePlayers(int minimum, int maximum) {
        validateRange(minimum, maximum, "online players");
        return configured(
                "server_online_players",
                Map.of("min", minimum, "max", maximum),
                context -> inRange(Bukkit.getOnlinePlayers().size(), minimum, maximum));
    }

    /**
     * Creates a condition requiring no more than the given number of online players.
     *
     * @param maximum maximum online player count
     * @return the server player count condition
     * @throws IllegalArgumentException if maximum is negative
     */
    public static EventCondition serverOnlinePlayersAtMost(int maximum) {
        return serverOnlinePlayers(0, maximum);
    }

    /**
     * Creates a condition requiring the server to have a plugin enabled.
     *
     * @param pluginName plugin name
     * @return the plugin state condition
     */
    public static EventCondition serverPluginEnabled(String pluginName) {
        requireText(pluginName, "Plugin name");
        return configured("server_plugin_enabled", Map.of("name", pluginName), context -> Bukkit.getPluginManager()
                .isPluginEnabled(pluginName));
    }

    /**
     * Creates a condition requiring a plugin to be absent or disabled.
     *
     * @param pluginName plugin name
     * @return the plugin state condition
     */
    public static EventCondition serverPluginDisabled(String pluginName) {
        requireText(pluginName, "Plugin name");
        return configured("server_plugin_disabled", Map.of("name", pluginName), context -> !Bukkit.getPluginManager()
                .isPluginEnabled(pluginName));
    }

    /**
     * Creates a condition matching text in the server version string.
     *
     * @param text text to find in the server version
     * @return the server version condition
     */
    public static EventCondition serverVersionContains(String text) {
        requireText(text, "Server version text");
        return configured("server_version_contains", Map.of("value", text), context -> Bukkit.getVersion()
                .contains(text));
    }

    /**
     * Creates a condition requiring the server TPS to be at least the given value.
     *
     * @param minimum minimum TPS value
     * @return the TPS condition
     */
    public static EventCondition serverTpsAtLeast(double minimum) {
        if (!Double.isFinite(minimum) || minimum < 0.0D) {
            throw new IllegalArgumentException("Minimum TPS must be finite and non-negative");
        }
        return configured(
                "server_tps",
                Map.of("min", minimum),
                context -> Bukkit.getServer().getTPS()[0] >= minimum);
    }

    /**
     * Combines conditions using logical AND.
     *
     * @param conditions conditions that must all pass
     * @return the combined condition
     */
    public static EventCondition allOf(Collection<? extends EventCondition> conditions) {
        java.util.List<EventCondition> copied = java.util.List.copyOf(conditions);
        return configured("all_of", compositeParameters(copied), context -> copied.stream()
                .allMatch(condition -> condition.test(context)));
    }

    /**
     * Combines conditions using logical OR.
     *
     * @param conditions conditions where at least one must pass
     * @return the combined condition
     */
    public static EventCondition anyOf(Collection<? extends EventCondition> conditions) {
        java.util.List<EventCondition> copied = java.util.List.copyOf(conditions);
        return configured("any_of", compositeParameters(copied), context -> copied.stream()
                .anyMatch(condition -> condition.test(context)));
    }

    /**
     * Negates a condition.
     *
     * @param condition the condition to negate
     * @return the negated condition
     */
    public static EventCondition not(EventCondition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition must not be null");
        }
        Map<String, Object> parameters = new java.util.LinkedHashMap<>();
        parameters.put("type", condition.type());
        parameters.put("parameters", condition.parameters());
        return configured("not", parameters, context -> !condition.test(context));
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private static void validateRange(int minimum, int maximum, String name) {
        if (minimum < 0 || maximum < 0 || minimum > maximum) {
            throw new IllegalArgumentException("Invalid " + name + " range: " + minimum + "-" + maximum);
        }
    }

    private static boolean inRange(int value, int minimum, int maximum) {
        return value >= minimum && value <= maximum;
    }

    private static EventCondition configured(String type, Map<String, Object> parameters, EventCondition delegate) {
        return new EventCondition() {
            @Override
            public boolean test(EventContext context) {
                return delegate.test(context);
            }

            @Override
            public String type() {
                return type;
            }

            @Override
            public Map<String, Object> parameters() {
                return parameters;
            }
        };
    }

    private static Map<String, Object> compositeParameters(Collection<? extends EventCondition> conditions) {
        return Map.of(
                "conditions",
                conditions.stream()
                        .map(condition -> {
                            if (condition.type() == null || condition.type().isBlank()) {
                                throw new IllegalArgumentException("Nested condition is not serializable");
                            }
                            Map<String, Object> values = new java.util.LinkedHashMap<>();
                            values.put("type", condition.type());
                            values.put("parameters", condition.parameters());
                            return values;
                        })
                        .toList());
    }
}
