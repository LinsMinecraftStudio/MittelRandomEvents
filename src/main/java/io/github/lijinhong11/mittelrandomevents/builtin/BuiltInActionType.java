package io.github.lijinhong11.mittelrandomevents.builtin;

import io.github.lijinhong11.mittellib.utils.StringUtils;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittelrandomevents.MittelRandomEvents;
import io.github.lijinhong11.mittelrandomevents.api.action.ActionType;
import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import io.github.lijinhong11.mittelrandomevents.utils.RegistryUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Built-in action types shipped with MittelRandomEvents.
 *
 * <p>Every constant is registered by the plugin during startup. External plugins can add their
 * own {@link io.github.lijinhong11.mittelrandomevents.api.action.ActionType} implementations to
 * the same registry without modifying this enum.
 */
public enum BuiltInActionType implements ActionType {
    /**
     * Selects online players, optionally filtered by permission and world.
     */
    SELECT_PLAYERS("select_players") {
        @Override
        public void execute(EventAction action, EventContext context) {
            String permission = stringParameter(action, "permission", "", context, null);
            String worldName = stringParameter(action, "world", "", context, null);

            List<Player> players = Bukkit.getOnlinePlayers().stream()
                    .filter(player -> permission.isBlank() || player.hasPermission(permission))
                    .filter(player ->
                            worldName.isBlank() || player.getWorld().getName().equals(worldName))
                    .collect(Collectors.toCollection(ArrayList::new));
            shuffle(players, context);
            limit(players, amountParameter(action, context));
            context.setSelectedPlayers(players);
        }
    },

    /**
     * Selects loaded worlds, optionally filtering by world name.
     */
    SELECT_WORLDS("select_worlds") {
        @Override
        public void execute(EventAction action, EventContext context) {
            String worldName = stringParameter(action, "world", "", context, null);
            List<World> worlds = worldName.isBlank()
                    ? new ArrayList<>(Bukkit.getWorlds())
                    : Bukkit.getWorlds().stream()
                            .filter(world -> world.getName().equals(worldName))
                            .collect(Collectors.toCollection(ArrayList::new));
            shuffle(worlds, context);
            limit(worlds, amountParameter(action, context));
            context.setSelectedWorlds(worlds);
        }
    },

    /**
     * Removes all currently selected players from the context.
     */
    CLEAR_PLAYERS("clear_players") {
        @Override
        public void execute(EventAction action, EventContext context) {
            context.setSelectedPlayers(List.of());
        }
    },

    /**
     * Removes all currently selected worlds from the context.
     */
    CLEAR_WORLDS("clear_worlds") {
        @Override
        public void execute(EventAction action, EventContext context) {
            context.setSelectedWorlds(List.of());
        }
    },

    /** Sends configured message text to the currently selected players. */
    MESSAGE_PLAYERS("message_players") {
        @Override
        public void execute(EventAction action, EventContext context) {
            context.getSelectedPlayers()
                    .forEach(player -> onEntityAndWait(
                            player,
                            selected -> selected.sendMessage(ComponentUtils.deserialize(
                                    stringParameter(action, "message", "", context, selected)))));
        }
    },

    /** Broadcasts configured message text to the server. */
    BROADCAST("broadcast") {
        @Override
        public void execute(EventAction action, EventContext context) {
            Bukkit.broadcast(ComponentUtils.deserialize(stringParameter(action, "message", "", context, null)));
        }
    },

    /** Executes a command as the console sender. */
    EXECUTE_CONSOLE_COMMAND("execute_console_command") {
        @Override
        public void execute(EventAction action, EventContext context) {
            if (booleanParameter(action, "runForEachPlayer", false, context, null)) {
                context.getSelectedPlayers()
                        .forEach(player -> Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                interpolateCommand(
                                        stringParameter(action, "command", "", context, player),
                                        action,
                                        context,
                                        player)));
                return;
            }
            String command = stringParameter(action, "command", "", context, null);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), interpolateCommand(command, action, context, null));
        }
    },

    /** Executes a command once for every selected player. */
    EXECUTE_PLAYER_COMMAND("execute_player_command") {
        @Override
        public void execute(EventAction action, EventContext context) {
            context.getSelectedPlayers()
                    .forEach(player -> onEntityAndWait(
                            player,
                            selected -> selected.performCommand(
                                    stringParameter(action, "command", "", context, selected))));
        }
    },

    /** Heals every selected player by the configured amount. */
    HEAL_PLAYERS("heal_players") {
        @Override
        public void execute(EventAction action, EventContext context) {
            double amount = doubleParameter(action, "amount", 0.0D, context, null);
            context.getSelectedPlayers()
                    .forEach(player -> onEntityAndWait(
                            player,
                            selected -> selected.setHealth(
                                    Math.min(selected.getMaxHealth(), selected.getHealth() + Math.max(0.0D, amount)))));
        }
    },

    /** Damages every selected player by the configured amount. */
    DAMAGE_PLAYERS("damage_players") {
        @Override
        public void execute(EventAction action, EventContext context) {
            double amount = Math.max(0.0D, doubleParameter(action, "amount", 0.0D, context, null));
            context.getSelectedPlayers()
                    .forEach(player -> onEntityAndWait(player, selected -> selected.damage(amount)));
        }
    },

    /** Clears the inventories of every selected player. */
    CLEAR_INVENTORIES("clear_inventories") {
        @Override
        public void execute(EventAction action, EventContext context) {
            context.getSelectedPlayers()
                    .forEach(player -> onEntityAndWait(
                            player, selected -> selected.getInventory().clear()));
        }
    },

    /** Plays a Bukkit sound at every selected player's location. */
    PLAY_SOUND("play_sound") {
        @Override
        public void execute(EventAction action, EventContext context) {
            Sound sound = soundParameter(action, context);
            float volume = (float) doubleParameter(action, "volume", 1.0D, context, null);
            float pitch = (float) doubleParameter(action, "pitch", 1.0D, context, null);
            context.getSelectedPlayers()
                    .forEach(player -> onEntityAndWait(
                            player, selected -> selected.playSound(selected.getLocation(), sound, volume, pitch)));
        }
    },

    /** Applies storm and thunder settings to every selected world. */
    SET_WEATHER("set_weather") {
        @Override
        public void execute(EventAction action, EventContext context) {
            boolean storm = booleanParameter(action, "storm", false, context, null);
            boolean thunder = booleanParameter(action, "thunder", false, context, null);
            context.getSelectedWorlds().forEach(world -> {
                world.setStorm(storm);
                world.setThundering(thunder);
            });
        }
    },

    /** Stops all actions after this action in the current event. */
    STOP_ACTIONS("stop_actions") {
        @Override
        public void execute(EventAction action, EventContext context) {
            context.stop();
        }
    },

    /** Skips the action immediately following this action. */
    SKIP_NEXT_ACTION("skip_next_action") {
        @Override
        public void execute(EventAction action, EventContext context) {
            context.skipNext();
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

    @Override
    public Component displayName() {
        return Component.text(displayNameText());
    }

    @Override
    public List<Component> lore() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(descriptionText()));
        if (!parameterNames().isEmpty()) {
            lines.add(Component.text("Parameters: " + String.join(", ", parameterNames())));
        }
        return List.copyOf(lines);
    }

    private String displayNameText() {
        return switch (this) {
            case SELECT_PLAYERS -> "Select Players";
            case SELECT_WORLDS -> "Select Worlds";
            case CLEAR_PLAYERS -> "Clear Players";
            case CLEAR_WORLDS -> "Clear Worlds";
            case MESSAGE_PLAYERS -> "Message Players";
            case BROADCAST -> "Broadcast";
            case EXECUTE_CONSOLE_COMMAND -> "Console Command";
            case EXECUTE_PLAYER_COMMAND -> "Player Command";
            case HEAL_PLAYERS -> "Heal Players";
            case DAMAGE_PLAYERS -> "Damage Players";
            case CLEAR_INVENTORIES -> "Clear Inventories";
            case PLAY_SOUND -> "Play Sound";
            case SET_WEATHER -> "Set Weather";
            case STOP_ACTIONS -> "Stop Actions";
            case SKIP_NEXT_ACTION -> "Skip Next Action";
        };
    }

    private String descriptionText() {
        return switch (this) {
            case SELECT_PLAYERS -> "Selects online players.";
            case SELECT_WORLDS -> "Selects loaded worlds.";
            case CLEAR_PLAYERS -> "Clears selected players.";
            case CLEAR_WORLDS -> "Clears selected worlds.";
            case MESSAGE_PLAYERS -> "Sends a message to selected players.";
            case BROADCAST -> "Broadcasts a message to the server.";
            case EXECUTE_CONSOLE_COMMAND -> "Executes a command as the console.";
            case EXECUTE_PLAYER_COMMAND -> "Executes a command as selected players.";
            case HEAL_PLAYERS -> "Heals selected players.";
            case DAMAGE_PLAYERS -> "Damages selected players.";
            case CLEAR_INVENTORIES -> "Clears selected player inventories.";
            case PLAY_SOUND -> "Plays a sound for selected players.";
            case SET_WEATHER -> "Changes weather in selected worlds.";
            case STOP_ACTIONS -> "Stops the remaining actions in this event.";
            case SKIP_NEXT_ACTION -> "Skips the next action in this event.";
        };
    }

    @Override
    public Collection<String> parameterNames() {
        return switch (this) {
            case SELECT_PLAYERS -> List.of("permission", "world", "amount");
            case SELECT_WORLDS -> List.of("world", "amount");
            case MESSAGE_PLAYERS, BROADCAST -> List.of("message");
            case EXECUTE_CONSOLE_COMMAND -> List.of("command", "runForEachPlayer");
            case EXECUTE_PLAYER_COMMAND -> List.of("command");
            case HEAL_PLAYERS, DAMAGE_PLAYERS -> List.of("amount");
            case PLAY_SOUND -> List.of("sound", "volume", "pitch");
            case SET_WEATHER -> List.of("storm", "thunder");
            default -> List.of();
        };
    }

    private static String stringParameter(
            EventAction action, String key, String defaultValue, EventContext context, Player player) {
        Object value = action.parameters().get(key);
        return value == null ? defaultValue : StringUtils.parsePlaceholders(player, String.valueOf(value));
    }

    private static int amountParameter(EventAction action, EventContext context) {
        Object value = action.parameters().get("amount");
        if (value instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        if (value == null) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(StringUtils.parsePlaceholders(String.valueOf(value))));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static double doubleParameter(
            EventAction action, String key, double defaultValue, EventContext context, Player player) {
        Object value = action.parameters().get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null
                    ? defaultValue
                    : Double.parseDouble(StringUtils.parsePlaceholders(player, String.valueOf(value)));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static boolean booleanParameter(
            EventAction action, String key, boolean defaultValue, EventContext context, Player player) {
        Object value = action.parameters().get(key);
        return value == null
                ? defaultValue
                : Boolean.parseBoolean(StringUtils.parsePlaceholders(player, String.valueOf(value)));
    }

    /**
     * Expands action/context parameters in a console command. Supported forms are {@code {name}},
     * {@code ${name}} and {@code %name%}. When a player is supplied, common player values are also
     * available as {@code playerName}, {@code playerUuid}, {@code playerWorld}, {@code playerX},
     * {@code playerY} and {@code playerZ}.
     */
    private static String interpolateCommand(String command, EventAction action, EventContext context, Player player) {
        Map<String, Object> variables = new LinkedHashMap<>(context.parameters().asMap());
        variables.putAll(action.parameters().asMap());
        if (player != null) {
            variables.put("player", player.getName());
            variables.put("playerUuid", player.getUniqueId());
            variables.put("playerWorld", player.getWorld().getName());
            variables.put("playerX", player.getX());
            variables.put("playerY", player.getY());
            variables.put("playerZ", player.getZ());
        }

        String result = command;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String value = String.valueOf(entry.getValue());
            result = result.replace("%" + entry.getKey() + "%", value);
        }
        return result;
    }

    private static Sound soundParameter(EventAction action, EventContext context) {
        String value = stringParameter(action, "sound", "minecraft:block.note_block.pling", context, null);
        Sound sound = RegistryUtils.get(Registry.SOUND_EVENT, value);
        if (sound == null) {
            throw new IllegalArgumentException("Unknown sound: " + value);
        }
        return sound;
    }

    private static void onEntityAndWait(Player player, Consumer<Player> action) {
        CompletableFuture<Void> completed = new CompletableFuture<>();
        player.getScheduler()
                .run(
                        MittelRandomEvents.getInstance(),
                        task -> {
                            try {
                                action.accept(player);
                                completed.complete(null);
                            } catch (Throwable throwable) {
                                completed.completeExceptionally(throwable);
                            }
                        },
                        null);
        completed.join();
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
