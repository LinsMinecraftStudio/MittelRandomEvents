package io.github.lijinhong11.mittelrandomevents.builtin;

import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

/**
 * MiniPlaceholders integration.
 *
 * <p>This class is loaded only when MiniPlaceholders is enabled.
 */
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
        TagResolver placeholders = player == null
                ? MiniPlaceholders.globalPlaceholders()
                : TagResolver.resolver(MiniPlaceholders.globalPlaceholders(), MiniPlaceholders.audiencePlaceholders());
        return miniMessage.serialize(miniMessage.deserialize(value, player, placeholders));
    }
}
