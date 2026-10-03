package io.github.lijinhong11.mittelrandomevents.data;

import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEventManager;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLine;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLineManager;
import io.github.lijinhong11.mittelrandomevents.utils.RegistryUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Reads and saves {@link RandomEventLine} objects from {@code data/lines.yml}.
 */
public final class RandomEventLineDataManager extends AbstractYamlDataManager<RandomEventLine> {
    private final RandomEventManager eventManager;
    private final RandomEventLineManager lineManager;

    public RandomEventLineDataManager(RandomEventManager eventManager, RandomEventLineManager lineManager) {
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
        if (section.contains("interval-seconds")) {
            section.set("interval-seconds", null);
        }
        List<RandomEvent> events = new ArrayList<>();
        Map<String, Double> configuredWeights = new LinkedHashMap<>();
        ConfigurationSection eventSection = section.getConfigurationSection("events");
        if (eventSection != null) {
            for (String eventId : eventSection.getKeys(false)) {
                double weight = eventSection.getDouble(eventId, 1.0D);
                if (eventSection.isConfigurationSection(eventId)) {
                    ConfigurationSection eventEntry = eventSection.getConfigurationSection(eventId);
                    if (eventEntry != null
                            && (eventEntry.getBoolean("empty", false)
                                    || "empty".equals(eventEntry.getString("kind")))) {
                        events.add(RandomEvent.empty(eventId));
                        weight = eventEntry.getDouble("weight", 1.0D);
                        configuredWeights.put(eventId, weight);
                        continue;
                    }
                    if (eventEntry != null) {
                        weight = eventEntry.getDouble("weight", 1.0D);
                    }
                }
                double entryWeight = weight;
                eventManager.get(eventId).ifPresent(event -> {
                    events.add(event);
                    configuredWeights.put(eventId, entryWeight);
                });
            }
        }
        RandomEventLine line = new RandomEventLine(section.getName(), events);
        String cron = section.getString("cron");
        if (cron != null && !cron.isBlank()) {
            line.setCron(cron);
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
        section.set("cron", line.cron());
        section.set("icon", line.getIcon().name());
        ConfigurationSection events = section.createSection("events");
        for (RandomEvent event : line.events()) {
            ConfigurationSection entry = events.createSection(event.id());
            boolean reference = eventManager
                    .get(event.id())
                    .map(registered -> registered == event)
                    .orElse(false);
            entry.set("kind", reference ? "reference" : "empty");
            entry.set("weight", line.weightOf(event));
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
        saveData();
    }

    public void saveData() {
        clearConfiguration();
        for (RandomEventLine line : lineManager.lines()) write(line.id(), line);
        saveConfiguration();
    }

    private static Material material(String value, Material fallback) {
        if (value == null) {
            return fallback;
        }

        return RegistryUtils.get(Registry.MATERIAL, value);
    }
}
