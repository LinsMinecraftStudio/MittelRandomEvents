package io.github.lijinhong11.mittelrandomevents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittelrandomevents.MittelRandomEvents;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEventManager;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLine;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLineManager;
import io.github.lijinhong11.mittelrandomevents.utils.Constants;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class MRECommands {
    public static void register() {
        RandomEventManager rem = MittelRandomEvents.getInstance().getEventManager();
        RandomEventLineManager relm = MittelRandomEvents.getInstance().getLineManager();

        LiteralCommandNode<CommandSourceStack> cmdCE = Commands.literal("createEvent")
                .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_EVENTS))
                .then(Commands.argument("id", StringArgumentType.string())
                        .executes(c -> {
                            CommandSender cs = c.getSource().getSender();
                            String id = c.getArgument("id", String.class);

                            if (rem.get(id).isPresent()) {
                                MittelRandomEvents.getInstance().getLanguageManager().sendMessage(cs, "command.create.random_event.exists");
                                return Command.SINGLE_SUCCESS;
                            }

                            RandomEvent re = new RandomEvent(id, new ArrayList<>());
                            rem.register(re);
                            MittelRandomEvents.getInstance().getLanguageManager().sendMessage(cs, "command.create.random_event.success",
                                    MessageReplacement.replace("%id%", id));
                            return Command.SINGLE_SUCCESS;
                        }).then(Commands.argument("displayName", StringArgumentType.greedyString()))
                        .executes(c -> {
                            CommandSender cs = c.getSource().getSender();
                            String id = c.getArgument("id", String.class);

                            if (rem.get(id).isPresent()) {
                                MittelRandomEvents.getInstance().getLanguageManager().sendMessage(cs, "command.create.random_event.exists");
                                return Command.SINGLE_SUCCESS;
                            }

                            RandomEvent re = new RandomEvent(id, new ArrayList<>());
                            re.setDisplayName(ComponentUtils.deserialize(c.getArgument("displayName", String.class)));
                            rem.register(re);
                            MittelRandomEvents.getInstance().getLanguageManager().sendMessage(cs, "command.create.random_event.success",
                                    MessageReplacement.replace("%id%", id));
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();

        LiteralCommandNode<CommandSourceStack> cmdCL = Commands.literal("createLine")
                .requires(c -> c.getSender().hasPermission(Constants.PERM_CREATE_LINES))
                .then(Commands.argument("id", StringArgumentType.string())
                        .executes(c -> {
                            String id = c.getArgument("id", String.class);

                            if (relm.get(id).isPresent()) {
                                MittelRandomEvents.getInstance().getLanguageManager().sendMessage(c.getSource().getSender(), "command.create.line.exists");
                                return Command.SINGLE_SUCCESS;
                            }

                            RandomEventLine rel = new RandomEventLine(id, 0, new ArrayList<>());
                            relm.register(rel);
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }
}
