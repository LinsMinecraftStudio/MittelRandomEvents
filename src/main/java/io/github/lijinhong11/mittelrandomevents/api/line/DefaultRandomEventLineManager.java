package io.github.lijinhong11.mittelrandomevents.api.line;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Default insertion-ordered manager for random event lines. */
public final class DefaultRandomEventLineManager implements RandomEventLineManager {
    private final Map<String, RandomEventLine> lines = new LinkedHashMap<>();

    @Override
    public void register(RandomEventLine line) {
        if (line == null || line.id() == null || line.id().isBlank()) {
            throw new IllegalArgumentException("Line id must not be blank");
        }
        lines.put(line.id(), line);
    }

    @Override
    public void unregister(String id) {
        lines.remove(id);
    }

    @Override
    public Optional<RandomEventLine> get(String id) {
        return Optional.ofNullable(lines.get(id));
    }

    @Override
    public Collection<RandomEventLine> lines() {
        return Collections.unmodifiableCollection(lines.values());
    }

    @Override
    public void clear() {
        lines.clear();
    }
}
