package io.github.lijinhong11.mittelrandomevents.api.registry;

import io.github.lijinhong11.mittelrandomevents.api.event.RandomEvent;

/**
 * Registry containing reusable {@link RandomEvent} definitions.
 */
public final class RandomEventRegistry extends DefaultRegistry<RandomEvent> {
    /**
     * Creates an event registry that uses {@link RandomEvent#id()} as its key.
     */
    public RandomEventRegistry() {
        super(RandomEvent::id);
    }
}
