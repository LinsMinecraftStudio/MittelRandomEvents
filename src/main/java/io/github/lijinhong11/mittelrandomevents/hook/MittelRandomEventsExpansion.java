package io.github.lijinhong11.mittelrandomevents.hook;

import io.github.lijinhong11.mittellib.hook.placeholder.UniversalPlaceholderExpansion;
import io.github.lijinhong11.mittellib.utils.StringUtils;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittelrandomevents.MittelRandomEvents;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLine;
import org.jetbrains.annotations.NotNull;

/** PlaceholderAPI and MiniPlaceholders expansion for MittelRandomEvents state. */
public final class MittelRandomEventsExpansion extends UniversalPlaceholderExpansion {
    private final MittelRandomEvents plugin;

    public MittelRandomEventsExpansion(MittelRandomEvents plugin) {
        this.plugin = plugin;
        registerPlaceholder(
                "event_count",
                PlaceholderType.GLOBAL,
                (viewer, target, args) ->
                        String.valueOf(plugin.getEventManager().events().size()));
        registerPlaceholder(
                "line_count",
                PlaceholderType.GLOBAL,
                (viewer, target, args) ->
                        String.valueOf(plugin.getLineManager().lines().size()));
        registerPlaceholder("event_enabled", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            RandomEvent event = event(args);
            return event == null ? "false" : String.valueOf(event.isEnabled());
        });
        registerPlaceholder("event_display_name", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            RandomEvent event = event(args);
            return event == null ? "" : ComponentUtils.serialize(event.getDisplayName());
        });
        registerPlaceholder("line_running", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            RandomEventLine line = line(args);
            return line == null ? "false" : String.valueOf(plugin.getTaskMaker().isRunning(line.id()));
        });
        registerPlaceholder("next_event_remaining", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            RandomEventLine line = line(args);
            return line == null ? "0" : String.valueOf(line.remainingNextEventSeconds());
        });
        registerPlaceholder("next_event_remaining_formatted", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            RandomEventLine line = line(args);
            return line == null ? "0" : StringUtils.formatCountdown(line.remainingNextEventSeconds());
        });
        registerPlaceholder("current_event", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            RandomEventLine line = line(args);
            return line == null || line.currentEvent() == null
                    ? ""
                    : line.currentEvent().id();
        });
        registerPlaceholder("current_event_display_name", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            RandomEventLine line = line(args);
            return line == null || line.currentEvent() == null
                    ? ""
                    : ComponentUtils.serialize(line.currentEvent().getDisplayName());
        });
    }

    @Override
    public @NotNull String identifier() {
        return "mittelrandomevents";
    }

    @Override
    public @NotNull String author() {
        return "lijinhong11 (mmmjjkx)";
    }

    @Override
    public @NotNull String version() {
        return plugin.getPluginMeta().getVersion();
    }

    private RandomEvent event(String[] args) {
        return args.length == 0 ? null : plugin.getEventManager().get(args[0]).orElse(null);
    }

    private RandomEventLine line(String[] args) {
        return args.length == 0 ? null : plugin.getLineManager().get(args[0]).orElse(null);
    }
}
