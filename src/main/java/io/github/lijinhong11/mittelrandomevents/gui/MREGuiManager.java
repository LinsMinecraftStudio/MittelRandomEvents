package io.github.lijinhong11.mittelrandomevents.gui;

import io.github.lijinhong11.mittellib.gui.dialog.impl.input.FloatInputDialog;
import io.github.lijinhong11.mittellib.gui.dialog.impl.input.IntegerInputDialog;
import io.github.lijinhong11.mittellib.gui.dialog.impl.input.TextInputDialog;
import io.github.lijinhong11.mittellib.gui.inventory.MittelGUI;
import io.github.lijinhong11.mittellib.gui.inventory.choosers.MaterialChooser;
import io.github.lijinhong11.mittellib.gui.inventory.impl.ChestGUI;
import io.github.lijinhong11.mittellib.gui.inventory.impl.PaginatedChestGUI;
import io.github.lijinhong11.mittellib.gui.inventory.item.ButtonItem;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittelrandomevents.MittelRandomEvents;
import io.github.lijinhong11.mittelrandomevents.api.action.ActionType;
import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import io.github.lijinhong11.mittelrandomevents.api.event.EventCondition;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLine;
import io.github.lijinhong11.mittelrandomevents.builtin.BuiltInEventCondition;
import io.github.lijinhong11.mittelrandomevents.context.DefaultEventContext;
import io.github.lijinhong11.mittelrandomevents.data.EventConditionCodec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** SuperMines-style navigation, paginated lists and management screens for MRE objects. */
public final class MREGuiManager {
    private MREGuiManager() {}

    public static void openMain(Player player) {
        ChestGUI gui = MittelGUI.chestBuilder()
                .title(msg(player, "gui.home.title"))
                .size(27)
                .structure("XXXXXXXXC", "XXXPXMXXX", "XXXXXXXXX")
                .bind('X', ButtonItem.BACKGROUND)
                .bind('P', sectionButton(Material.NETHER_STAR, player, "gui.home.events", () -> openEventList(player)))
                .bind('M', sectionButton(Material.CLOCK, player, "gui.home.lines", () -> openLineList(player)))
                .bind('C', ButtonItem.clickable(messagedItem(Material.BARRIER, player, "gui.items.close"), (g, e) -> {
                    player.closeInventory();
                    return false;
                }))
                .build();
        gui.open(player);
    }

    public static void openEventList(Player player) {
        PaginatedChestGUI gui = paged(player, "gui.events.title", () -> openMain(player));
        gui.addPageItem(ButtonItem.clickable(messagedItem(Material.EMERALD, player, "gui.items.create"), (g, e) -> {
            createEvent(player);
            return false;
        }));

        for (RandomEvent event : plugin().getEventManager().events()) {
            gui.addPageItem(ButtonItem.clickable(objectItem(player, event), (g, e) -> {
                if (e.getClick().isRightClick()) {
                    plugin().getEventManager().unregister(event.id());
                    plugin().getLineManager().lines().forEach(line -> line.events().stream()
                            .filter(registered -> registered.equals(event))
                            .toList()
                            .forEach(line::removeEvent));
                    plugin().saveData();
                    openEventList(player);
                } else {
                    openEventManagement(player, event);
                }
                return false;
            }));
        }
        gui.open(player);
    }

    public static void openLineList(Player player) {
        PaginatedChestGUI gui = paged(player, "gui.lines.title", () -> openMain(player));
        gui.addPageItem(ButtonItem.clickable(messagedItem(Material.EMERALD, player, "gui.items.create"), (g, e) -> {
            createLine(player);
            return false;
        }));

        for (RandomEventLine line : plugin().getLineManager().lines()) {
            gui.addPageItem(ButtonItem.clickable(objectItem(player, line), (g, e) -> {
                if (e.getClick().isRightClick()) {
                    plugin().getTaskMaker().cancelLine(line.id());
                    plugin().getLineManager().unregister(line.id());
                    plugin().saveData();
                    openLineList(player);
                } else {
                    openLineManagement(player, line);
                }
                return false;
            }));
        }
        gui.open(player);
    }

    private static void openEventManagement(Player player, RandomEvent event) {
        ChestGUI gui =
                management(player, "gui.events.management.title", MessageReplacement.replace("%id%", event.id()));
        gui.putItem(slot(2, 5), ButtonItem.unclickable(objectItem(player, event)));
        gui.putItem(
                slot(3, 3),
                ButtonItem.clickable(
                        messagedItem(
                                Material.LEVER, player, event.isEnabled() ? "gui.items.disable" : "gui.items.enable"),
                        (g, e) -> {
                            event.setEnabled(!event.isEnabled());
                            plugin().saveData();
                            openEventManagement(player, event);
                            return false;
                        }));
        gui.putItem(
                slot(3, 5),
                ButtonItem.clickable(messagedItem(Material.NAME_TAG, player, "gui.items.rename"), (g, e) -> {
                    player.closeInventory();
                    TextInputDialog.create(
                                    msg(player, "gui.common.rename-title"),
                                    msg(player, "gui.common.rename-label"),
                                    value -> {
                                        event.setDisplayName(ComponentUtils.deserialize(value));
                                        plugin().saveData();
                                        openEventManagement(player, event);
                                    },
                                    256,
                                    ComponentUtils.serialize(event.getDisplayName()),
                                    () -> openEventManagement(player, event))
                            .show(player);
                    return false;
                }));
        gui.putItem(
                slot(3, 7),
                ButtonItem.clickable(messagedItem(Material.COMMAND_BLOCK, player, "gui.items.actions"), (g, e) -> {
                    openActionList(player, event, false);
                    return false;
                }));
        gui.putItem(
                slot(3, 8),
                ButtonItem.clickable(messagedItem(Material.COMMAND_BLOCK, player, "gui.items.end-actions"), (g, e) -> {
                    openActionList(player, event, true);
                    return false;
                }));
        gui.putItem(
                slot(4, 5),
                ButtonItem.clickable(messagedItem(Material.COMPARATOR, player, "gui.items.conditions"), (g, e) -> {
                    openConditionList(player, event);
                    return false;
                }));
        gui.putItem(
                slot(4, 3),
                ButtonItem.clickable(messagedItem(Material.ITEM_FRAME, player, "gui.items.icon"), (g, e) -> {
                    MaterialChooser.openVanillaChooser(player, selected -> {
                        event.setIcon(selected.toItem().getType());
                        plugin().saveData();
                        openEventManagement(player, event);
                    });
                    return false;
                }));
        putBack(gui, player, MREGuiManager::openEventList);
        gui.open(player);
    }

    private static void openLineManagement(Player player, RandomEventLine line) {
        ChestGUI gui = management(player, "gui.lines.management.title", MessageReplacement.replace("%id%", line.id()));
        gui.putItem(slot(2, 5), ButtonItem.unclickable(objectItem(player, line)));
        gui.putItem(
                slot(3, 3),
                ButtonItem.clickable(
                        messagedItem(
                                Material.REDSTONE_TORCH,
                                player,
                                plugin().getTaskMaker().isRunning(line.id()) ? "gui.items.stop" : "gui.items.start"),
                        (g, e) -> {
                            if (plugin().getTaskMaker().isRunning(line.id())) {
                                plugin().getTaskMaker().cancelLine(line.id());
                            } else {
                                plugin().getTaskMaker()
                                        .startLine(line, () -> new DefaultEventContext(plugin().getEventManager()));
                            }
                            openLineManagement(player, line);
                            return false;
                        }));
        gui.putItem(
                slot(3, 5),
                ButtonItem.clickable(messagedItem(Material.LIGHTNING_ROD, player, "gui.items.run"), (g, e) -> {
                    plugin().getTaskMaker().runOnce(line, () -> new DefaultEventContext(plugin().getEventManager()));
                    return false;
                }));
        gui.putItem(
                slot(3, 7),
                ButtonItem.clickable(messagedItem(Material.NAME_TAG, player, "gui.items.rename"), (g, e) -> {
                    player.closeInventory();
                    TextInputDialog.create(
                                    msg(player, "gui.common.rename-title"),
                                    msg(player, "gui.common.rename-label"),
                                    value -> {
                                        line.setDisplayNameFunction(sender -> ComponentUtils.deserialize(value));
                                        plugin().saveData();
                                        openLineManagement(player, line);
                                    },
                                    256,
                                    ComponentUtils.serialize(
                                            line.getDisplayNameFunction().apply(player)),
                                    () -> openLineManagement(player, line))
                            .show(player);
                    return false;
                }));
        gui.putItem(
                slot(4, 5), ButtonItem.clickable(messagedItem(Material.BUNDLE, player, "gui.items.events"), (g, e) -> {
                    openLineEvents(player, line);
                    return false;
                }));
        gui.putItem(
                slot(4, 3),
                ButtonItem.clickable(messagedItem(Material.REPEATER, player, "gui.items.interval"), (g, e) -> {
                    editInterval(player, line);
                    return false;
                }));
        gui.putItem(slot(4, 7), ButtonItem.clickable(messagedItem(Material.CLOCK, player, "gui.items.cron"), (g, e) -> {
            editCron(player, line);
            return false;
        }));
        gui.putItem(
                slot(5, 5),
                ButtonItem.clickable(messagedItem(Material.ITEM_FRAME, player, "gui.items.icon"), (g, e) -> {
                    MaterialChooser.openVanillaChooser(player, selected -> {
                        line.setIcon(selected.toItem().getType());
                        plugin().saveData();
                        openLineManagement(player, line);
                    });
                    return false;
                }));
        putBack(gui, player, MREGuiManager::openLineList);
        gui.open(player);
    }

    private static void createEvent(Player player) {
        player.closeInventory();
        TextInputDialog.create(
                        msg(player, "gui.events.create.title"),
                        msg(player, "gui.events.create.label"),
                        id -> {
                            if (plugin().getEventManager().get(id).isEmpty()) {
                                plugin().getEventManager().register(new RandomEvent(id, List.of()));
                                plugin().saveData();
                            }
                            openEventList(player);
                        },
                        () -> openEventList(player))
                .show(player);
    }

    private static void createLine(Player player) {
        player.closeInventory();
        TextInputDialog.create(
                        msg(player, "gui.lines.create.title"),
                        msg(player, "gui.lines.create.label"),
                        id -> {
                            if (plugin().getLineManager().get(id).isEmpty()) {
                                plugin().getLineManager().register(new RandomEventLine(id, 0, List.of()));
                                plugin().saveData();
                            }
                            openLineList(player);
                        },
                        () -> openLineList(player))
                .show(player);
    }

    private static void openActionList(Player player, RandomEvent event, boolean ending) {
        PaginatedChestGUI gui = paged(
                player,
                ending ? "gui.actions.end-title" : "gui.actions.start-title",
                () -> openEventManagement(player, event));
        gui.addPageItem(ButtonItem.clickable(messagedItem(Material.EMERALD, player, "gui.items.create"), (g, e) -> {
            openActionChooser(player, event, ending);
            return false;
        }));
        for (EventAction action : ending ? event.endActions() : event.actions()) {
            ActionType type =
                    plugin().getEventManager().getActionType(action.type()).orElse(null);
            Material icon = type == null ? Material.BARRIER : type.icon();
            Component name = type == null
                    ? msg(player, "gui.common.unknown-type", MessageReplacement.replace("%value%", action.type()))
                    : actionName(player, type);
            ItemStack display = messagedItem(
                    icon,
                    player,
                    "gui.objects.action",
                    MessageReplacement.replace("%name%", ComponentUtils.serialize(name)),
                    MessageReplacement.replace("%type%", action.type()),
                    MessageReplacement.replace(
                            "%parameters%", action.parameters().asMap().toString()));
            gui.addPageItem(ButtonItem.clickable(display, (g, e) -> {
                if (e.getClick().isRightClick()) {
                    if (ending ? event.removeEndAction(action) : event.removeAction(action)) {
                        plugin().saveData();
                    }
                    openActionList(player, event, ending);
                } else if (type != null && (ending ? event.endActions() : event.actions()).contains(action)) {
                    openActionParameters(player, event, action, type, ending);
                }
                return false;
            }));
        }
        gui.open(player);
    }

    private static void openActionParameters(
            Player player, RandomEvent event, EventAction action, ActionType type, boolean ending) {
        if (!(ending ? event.endActions() : event.actions()).contains(action)) {
            openActionList(player, event, ending);
            return;
        }
        PaginatedChestGUI gui =
                paged(player, "gui.actions.parameters-title", () -> openActionList(player, event, ending));
        if (type.parameterNames().isEmpty()) {
            gui.addPageItem(ButtonItem.unclickable(messagedItem(Material.PAPER, player, "gui.items.no-parameters")));
        }
        if (isConditionalAction(type)) {
            gui.addPageItem(
                    ButtonItem.clickable(messagedItem(Material.COMPARATOR, player, "gui.items.conditions"), (g, e) -> {
                        openActionConditionList(player, event, action, type, ending);
                        return false;
                    }));
        }
        for (String parameter : type.parameterNames()) {
            Object current = action.parameters().get(parameter);
            ItemStack display = messagedItem(
                    Material.WRITABLE_BOOK,
                    player,
                    "gui.objects.parameter",
                    MessageReplacement.replace("%name%", parameter),
                    MessageReplacement.replace("%value%", String.valueOf(current)));
            gui.addPageItem(ButtonItem.clickable(display, (g, e) -> {
                player.closeInventory();
                TextInputDialog.create(
                                msg(player, "gui.actions.parameter-title"),
                                Component.text(parameter),
                                value -> {
                                    action.parameters().set(parameter, scalar(value));
                                    plugin().saveData();
                                    openActionParameters(player, event, action, type, ending);
                                },
                                512,
                                current == null ? "" : String.valueOf(current),
                                () -> openActionParameters(player, event, action, type, ending))
                        .show(player);
                return false;
            }));
        }
        gui.open(player);
    }

    private static void openActionConditionList(
            Player player, RandomEvent event, EventAction action, ActionType type, boolean ending) {
        PaginatedChestGUI gui = paged(
                player,
                "gui.actions.conditions-title",
                () -> openActionParameters(player, event, action, type, ending));
        gui.addPageItem(ButtonItem.clickable(messagedItem(Material.EMERALD, player, "gui.items.create"), (g, e) -> {
            openActionConditionChooser(player, event, action, type, ending);
            return false;
        }));
        for (EventCondition condition : action.conditions()) {
            ItemStack display = messagedItem(
                    condition.icon(),
                    player,
                    "gui.objects.condition",
                    MessageReplacement.replace("%name%", ComponentUtils.serialize(conditionName(player, condition))),
                    MessageReplacement.replace("%type%", String.valueOf(condition.type())),
                    MessageReplacement.replace(
                            "%parameters%", condition.parameters().toString()));
            gui.addPageItem(ButtonItem.clickable(display, (g, e) -> {
                if (e.getClick().isRightClick()) {
                    if (action.removeCondition(condition)) plugin().saveData();
                    openActionConditionList(player, event, action, type, ending);
                } else if (action.conditions().contains(condition)) {
                    openActionConditionParameters(player, event, action, type, condition, ending);
                }
                return false;
            }));
        }
        gui.open(player);
    }

    private static void openActionConditionChooser(
            Player player, RandomEvent event, EventAction action, ActionType type, boolean ending) {
        PaginatedChestGUI gui = paged(
                player,
                "gui.conditions.chooser-title",
                () -> openActionConditionList(player, event, action, type, ending));
        for (EventCondition condition : conditionTemplates()) {
            gui.addPageItem(ButtonItem.clickable(
                    messagedItem(
                            condition.icon(),
                            player,
                            "gui.objects.condition-template",
                            MessageReplacement.replace(
                                    "%name%", ComponentUtils.serialize(conditionName(player, condition))),
                            MessageReplacement.replace("%type%", String.valueOf(condition.type())),
                            MessageReplacement.replace(
                                    "%parameters%", condition.parameters().toString())),
                    (g, e) -> {
                        action.addCondition(condition);
                        plugin().saveData();
                        openActionConditionList(player, event, action, type, ending);
                        return false;
                    }));
        }
        gui.open(player);
    }

    private static void openActionConditionParameters(
            Player player,
            RandomEvent event,
            EventAction action,
            ActionType type,
            EventCondition condition,
            boolean ending) {
        if (!action.conditions().contains(condition)) {
            openActionConditionList(player, event, action, type, ending);
            return;
        }
        PaginatedChestGUI gui = paged(
                player,
                "gui.conditions.parameters-title",
                () -> openActionConditionList(player, event, action, type, ending));
        for (Map.Entry<String, Object> entry : condition.parameters().entrySet()) {
            if (entry.getValue() instanceof Map<?, ?> || entry.getValue() instanceof Iterable<?>) continue;
            ItemStack display = messagedItem(
                    Material.WRITABLE_BOOK,
                    player,
                    "gui.objects.parameter",
                    MessageReplacement.replace("%name%", entry.getKey()),
                    MessageReplacement.replace("%value%", String.valueOf(entry.getValue())));
            gui.addPageItem(ButtonItem.clickable(display, (g, e) -> {
                player.closeInventory();
                TextInputDialog.create(
                                msg(player, "gui.conditions.parameter-title"),
                                msg(
                                        player,
                                        "gui.common.parameter",
                                        MessageReplacement.replace("%value%", entry.getKey())),
                                value -> {
                                    Map<String, Object> encoded = EventConditionCodec.encode(condition);
                                    Map<String, Object> parameters = new LinkedHashMap<>(
                                            EventConditionCodec.stringMap(encoded.get("parameters")));
                                    parameters.put(entry.getKey(), scalar(value));
                                    encoded.put("parameters", parameters);
                                    action.removeCondition(condition);
                                    action.addCondition(EventConditionCodec.decode(encoded));
                                    plugin().saveData();
                                    openActionConditionList(player, event, action, type, ending);
                                },
                                512,
                                String.valueOf(entry.getValue()),
                                () -> openActionConditionParameters(player, event, action, type, condition, ending))
                        .show(player);
                return false;
            }));
        }
        gui.open(player);
    }

    private static boolean isConditionalAction(ActionType type) {
        return type.id().equals("stop_actions") || type.id().equals("skip_next_action");
    }

    private static List<EventCondition> conditionTemplates() {
        return List.of(
                BuiltInEventCondition.players(0, Integer.MAX_VALUE),
                BuiltInEventCondition.worlds(0, Integer.MAX_VALUE),
                BuiltInEventCondition.serverOnlinePlayers(0, Integer.MAX_VALUE),
                BuiltInEventCondition.serverTpsAtLeast(0.0D),
                BuiltInEventCondition.serverPluginEnabled("PluginName"),
                BuiltInEventCondition.serverVersionContains("version"));
    }

    private static void openConditionList(Player player, RandomEvent event) {
        PaginatedChestGUI gui = paged(player, "gui.conditions.title", () -> openEventManagement(player, event));
        gui.addPageItem(ButtonItem.clickable(messagedItem(Material.EMERALD, player, "gui.items.create"), (g, e) -> {
            openConditionChooser(player, event);
            return false;
        }));
        for (int index = 0; index < event.conditions().size(); index++) {
            EventCondition condition = event.conditions().get(index);
            ItemStack display = messagedItem(
                    condition.icon(),
                    player,
                    "gui.objects.condition",
                    MessageReplacement.replace("%name%", ComponentUtils.serialize(conditionName(player, condition))),
                    MessageReplacement.replace("%type%", String.valueOf(condition.type())),
                    MessageReplacement.replace(
                            "%parameters%", condition.parameters().toString()));
            int conditionIndex = index;
            gui.addPageItem(ButtonItem.clickable(display, (g, e) -> {
                if (e.getClick().isRightClick()) {
                    event.removeCondition(conditionIndex);
                    plugin().saveData();
                    openConditionList(player, event);
                } else {
                    openConditionParameters(player, event, conditionIndex);
                }
                return false;
            }));
        }
        gui.open(player);
    }

    private static void openConditionChooser(Player player, RandomEvent event) {
        PaginatedChestGUI gui = paged(player, "gui.conditions.chooser-title", () -> openConditionList(player, event));
        List<EventCondition> templates = List.of(
                BuiltInEventCondition.players(0, Integer.MAX_VALUE),
                BuiltInEventCondition.worlds(0, Integer.MAX_VALUE),
                BuiltInEventCondition.serverOnlinePlayers(0, Integer.MAX_VALUE),
                BuiltInEventCondition.serverTpsAtLeast(0.0D),
                BuiltInEventCondition.serverPluginEnabled("PluginName"),
                BuiltInEventCondition.serverVersionContains("version"));
        for (EventCondition condition : templates) {
            gui.addPageItem(ButtonItem.clickable(
                    messagedItem(
                            condition.icon(),
                            player,
                            "gui.objects.condition-template",
                            MessageReplacement.replace(
                                    "%name%", ComponentUtils.serialize(conditionName(player, condition))),
                            MessageReplacement.replace("%type%", String.valueOf(condition.type())),
                            MessageReplacement.replace(
                                    "%parameters%", condition.parameters().toString())),
                    (g, e) -> {
                        event.addCondition(condition);
                        plugin().saveData();
                        openConditionList(player, event);
                        return false;
                    }));
        }
        gui.open(player);
    }

    private static void openConditionParameters(Player player, RandomEvent event, int conditionIndex) {
        EventCondition condition = event.conditions().get(conditionIndex);
        PaginatedChestGUI gui =
                paged(player, "gui.conditions.parameters-title", () -> openConditionList(player, event));
        for (Map.Entry<String, Object> entry : condition.parameters().entrySet()) {
            if (entry.getValue() instanceof Map<?, ?> || entry.getValue() instanceof Iterable<?>) continue;
            ItemStack display = messagedItem(
                    Material.WRITABLE_BOOK,
                    player,
                    "gui.objects.parameter",
                    MessageReplacement.replace("%name%", entry.getKey()),
                    MessageReplacement.replace("%value%", String.valueOf(entry.getValue())));
            gui.addPageItem(ButtonItem.clickable(display, (g, e) -> {
                player.closeInventory();
                TextInputDialog.create(
                                msg(player, "gui.conditions.parameter-title"),
                                msg(
                                        player,
                                        "gui.common.parameter",
                                        MessageReplacement.replace("%value%", entry.getKey())),
                                value -> {
                                    Map<String, Object> encoded = EventConditionCodec.encode(condition);
                                    Map<String, Object> parameters = new LinkedHashMap<>(
                                            EventConditionCodec.stringMap(encoded.get("parameters")));
                                    parameters.put(entry.getKey(), scalar(value));
                                    encoded.put("parameters", parameters);
                                    event.setCondition(conditionIndex, EventConditionCodec.decode(encoded));
                                    plugin().saveData();
                                    openConditionParameters(player, event, conditionIndex);
                                },
                                512,
                                String.valueOf(entry.getValue()),
                                () -> openConditionParameters(player, event, conditionIndex))
                        .show(player);
                return false;
            }));
        }
        gui.open(player);
    }

    private static void editInterval(Player player, RandomEventLine line) {
        player.closeInventory();
        IntegerInputDialog.create(
                        msg(player, "gui.lines.interval-title"),
                        msg(player, "gui.lines.interval-label"),
                        0,
                        Integer.MAX_VALUE,
                        value -> {
                            line.setIntervalSeconds(value);
                            line.setCron(null);
                            restart(line);
                            plugin().saveData();
                            openLineManagement(player, line);
                        },
                        () -> openLineManagement(player, line))
                .show(player);
    }

    private static void editCron(Player player, RandomEventLine line) {
        player.closeInventory();
        TextInputDialog.create(
                        msg(player, "gui.lines.cron-title"),
                        msg(player, "gui.lines.cron-label"),
                        value -> {
                            line.setCron(value.isBlank() ? null : value);
                            restart(line);
                            plugin().saveData();
                            openLineManagement(player, line);
                        },
                        128,
                        line.cron() == null ? "" : line.cron(),
                        () -> openLineManagement(player, line))
                .show(player);
    }

    private static void restart(RandomEventLine line) {
        plugin().getTaskMaker().restartLine(line, () -> new DefaultEventContext(plugin().getEventManager()));
    }

    private static void openActionChooser(Player player, RandomEvent event, boolean ending) {
        PaginatedChestGUI gui = paged(player, "gui.actions.chooser-title", () -> openActionList(player, event, ending));
        for (ActionType type : plugin().getEventManager().actionTypesRegistry().values()) {
            ItemStack display = messagedItem(
                    type.icon(),
                    player,
                    type.parameterNames().isEmpty()
                            ? "gui.objects.action-template-no-parameters"
                            : "gui.objects.action-template",
                    MessageReplacement.replace("%name%", ComponentUtils.serialize(actionName(player, type))),
                    MessageReplacement.replace(
                            "%description%", ComponentUtils.serialize(actionDescription(player, type))),
                    MessageReplacement.replace("%parameters%", String.join(", ", type.parameterNames())));
            gui.addPageItem(ButtonItem.clickable(display, (g, e) -> {
                if (ending) {
                    event.addEndAction(EventAction.of(type.id()));
                } else {
                    event.addAction(EventAction.of(type.id()));
                }
                plugin().saveData();
                openActionList(player, event, ending);
                return false;
            }));
        }
        gui.open(player);
    }

    private static void openLineEvents(Player player, RandomEventLine line) {
        PaginatedChestGUI gui = paged(player, "gui.line-events.title", () -> openLineManagement(player, line));
        gui.addPageItem(ButtonItem.clickable(messagedItem(Material.EMERALD, player, "gui.items.create"), (g, e) -> {
            openLineEventChooser(player, line);
            return false;
        }));
        for (RandomEvent event : line.events()) {
            ItemStack display = messagedItem(
                    event.getIcon(),
                    player,
                    "gui.objects.line-event",
                    MessageReplacement.replace("%name%", ComponentUtils.serialize(event.displayName(player))),
                    MessageReplacement.replace("%id%", event.id()),
                    MessageReplacement.replace("%weight%", String.valueOf(line.weightOf(event))));
            gui.addPageItem(ButtonItem.clickable(display, (g, e) -> {
                if (e.getClick().isRightClick()) {
                    line.removeEvent(event);
                    plugin().saveData();
                    openLineEvents(player, line);
                } else {
                    editWeight(player, line, event);
                }
                return false;
            }));
        }
        gui.open(player);
    }

    private static void openLineEventChooser(Player player, RandomEventLine line) {
        PaginatedChestGUI gui = paged(player, "gui.line-events.chooser-title", () -> openLineEvents(player, line));
        for (RandomEvent event : plugin().getEventManager().events()) {
            if (line.events().contains(event)) continue;
            gui.addPageItem(ButtonItem.clickable(objectItem(player, event), (g, e) -> {
                line.addEvent(event);
                plugin().saveData();
                openLineEvents(player, line);
                return false;
            }));
        }
        gui.open(player);
    }

    private static void editWeight(Player player, RandomEventLine line, RandomEvent event) {
        player.closeInventory();
        FloatInputDialog.createWithDefaultStep(
                        msg(player, "gui.line-events.weight-title"),
                        msg(player, "gui.line-events.weight-label"),
                        (float) line.weightOf(event),
                        0.1f,
                        Float.MAX_VALUE,
                        value -> {
                            line.setWeight(event, value);
                            plugin().saveData();
                            openLineEvents(player, line);
                        },
                        () -> openLineEvents(player, line))
                .show(player);
    }

    private static PaginatedChestGUI paged(Player player, String titleKey, Runnable back) {
        return MittelGUI.pagedChestBuilder()
                .title(msg(player, titleKey))
                .size(54)
                .structure("XXXXXXXXX", "XCCCCCCCX", "XCCCCCCCX", "XCCCCCCCX", "XCCCCCCCX", "XKXPXNXXB")
                .content('C')
                .previousPage('P', ButtonItem.unclickable(messagedItem(Material.ARROW, player, "gui.items.previous")))
                .nextPage('N', ButtonItem.unclickable(messagedItem(Material.ARROW, player, "gui.items.next")))
                .bind('X', ButtonItem.BACKGROUND)
                .bind('K', ButtonItem.BACKGROUND)
                .bind('B', ButtonItem.clickable(messagedItem(Material.BARRIER, player, "gui.items.back"), (g, e) -> {
                    back.run();
                    return false;
                }))
                .build();
    }

    private static ChestGUI management(Player player, String titleKey, MessageReplacement... replacements) {
        return MittelGUI.chestBuilder()
                .title(plugin().getLanguageManager().getMsgComponent(player, titleKey, replacements))
                .size(54)
                .structure("XXXXXXXXX", "X       X", "X       X", "X       X", "X       X", "XXXXXXXXX")
                .bind('X', ButtonItem.BACKGROUND)
                .build();
    }

    private static void putBack(ChestGUI gui, Player player, java.util.function.Consumer<Player> back) {
        gui.putItem(8, ButtonItem.clickable(messagedItem(Material.BARRIER, player, "gui.items.back"), (g, e) -> {
            back.accept(player);
            return false;
        }));
    }

    private static ButtonItem sectionButton(Material material, Player player, String sectionKey, Runnable action) {
        ItemStack translated = plugin().getLanguageManager().getMessagedItem(material, sectionKey, player);
        return ButtonItem.clickable(translated, (g, e) -> {
            action.run();
            return false;
        });
    }

    private static ItemStack objectItem(Player player, RandomEvent event) {
        return messagedItem(
                event.getIcon(),
                player,
                "gui.objects.event",
                MessageReplacement.replace("%name%", ComponentUtils.serialize(event.displayName(player))),
                MessageReplacement.replace("%id%", event.id()),
                MessageReplacement.replace("%enabled%", String.valueOf(event.isEnabled())),
                MessageReplacement.replace(
                        "%actions%", String.valueOf(event.actions().size())));
    }

    private static ItemStack objectItem(Player player, RandomEventLine line) {
        return messagedItem(
                line.getIcon(),
                player,
                "gui.objects.line",
                MessageReplacement.replace("%name%", ComponentUtils.serialize(line.displayName(player))),
                MessageReplacement.replace("%id%", line.id()),
                MessageReplacement.replace(
                        "%events%", String.valueOf(line.events().size())),
                MessageReplacement.replace(
                        "%running%", String.valueOf(plugin().getTaskMaker().isRunning(line.id()))));
    }

    private static ItemStack messagedItem(Material material, Player player, String key) {
        return plugin().getLanguageManager().getMessagedItem(material, key, player);
    }

    private static ItemStack messagedItem(
            Material material, Player player, String key, MessageReplacement... replacements) {
        return plugin().getLanguageManager().getMessagedItem(material, key, player, replacements);
    }

    private static Component msg(Player player, String key) {
        return plugin().getLanguageManager().getMsgComponent(player, key);
    }

    private static Component msg(Player player, String key, MessageReplacement... replacements) {
        return plugin().getLanguageManager().getMsgComponent(player, key, replacements);
    }

    private static Component actionName(Player player, ActionType type) {
        return localizedOrDefault(player, "mre.action." + type.id() + ".name", type.displayName());
    }

    private static Component conditionName(Player player, EventCondition condition) {
        String type = condition.type();
        return type == null
                ? condition.displayName()
                : localizedOrDefault(player, "mre.condition." + type + ".name", condition.displayName());
    }

    private static List<Component> actionLore(Player player, ActionType type) {
        Component description = actionDescription(player, type);
        if (description.equals(Component.empty())) return type.lore();
        if (type.parameterNames().isEmpty()) return List.of(description);
        return List.of(
                description,
                msg(
                        player,
                        "gui.common.parameters",
                        MessageReplacement.replace("%value%", String.join(", ", type.parameterNames()))));
    }

    private static Component actionDescription(Player player, ActionType type) {
        return localizedOrDefault(player, "mre.action." + type.id() + ".description", type.description());
    }

    private static Component localizedOrDefault(Player player, String key, Component fallback) {
        String localized = plugin().getLanguageManager().getMsg(player, key);
        return localized.equals(key) ? fallback : ComponentUtils.deserialize(localized);
    }

    private static Object scalar(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return value;
        }
    }

    private static int slot(int row, int column) {
        return (row - 1) * 9 + (column - 1);
    }

    private static MittelRandomEvents plugin() {
        return MittelRandomEvents.getInstance();
    }
}
