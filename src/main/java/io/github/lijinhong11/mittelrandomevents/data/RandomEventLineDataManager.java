package io.github.lijinhong11.mittelrandomevents.data;

import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEventManager;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLine;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLineManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads and saves {@link RandomEventLine} objects from {@code data/lines.yml}. */
public final class RandomEventLineDataManager extends AbstractYamlDataManager<RandomEventLine> {
    private final RandomEventManager eventManager;
    private final RandomEventLineManager lineManager;

    public RandomEventLineDataManager(
            RandomEventManager eventManager, RandomEventLineManager lineManager) {
        super("data/lines.yml");
        this.eventManager = eventManager;
        this.lineManager = lineManager;
        loadData();
    }

    private void loadData() {
        for (RandomEventLine line : loadAll()) lineManager.register(line);
    }

    @Override
    protected RandomEventLine read(ConfigurationSection section) {
        List<RandomEvent> events = new ArrayList<>();
        Map<String, Double> configuredWeights = new LinkedHashMap<>();
        ConfigurationSection eventSection = section.getConfigurationSection("events");
        if (eventSection != null) {
            for (String eventId : eventSection.getKeys(false)) {
                double weight = eventSection.getDouble(eventId, 1.0D);
                if (eventSection.isConfigurationSection(eventId)) {
                    ConfigurationSection eventEntry = eventSection.getConfigurationSection(eventId);
                    if (eventEntry != null && eventEntry.getBoolean("empty", false)) {
                        events.add(RandomEvent.empty(eventId));
                        weight = eventEntry.getDouble("weight", 1.0D);
                        configuredWeights.put(eventId, weight);
                        continue;
                    }
                }
                double entryWeight = weight;
                eventManager.get(eventId).ifPresent(event -> {
                    events.add(event);
                    configuredWeights.put(eventId, entryWeight);
                });
            }
        }
        RandomEventLine line = new RandomEventLine(
                section.getName(), section.getInt("interval-seconds", 60), events);
        String displayName = section.getString("display-name");
        if (displayName != null) {
            line.setDisplayName(io.github.lijinhong11.mittellib.utils.components.ComponentUtils.deserialize(displayName));
        }
        line.setIcon(material(section.getString("icon"), line.getIcon()));
        if (eventSection != null) {
            for (RandomEvent event : events) {
                line.setWeight(event, configuredWeights.getOrDefault(event.id(), 1.0D));
            }
        }
        return line;
    }

    @Override
    protected void write(ConfigurationSection section, RandomEventLine line) {
        section.set("interval-seconds", line.intervalSeconds());
        section.set("display-name", io.github.lijinhong11.mittellib.utils.components.ComponentUtils.serialize(line.getDisplayName()));
        section.set("icon", line.getIcon().name());
        ConfigurationSection events = section.createSection("events");
        for (RandomEvent event : line.events()) {
            if (event.actions().isEmpty()) {
                ConfigurationSection empty = events.createSection(event.id());
                empty.set("empty", true);
                empty.set("weight", line.weightOf(event));
            } else {
                events.set(event.id(), line.weightOf(event));
            }
        }
    }

    public Collection<RandomEventLine> lines() {
        return lineManager.lines();
    }

    public RandomEventLine get(String id) {
        return lineManager.get(id).orElse(null);
    }

    @Override
    public void reloadData() {
        lineManager.clear();
        reloadConfiguration();
        loadData();
    }

    @Override
    public void saveAndClose() {
        for (RandomEventLine line : lineManager.lines()) write(line.id(), line);
        saveConfiguration();
    }

    private static org.bukkit.Material material(String value, org.bukkit.Material fallback) {
        if (value == null) {
            return fallback;
        }
        org.bukkit.Material material = org.bukkit.Material.matchMaterial(value);
        return material == null ? fallback : material;
    }
}
