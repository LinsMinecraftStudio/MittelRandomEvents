package io.github.lijinhong11.mittelrandomevents.context;

import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEventManager;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Default mutable context created for one RandomEventLine execution.
 */
public final class DefaultEventContext implements EventContext {
    private final RandomEventManager manager;
    private final Random random;
    private final long startedAtMillis;
    private final Map<String, Object> data = new HashMap<>();
    private Collection<Player> selectedPlayers = List.of();
    private Collection<World> selectedWorlds = List.of();

    public DefaultEventContext(RandomEventManager manager) {
        this(manager, new Random());
    }

    public DefaultEventContext(RandomEventManager manager, Random random) {
        this.manager = manager;
        this.random = random;
        this.startedAtMillis = System.currentTimeMillis();
    }

    @Override public RandomEventManager manager() {
        return manager;
    }

    @Override public Random getRandom() {
        return random;
    }

    @Override public Collection<Player> getSelectedPlayers() {
        return List.copyOf(selectedPlayers);
    }

    @Override public void setSelectedPlayers(Collection<? extends Player> players) {
        selectedPlayers = new ArrayList<>(players);
    }

    @Override public Collection<World> getSelectedWorlds() {
        return List.copyOf(selectedWorlds);
    }

    @Override public void setSelectedWorlds(Collection<World> worlds) {
        selectedWorlds = new ArrayList<>(worlds);
    }

    @Override public long elapsedMillis() {
        return System.currentTimeMillis() - startedAtMillis;
    }

    @SuppressWarnings("unchecked")
    @Override public <T> T get(String key) {
        return (T) data.get(key);
    }

    @Override public <T> void set(String key, T value) {
        data.put(key, value);
    }
}
