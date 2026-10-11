package io.github.lijinhong11.mittelrandomevents.builtin;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

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
     * @return the resolved value
     */
    public static String resolve(String value, @Nullable Player player) {
        return PlaceholderAPI.setPlaceholders(player, value);
    }
}
