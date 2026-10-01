package io.github.lijinhong11.mittelrandomevents.utils;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

/** Helpers for resolving Bukkit registry-backed values from configuration strings. */
public final class RegistryUtils {
    private RegistryUtils() {}

    public static <T extends Keyed> T get(Registry<T> registry, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.contains(":") ? value : "minecraft:" + value.toLowerCase(java.util.Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(normalized);
        return key == null ? null : registry.get(key);
    }
}
