package io.github.lijinhong11.mittelrandomevents.builtin;

import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

/** MiniPlaceholders integration. This class is loaded only when MiniPlaceholders is enabled. */
public final class MiniPlaceholdersResolver {
    private MiniPlaceholdersResolver() {}

    /**
     * Resolves MiniPlaceholders for a player audience and serializes the result back to text.
     *
     * @param value the MiniMessage value containing placeholders
     * @param player the player audience
     * @param context the current event context
     * @return the serialized resolved value
     */
    public static String resolve(String value, Player player, EventContext context) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        return miniMessage.serialize(
                miniMessage.deserialize(value, MiniPlaceholders.getAudiencePlaceholders(player)));
    }
}
