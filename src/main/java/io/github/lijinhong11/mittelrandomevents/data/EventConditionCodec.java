package io.github.lijinhong11.mittelrandomevents.data;

import io.github.lijinhong11.mittelrandomevents.api.event.EventCondition;
import io.github.lijinhong11.mittelrandomevents.builtin.BuiltInEventCondition;
import io.github.lijinhong11.mittelrandomevents.builtin.BuiltInEventConditionLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Bidirectional map codec for persisted event conditions. */
public final class EventConditionCodec {
    private EventConditionCodec() {}

    public static EventCondition decode(Map<?, ?> values) {
        Object rawType = values.get("type");
        if (rawType == null || String.valueOf(rawType).isBlank()) {
            throw new IllegalArgumentException("Condition type must not be blank");
        }

        Map<String, Object> parameters = stringMap(values.get("parameters"));
        String type = String.valueOf(rawType);
        EventCondition condition =
                switch (type) {
                    case "not" -> BuiltInEventCondition.not(decode(nested(parameters)));
                    case "all_of" -> BuiltInEventCondition.allOf(decodeChildren(parameters));
                    case "any_of" -> BuiltInEventCondition.anyOf(decodeChildren(parameters));
                    default -> BuiltInEventConditionLoader.load(type, parameters);
                };
        if (condition == null) {
            throw new IllegalArgumentException("Unknown condition type: " + rawType);
        }
        return condition;
    }

    public static Map<String, Object> encode(EventCondition condition) {
        String type = condition.type();
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("EventCondition is not serializable: missing type");
        }

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("type", type);
        values.put("parameters", new LinkedHashMap<>(condition.parameters()));
        return values;
    }

    public static Map<String, Object> stringMap(Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) {
            map.forEach((key, nested) -> result.put(String.valueOf(key), nested));
        }
        return result;
    }

    private static Map<String, Object> nested(Map<String, Object> parameters) {
        Object type = parameters.get("type");
        if (type == null) {
            throw new IllegalArgumentException("Nested condition type is missing");
        }
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("type", type);
        nested.put("parameters", parameters.get("parameters"));
        return nested;
    }

    private static List<EventCondition> decodeChildren(Map<String, Object> parameters) {
        Object rawConditions = parameters.get("conditions");
        if (!(rawConditions instanceof Iterable<?> iterable)) {
            throw new IllegalArgumentException("Composite condition requires a conditions list");
        }
        List<EventCondition> conditions = new ArrayList<>();
        for (Object value : iterable) {
            if (!(value instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("Nested condition must be a map");
            }
            conditions.add(decode(map));
        }
        return conditions;
    }
}
