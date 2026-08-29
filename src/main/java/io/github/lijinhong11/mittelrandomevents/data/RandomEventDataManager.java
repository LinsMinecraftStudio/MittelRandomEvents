package io.github.lijinhong11.mittelrandomevents.data;

import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEventManager;
import io.github.lijinhong11.mittelrandomevents.api.event.EventCondition;
import io.github.lijinhong11.mittelrandomevents.builtin.BuiltInEventConditionLoader;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and saves {@link RandomEvent} objects from {@code data/events.yml}.
 */
public final class RandomEventDataManager extends AbstractYamlDataManager<RandomEvent> {
    private final RandomEventManager eventManager;

    public RandomEventDataManager(RandomEventManager eventManager) {
        super("data/events.yml");
        this.eventManager = eventManager;
        loadData();
    }

    private void loadData() {
        for (RandomEvent event : loadAll()) {
            eventManager.register(event);
        }
    }

    @Override protected RandomEvent read(ConfigurationSection section) {
        List<EventAction> actions = new ArrayList<>();
        for (Map<?, ?> rawAction : section.getMapList("actions")) {
            Object rawType = rawAction.get("type");
            if (rawType == null) continue;
            Map<String, Object> parameters = new LinkedHashMap<>();
            if (rawAction.get("parameters") instanceof Map<?, ?> map) {
                map.forEach((key, value) -> parameters.put(String.valueOf(key), value));
            }
            actions.add(new EventAction(String.valueOf(rawType), parameters));
        }
        RandomEvent event = new RandomEvent(section.getName(), actions);
        String displayName = section.getString("display-name");
        if (displayName != null) {
            event.setDisplayName(io.github.lijinhong11.mittellib.utils.components.ComponentUtils.deserialize(displayName));
        }
        event.setEnabled(section.getBoolean("enabled", true));
        event.setIcon(material(section.getString("icon"), event.getIcon()));
        List<EventCondition> conditions = new ArrayList<>();
        for (Map<?, ?> rawCondition : section.getMapList("conditions")) {
            Object rawType = rawCondition.get("type");
            if (rawType == null) {
                continue;
            }
            Map<String, Object> parameters = new LinkedHashMap<>();
            if (rawCondition.get("parameters") instanceof Map<?, ?> map) {
                map.forEach((key, value) -> parameters.put(String.valueOf(key), value));
            }
            try {
                EventCondition condition = BuiltInEventConditionLoader.load(
                        String.valueOf(rawType), parameters);
                if (condition != null) {
                    conditions.add(condition);
                }
            } catch (IllegalArgumentException exception) {
                // Invalid conditions do not prevent other events from loading.
            }
        }
        event.setConditions(conditions);
        return event;
    }

    @Override protected void write(ConfigurationSection section, RandomEvent event) {
        section.set("enabled", event.isEnabled());
        section.set("display-name", io.github.lijinhong11.mittellib.utils.components.ComponentUtils.serialize(event.getDisplayName()));
        section.set("icon", event.getIcon().name());
        section.set("conditions", event.conditions().stream()
                .filter(condition -> condition.type() != null)
                .map(condition -> {
                    Map<String, Object> values = new LinkedHashMap<>();
                    values.put("type", condition.type());
                    values.put("parameters", condition.parameters());
                    return values;
                })
                .toList());
        section.set("actions", event.actions().stream().map(action -> {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("type", action.type());
            values.put("parameters", action.parameters());
            return values;
        }).toList());
    }

    @Override public void reloadData() {
        eventManager.eventsRegistry().clear();
        reloadConfiguration();
        loadData();
    }

    @Override public void saveAndClose() {
        for (RandomEvent event : eventManager.events()) write(event.id(), event);
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
