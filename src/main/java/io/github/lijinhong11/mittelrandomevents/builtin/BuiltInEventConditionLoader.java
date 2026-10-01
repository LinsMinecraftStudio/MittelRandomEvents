package io.github.lijinhong11.mittelrandomevents.builtin;

import io.github.lijinhong11.mittelrandomevents.api.event.EventCondition;
import java.util.Map;
import org.bukkit.Bukkit;

/**
 * Deserializes built-in event conditions from YAML data.
 */
public final class BuiltInEventConditionLoader {
    private BuiltInEventConditionLoader() {}

    /**
     * Loads one built-in condition.
     *
     * @param type the condition type
     * @param parameters serialized parameters
     * @return the condition, or {@code null} when the type is unknown
     */
    public static EventCondition load(String type, Map<String, Object> parameters) {
        return switch (type) {
            case "players" ->
                BuiltInEventCondition.players(
                        integer(parameters, "min", 0), integer(parameters, "max", Integer.MAX_VALUE));
            case "worlds" ->
                BuiltInEventCondition.worlds(
                        integer(parameters, "min", 0), integer(parameters, "max", Integer.MAX_VALUE));
            case "player_permission" -> BuiltInEventCondition.playerPermission(string(parameters, "permission"));
            case "world" -> BuiltInEventCondition.world(string(parameters, "name"));
            case "player_name_contains" -> BuiltInEventCondition.playerNameContains(string(parameters, "value"));
            case "world_name_contains" -> BuiltInEventCondition.worldNameContains(string(parameters, "value"));
            case "server_online_players" ->
                BuiltInEventCondition.serverOnlinePlayers(
                        integer(parameters, "min", 0), integer(parameters, "max", Integer.MAX_VALUE));
            case "server_plugin_enabled" -> BuiltInEventCondition.serverPluginEnabled(string(parameters, "name"));
            case "server_plugin_disabled" -> BuiltInEventCondition.serverPluginDisabled(string(parameters, "name"));
            case "server_version_contains" -> BuiltInEventCondition.serverVersionContains(string(parameters, "value"));
            case "server_tps" -> BuiltInEventCondition.serverTpsAtLeast(decimal(parameters, "min", 0.0D));
            case "placeholder_player_equals" -> placeholderApi(parameters, false);
            case "placeholder_server_equals" -> placeholderApi(parameters, true);
            case "mini_placeholder_player_equals" -> miniPlaceholders(parameters);
            default -> null;
        };
    }

    private static EventCondition placeholderApi(Map<String, Object> parameters, boolean server) {
        requirePlugin("PlaceholderAPI");
        String placeholder = string(parameters, "placeholder");
        String expected = string(parameters, "expected");
        return server
                ? OptionalBuiltInEventCondition.PlaceholderApi.serverEquals(placeholder, expected)
                : OptionalBuiltInEventCondition.PlaceholderApi.equals(placeholder, expected);
    }

    private static EventCondition miniPlaceholders(Map<String, Object> parameters) {
        requirePlugin("MiniPlaceholders");
        return OptionalBuiltInEventCondition.MiniPlaceholders.equals(
                string(parameters, "placeholder"), string(parameters, "expected"));
    }

    private static void requirePlugin(String name) {
        if (!Bukkit.getPluginManager().isPluginEnabled(name)) {
            throw new IllegalArgumentException("Condition requires plugin: " + name);
        }
    }

    private static String string(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException("Missing condition parameter: " + key);
        }
        return String.valueOf(value);
    }

    private static int integer(Map<String, Object> parameters, String key, int defaultValue) {
        Object value = parameters.get(key);
        return value instanceof Number number ? number.intValue() : defaultValue;
    }

    private static double decimal(Map<String, Object> parameters, String key, double defaultValue) {
        Object value = parameters.get(key);
        return value instanceof Number number ? number.doubleValue() : defaultValue;
    }
}
