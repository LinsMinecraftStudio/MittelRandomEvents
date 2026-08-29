package io.github.lijinhong11.mittelrandomevents.api;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * A definition that exposes display components directly to API consumers.
 */
public interface Localized {
    /**
     * @return the stable definition ID
     */
    String id();

    /**
     * @param sender the audience, if relevant
     * @return the display name
     */
    Component displayName(@Nullable CommandSender sender);

    /**
     * @param sender the audience, if relevant
     * @return the description
     */
    default Component description(@Nullable CommandSender sender) {
        return Component.empty();
    }

    /**
     * Returns the material used as this definition's GUI icon.
     *
     * @return the GUI icon material
     */
    default Material icon() {
        return Material.PAPER;
    }
}
