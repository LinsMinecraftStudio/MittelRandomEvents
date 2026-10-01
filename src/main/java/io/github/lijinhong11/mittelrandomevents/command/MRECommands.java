package io.github.lijinhong11.mittelrandomevents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittelrandomevents.MittelRandomEvents;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEventManager;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLine;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLineManager;
import io.github.lijinhong11.mittelrandomevents.context.DefaultEventContext;
import io.github.lijinhong11.mittelrandomevents.gui.MREGuiManager;
import io.github.lijinhong11.mittelrandomevents.utils.Constants;
import io.github.lijinhong11.mittelrandomevents.utils.RegistryUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Commands for inspecting and managing random events and event lines. */
public final class MRECommands {
    private MRECommands() {}

    public static LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("mittelrandomevents")
                .then(Commands.literal("about")
                        .executes(c -> about(c.getSource().getSender())))
                .then(Commands.literal("gui")
                        .requires(
                                c -> c.getSender().hasPermission(Constants.PERM_GUI) && c.getSender() instanceof Player)
                        .executes(c -> {
                            MREGuiManager.openMain((Player) c.getSource().getSender());
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(events())
                .then(lines())
                .then(Commands.literal("reload")
                        .requires(c -> c.getSender().hasPermission(Constants.PERM_RELOAD))
                        .executes(c -> {
                            MittelRandomEvents plugin = plugin();
                            plugin.getLanguageManager().reload();
                            plugin.getTaskMaker().reload();
                            plugin.reloadData();
                            send(c.getSource().getSender(), "command.reload.success");
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> events() {
        return Commands.literal("events")
                .then(Commands.literal("list").executes(c -> {
                    RandomEventManager manager = plugin().getEventManager();
                    send(
                            c.getSource().getSender(),
                            "command.events.list",
                            replacement(
                                    "%count%", String.valueOf(manager.events().size())),
                            replacement(
                                    "%events%",
                                    manager.events().stream()
                                            .map(RandomEvent::id)
                                            .toList()
                                            .toString()));
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("info")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(c -> eventInfo(c, c.getArgument("id", String.class)))))
                .then(Commands.literal("create")
                        .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_EVENTS))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(c -> createEvent(c, c.getArgument("id", String.class)))
                                .then(Commands.argument("displayName", StringArgumentType.greedyString())
                                        .executes(c -> createEvent(
                                                c,
                                                c.getArgument("id", String.class),
                                                c.getArgument("displayName", String.class))))))
                .then(Commands.literal("delete")
                        .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_EVENTS))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(c -> deleteEvent(c, c.getArgument("id", String.class)))))
                .then(Commands.literal("set")
                        .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_EVENTS))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(eventEnabled())
                                .then(eventDisplayName())
                                .then(eventIcon())))
                .build();
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> eventEnabled() {
        return Commands.literal("enabled")
                .then(Commands.argument("value", BoolArgumentType.bool()).executes(c -> {
                    RandomEvent event = event(c.getArgument("id", String.class));
                    if (event == null) return missing(c.getSource().getSender(), "event");
                    event.setEnabled(BoolArgumentType.getBool(c, "value"));
                    plugin().saveData();
                    send(c.getSource().getSender(), "command.events.set.enabled");
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> eventDisplayName() {
        return Commands.literal("display-name")
                .then(Commands.argument("value", StringArgumentType.greedyString())
                        .executes(c -> {
                            RandomEvent event = event(c.getArgument("id", String.class));
                            if (event == null) return missing(c.getSource().getSender(), "event");
                            event.setDisplayName(ComponentUtils.deserialize(c.getArgument("value", String.class)));
                            plugin().saveData();
                            send(c.getSource().getSender(), "command.events.set.display-name");
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> eventIcon() {
        return Commands.literal("icon")
                .then(Commands.argument("material", StringArgumentType.word()).executes(c -> {
                    RandomEvent event = event(c.getArgument("id", String.class));
                    Material material = RegistryUtils.get(Registry.MATERIAL, c.getArgument("material", String.class));
                    if (event == null) return missing(c.getSource().getSender(), "event");
                    if (material == null) return fail(c.getSource().getSender(), "command.error.material");
                    event.setIcon(material);
                    plugin().saveData();
                    send(c.getSource().getSender(), "command.events.set.icon");
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private static LiteralCommandNode<CommandSourceStack> lines() {
        return Commands.literal("lines")
                .then(Commands.literal("list").executes(c -> {
                    RandomEventLineManager manager = plugin().getLineManager();
                    send(
                            c.getSource().getSender(),
                            "command.lines.list",
                            replacement(
                                    "%count%", String.valueOf(manager.lines().size())),
                            replacement(
                                    "%lines%",
                                    manager.lines().stream()
                                            .map(RandomEventLine::id)
                                            .toList()
                                            .toString()));
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("info")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(c -> lineInfo(c, c.getArgument("id", String.class)))))
                .then(Commands.literal("create")
                        .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_LINES))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("intervalSeconds", IntegerArgumentType.integer(0))
                                        .executes(c -> createLine(
                                                c,
                                                c.getArgument("id", String.class),
                                                IntegerArgumentType.getInteger(c, "intervalSeconds"))))))
                .then(Commands.literal("delete")
                        .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_LINES))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(c -> deleteLine(c, c.getArgument("id", String.class)))))
                .then(Commands.literal("add-event")
                        .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_LINES))
                        .then(Commands.argument("line", StringArgumentType.word())
                                .then(Commands.argument("event", StringArgumentType.word())
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                                .then(Commands.argument(
                                                                "weight",
                                                                DoubleArgumentType.doubleArg(0, Double.MAX_VALUE))
                                                        .executes(c -> addEvent(
                                                                c,
                                                                c.getArgument("line", String.class),
                                                                c.getArgument("event", String.class),
                                                                IntegerArgumentType.getInteger(c, "seconds"),
                                                                DoubleArgumentType.getDouble(c, "weight"))))))))
                .then(Commands.literal("set")
                        .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_LINES))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(lineInterval())
                                .then(lineCron())
                                .then(lineDisplayName())
                                .then(lineIcon())))
                .then(Commands.literal("start")
                        .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_LINES))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(c -> startLine(c, c.getArgument("id", String.class)))))
                .then(Commands.literal("stop")
                        .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_LINES))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(c -> stopLine(c, c.getArgument("id", String.class)))))
                .then(Commands.literal("run")
                        .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_LINES))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(c -> runLine(c, c.getArgument("id", String.class)))))
                .build();
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> lineInterval() {
        return Commands.literal("interval")
                .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                        .executes(c -> {
                            RandomEventLine line = line(c.getArgument("id", String.class));
                            if (line == null) return missing(c.getSource().getSender(), "line");
                            line.setIntervalSeconds(IntegerArgumentType.getInteger(c, "seconds"));
                            line.setCron(null);
                            restartLine(line);
                            plugin().saveData();
                            send(c.getSource().getSender(), "command.lines.set.interval");
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> lineCron() {
        return Commands.literal("cron")
                .then(Commands.argument("expression", StringArgumentType.greedyString())
                        .executes(c -> {
                            RandomEventLine line = line(c.getArgument("id", String.class));
                            if (line == null) return missing(c.getSource().getSender(), "line");
                            line.setCron(c.getArgument("expression", String.class));
                            restartLine(line);
                            plugin().saveData();
                            send(c.getSource().getSender(), "command.lines.set.cron");
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> lineDisplayName() {
        return Commands.literal("display-name")
                .then(Commands.argument("value", StringArgumentType.greedyString())
                        .executes(c -> {
                            RandomEventLine line = line(c.getArgument("id", String.class));
                            if (line == null) return missing(c.getSource().getSender(), "line");
                            line.setDisplayNameFunction(
                                    sender -> ComponentUtils.deserialize(c.getArgument("value", String.class)));
                            plugin().saveData();
                            send(c.getSource().getSender(), "command.lines.set.display-name");
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> lineIcon() {
        return Commands.literal("icon")
                .then(Commands.argument("material", StringArgumentType.word()).executes(c -> {
                    RandomEventLine line = line(c.getArgument("id", String.class));
                    Material material = RegistryUtils.get(Registry.MATERIAL, c.getArgument("material", String.class));
                    if (line == null) return missing(c.getSource().getSender(), "line");
                    if (material == null) return fail(c.getSource().getSender(), "command.error.material");
                    line.setIcon(material);
                    plugin().saveData();
                    send(c.getSource().getSender(), "command.lines.set.icon");
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private static int about(CommandSender sender) {
        send(
                sender,
                "command.about.name",
                replacement("%version%", plugin().getDescription().getVersion()));
        send(sender, "command.about.description");
        return Command.SINGLE_SUCCESS;
    }

    private static int createEvent(CommandContext<CommandSourceStack> c, String id) {
        return createEvent(c, id, null);
    }

    private static int createEvent(CommandContext<CommandSourceStack> c, String id, String displayName) {
        if (event(id) != null) return fail(c.getSource().getSender(), "command.events.exists");
        RandomEvent event = new RandomEvent(id, new ArrayList<>());
        if (displayName != null) event.setDisplayName(ComponentUtils.deserialize(displayName));
        plugin().getEventManager().register(event);
        plugin().saveData();
        send(c.getSource().getSender(), "command.events.create", replacement("%id%", id));
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteEvent(CommandContext<CommandSourceStack> c, String id) {
        if (event(id) == null) return missing(c.getSource().getSender(), "event");
        plugin().getLineManager().lines().forEach(line -> line.events().stream()
                .filter(event -> event.id().equals(id))
                .toList()
                .forEach(line::removeEvent));
        plugin().getEventManager().unregister(id);
        plugin().saveData();
        send(c.getSource().getSender(), "command.events.delete", replacement("%id%", id));
        return Command.SINGLE_SUCCESS;
    }

    private static int eventInfo(CommandContext<CommandSourceStack> c, String id) {
        RandomEvent event = event(id);
        if (event == null) return missing(c.getSource().getSender(), "event");
        send(
                c.getSource().getSender(),
                "command.events.info",
                replacement("%id%", event.id()),
                replacement("%enabled%", String.valueOf(event.isEnabled())),
                replacement("%actions%", String.valueOf(event.actions().size())),
                replacement("%conditions%", String.valueOf(event.conditions().size())));
        return Command.SINGLE_SUCCESS;
    }

    private static int createLine(CommandContext<CommandSourceStack> c, String id, int seconds) {
        if (line(id) != null) return fail(c.getSource().getSender(), "command.lines.exists");
        plugin().getLineManager().register(new RandomEventLine(id, seconds, new ArrayList<>()));
        plugin().saveData();
        send(c.getSource().getSender(), "command.lines.create", replacement("%id%", id));
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteLine(CommandContext<CommandSourceStack> c, String id) {
        if (line(id) == null) return missing(c.getSource().getSender(), "line");
        plugin().getTaskMaker().cancelLine(id);
        plugin().getLineManager().unregister(id);
        plugin().saveData();
        send(c.getSource().getSender(), "command.lines.delete", replacement("%id%", id));
        return Command.SINGLE_SUCCESS;
    }

    private static int addEvent(
            CommandContext<CommandSourceStack> c, String lineId, String eventId, int seconds, double weight) {
        RandomEventLine line = line(lineId);
        RandomEvent event = event(eventId);
        if (line == null) return missing(c.getSource().getSender(), "line");
        if (event == null) return missing(c.getSource().getSender(), "event");
        if (line.events().contains(event)) return fail(c.getSource().getSender(), "command.lines.add-event.exists");
        line.setIntervalSeconds(seconds);
        line.setCron(null);
        line.addEvent(event);
        line.setWeight(event, weight);
        restartLine(line);
        plugin().saveData();
        send(
                c.getSource().getSender(),
                "command.lines.add-event.success",
                replacement("%event%", eventId),
                replacement("%line%", lineId));
        return Command.SINGLE_SUCCESS;
    }

    private static int lineInfo(CommandContext<CommandSourceStack> c, String id) {
        RandomEventLine line = line(id);
        if (line == null) return missing(c.getSource().getSender(), "line");
        send(
                c.getSource().getSender(),
                "command.lines.info",
                replacement("%id%", line.id()),
                replacement("%interval%", String.valueOf(line.intervalSeconds())),
                replacement("%cron%", String.valueOf(line.cron())),
                replacement("%events%", String.valueOf(line.events().size())),
                replacement("%running%", String.valueOf(plugin().getTaskMaker().isRunning(id))));
        return Command.SINGLE_SUCCESS;
    }

    private static int startLine(CommandContext<CommandSourceStack> c, String id) {
        RandomEventLine line = line(id);
        if (line == null) return missing(c.getSource().getSender(), "line");
        plugin().getTaskMaker().startLine(line, () -> new DefaultEventContext(plugin().getEventManager()));
        send(c.getSource().getSender(), "command.lines.start", replacement("%id%", id));
        return Command.SINGLE_SUCCESS;
    }

    private static void restartLine(RandomEventLine line) {
        plugin().getTaskMaker().cancelLine(line.id());
        plugin().getTaskMaker().startLine(line, () -> new DefaultEventContext(plugin().getEventManager()));
    }

    private static int stopLine(CommandContext<CommandSourceStack> c, String id) {
        if (line(id) == null) return missing(c.getSource().getSender(), "line");
        plugin().getTaskMaker().cancelLine(id);
        send(c.getSource().getSender(), "command.lines.stop", replacement("%id%", id));
        return Command.SINGLE_SUCCESS;
    }

    private static int runLine(CommandContext<CommandSourceStack> c, String id) {
        RandomEventLine line = line(id);
        if (line == null) return missing(c.getSource().getSender(), "line");
        plugin().getTaskMaker().runOnce(line, () -> new DefaultEventContext(plugin().getEventManager()));
        send(c.getSource().getSender(), "command.lines.run", replacement("%id%", id));
        return Command.SINGLE_SUCCESS;
    }

    private static RandomEvent event(String id) {
        return plugin().getEventManager().get(id).orElse(null);
    }

    private static RandomEventLine line(String id) {
        return plugin().getLineManager().get(id).orElse(null);
    }

    private static MittelRandomEvents plugin() {
        return MittelRandomEvents.getInstance();
    }

    private static int missing(CommandSender sender, String type) {
        return fail(sender, "command.error.unknown", replacement("%type%", type));
    }

    private static int fail(CommandSender sender, String key, MessageReplacement... replacements) {
        send(sender, key, replacements);
        return 0;
    }

    private static MessageReplacement replacement(String key, String value) {
        return MessageReplacement.replace(key, value);
    }

    private static void send(CommandSender sender, String key, MessageReplacement... replacements) {
        plugin().getLanguageManager().sendMessage(sender, key, replacements);
    }
}
