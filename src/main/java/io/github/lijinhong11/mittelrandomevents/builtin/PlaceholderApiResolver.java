package io.github.lijinhong11.mittelrandomevents.builtin;

import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/**
 * PlaceholderAPI integration.
 *
 * <p>This class is loaded only when PlaceholderAPI is enabled.
 */
public final class PlaceholderApiResolver {
    private PlaceholderApiResolver() {}

    /**
     * Resolves PlaceholderAPI placeholders for a player.
     *
     * @param value the value containing placeholders
     * @param player the player context
     * @param context the current event context
     * @return the resolved value
     */
    public static String resolve(String value, Player player, EventContext context) {
        return PlaceholderAPI.setPlaceholders(player, value);
    }

    /**
     * Resolves server-level PlaceholderAPI placeholders.
     *
     * @param value the value containing placeholders
     * @param context the current event context
     * @return the resolved value
     */
    public static String resolve(String value, EventContext context) {
        return PlaceholderAPI.setPlaceholders(null, value);
    }
}
