package io.github.lijinhong11.mittelrandomevents.data;

import io.github.lijinhong11.mittelrandomevents.MittelRandomEvents;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Common YAML file lifecycle used by event and event-line data managers.
 */
public abstract class AbstractYamlDataManager<T> {
    private final File file;
    private YamlConfiguration configuration;

    protected AbstractYamlDataManager(String path) {
        file = new File(MittelRandomEvents.getInstance().getDataFolder(), path);
        if (!file.exists()) {
            MittelRandomEvents.getInstance().saveResource(path, false);
        }
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Returns every top-level object loaded from this file.
     *
     * @return the objects represented by the current YAML configuration
     */

    protected final List<T> loadAll() {
        return configuration.getKeys(false).stream().map(this::load).filter(java.util.Objects::nonNull).toList();
    }

    protected final T load(String id) {
        ConfigurationSection section = configuration.getConfigurationSection(id);
        return section == null ? null : read(section);
    }

    protected final void reloadConfiguration() {
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    protected final void write(String id, T value) {
        ConfigurationSection section = configuration.createSection(id);
        write(section, value);
    }

    protected final void saveConfiguration() {
        try {
            configuration.save(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save " + file.getName(), exception);
        }
    }

    protected abstract T read(ConfigurationSection section);

    protected abstract void write(ConfigurationSection section, T value);

    public abstract void reloadData();

    public abstract void saveAndClose();
}
