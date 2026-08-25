package io.github.lijinhong11.mittelrandomevents.builtin;

import io.github.lijinhong11.mittelrandomevents.api.action.ActionType;
import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Built-in action types shipped with MittelRandomEvents.
 *
 * <p>Every constant is registered by the plugin during startup. External plugins can add their
 * own {@link io.github.lijinhong11.mittelrandomevents.api.action.ActionType} implementations to
 * the same registry without modifying this enum.
 */
public enum BuiltInActionType implements ActionType {
    /** Selects online players, optionally filtered by permission and world. */
    SELECT_PLAYERS("select_players") {
        @Override
        public void execute(EventAction action, EventContext context) {
            String permission = stringParameter(action, "permission", "");
            String worldName = stringParameter(action, "world", "");

            List<Player> players = Bukkit.getOnlinePlayers().stream()
                    .filter(player -> permission.isBlank()
                            || player.hasPermission(permission))
                    .filter(player -> worldName.isBlank()
                            || player.getWorld().getName().equals(worldName))
                    .collect(Collectors.toCollection(ArrayList::new));
            shuffle(players, context);
            limit(players, amountParameter(action));
            context.setSelectedPlayers(players);
        }
    },

    /** Selects loaded worlds, optionally filtering by world name. */
    SELECT_WORLDS("select_worlds") {
        @Override
        public void execute(EventAction action, EventContext context) {
            String worldName = stringParameter(action, "world", "");
            List<World> worlds = worldName.isBlank()
                    ? new ArrayList<>(Bukkit.getWorlds())
                    : Bukkit.getWorlds().stream()
                            .filter(world -> world.getName().equals(worldName))
                            .collect(Collectors.toCollection(ArrayList::new));
            shuffle(worlds, context);
            limit(worlds, amountParameter(action));
            context.setSelectedWorlds(worlds);
        }
    },

    /** Removes all currently selected players from the context. */
    CLEAR_PLAYERS("clear_players") {
        @Override
        public void execute(EventAction action, EventContext context) {
            context.setSelectedPlayers(List.of());
        }
    },

    /** Removes all currently selected worlds from the context. */
    CLEAR_WORLDS("clear_worlds") {
        @Override
        public void execute(EventAction action, EventContext context) {
            context.setSelectedWorlds(List.of());
        }
    };

    private final String id;

    /**
     * Creates a built-in action type.
     *
     * @param id the configuration identifier of this action type
     */
    BuiltInActionType(String id) {
        this.id = id;
    }

    /**
     * Returns the identifier used in {@link EventAction} configuration.
     *
     * @return the built-in action identifier
     */
    @Override
    public String id() {
        return id;
    }

    private static String stringParameter(EventAction action, String key, String defaultValue) {
        Object value = action.parameters().get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private static int amountParameter(EventAction action) {
        Object value = action.parameters().get("amount");
        if (!(value instanceof Number number)) {
            return 0;
        }
        return Math.max(0, number.intValue());
    }

    private static <T> void shuffle(List<T> values, EventContext context) {
        Collections.shuffle(values, context.getRandom());
    }

    private static <T> void limit(List<T> values, int amount) {
        if (amount > 0 && values.size() > amount) {
            values.subList(amount, values.size()).clear();
        }
    }
}
