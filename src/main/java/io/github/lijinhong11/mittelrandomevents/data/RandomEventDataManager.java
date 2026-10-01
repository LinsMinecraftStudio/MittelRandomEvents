package io.github.lijinhong11.mittelrandomevents.data;

import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittelrandomevents.api.action.EventAction;
import io.github.lijinhong11.mittelrandomevents.api.event.EventCondition;
import io.github.lijinhong11.mittelrandomevents.api.event.ParameterContainer;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEventManager;
import io.github.lijinhong11.mittelrandomevents.utils.RegistryUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;

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

    @Override
    protected RandomEvent read(ConfigurationSection section) {
        List<Map<?, ?>> rawActions = section.getMapList("start-actions");
        if (rawActions.isEmpty()) {
            rawActions = section.getMapList("actions");
        }
        List<EventAction> actions = readActions(rawActions);
        List<EventAction> endActions = readActions(section.getMapList("end-actions"));
        RandomEvent event = new RandomEvent(section.getName(), actions, endActions);
        String displayName = section.getString("display-name");
        if (displayName != null) {
            event.setDisplayName(ComponentUtils.deserialize(displayName));
        }
        event.setEnabled(section.getBoolean("enabled", true));
        event.setIcon(material(section.getString("icon"), event.getIcon()));
        List<EventCondition> conditions = new ArrayList<>();
        for (Map<?, ?> rawCondition : section.getMapList("conditions")) {
            conditions.add(EventConditionCodec.decode(rawCondition));
        }
        event.setConditions(conditions);
        return event;
    }

    private static List<EventAction> readActions(List<Map<?, ?>> rawActions) {
        List<EventAction> actions = new ArrayList<>();
        for (Map<?, ?> rawAction : rawActions) {
            Object rawType = rawAction.get("type");
            if (rawType == null) continue;
            Map<String, Object> parameters = new LinkedHashMap<>();
            if (rawAction.get("parameters") instanceof Map<?, ?> map) {
                map.forEach((key, value) -> parameters.put(String.valueOf(key), value));
            }
            List<EventCondition> actionConditions = new ArrayList<>();
            if (rawAction.get("conditions") instanceof Iterable<?> rawConditions) {
                for (Object rawCondition : rawConditions) {
                    if (rawCondition instanceof Map<?, ?> map) {
                        actionConditions.add(EventConditionCodec.decode(map));
                    }
                }
            }
            actions.add(new EventAction(String.valueOf(rawType), new ParameterContainer(parameters), actionConditions));
        }
        return actions;
    }

    @Override
    protected void write(ConfigurationSection section, RandomEvent event) {
        section.set("enabled", event.isEnabled());
        section.set("display-name", ComponentUtils.serialize(event.getDisplayName()));
        section.set("icon", event.getIcon().name());
        section.set(
                "conditions",
                event.conditions().stream().map(EventConditionCodec::encode).toList());
        section.set("start-actions", encodeActions(event.actions()));
        section.set("end-actions", encodeActions(event.endActions()));
    }

    private static List<Map<String, Object>> encodeActions(List<EventAction> actions) {
        return actions.stream()
                .map(action -> {
                    Map<String, Object> values = new LinkedHashMap<>();
                    values.put("type", action.type());
                    values.put("parameters", action.parameters().asMap());
                    values.put(
                            "conditions",
                            action.conditions().stream()
                                    .map(EventConditionCodec::encode)
                                    .toList());
                    return values;
                })
                .toList();
    }

    @Override
    public void reloadData() {
        eventManager.eventsRegistry().clear();
        reloadConfiguration();
        loadData();
    }

    @Override
    public void saveAndClose() {
        saveData();
    }

    public void saveData() {
        clearConfiguration();
        for (RandomEvent event : eventManager.events()) write(event.id(), event);
        saveConfiguration();
    }

    private static org.bukkit.Material material(String value, org.bukkit.Material fallback) {
        if (value == null) {
            return fallback;
        }
        org.bukkit.Material material = RegistryUtils.get(Registry.MATERIAL, value);
        return material == null ? fallback : material;
    }
}
