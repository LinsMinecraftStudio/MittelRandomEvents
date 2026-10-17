package io.github.lijinhong11.mittelrandomevents.builtin;

import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import io.github.lijinhong11.mittelrandomevents.api.event.ParameterContainer;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEventManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Default mutable context created for one RandomEventLine execution.
 */
public final class DefaultEventContext implements EventContext {
    private final RandomEventManager manager;
    private final Random random;
    private final @Nullable CommandSender trialExecutor;
    private final long startedAtMillis;
    private final ParameterContainer parameters = new ParameterContainer();
    private boolean stopped;
    private boolean skipNext;
    private Collection<Player> selectedPlayers;
    private Collection<World> selectedWorlds;

    public DefaultEventContext(RandomEventManager manager) {
        this(manager, new Random());
    }

    public DefaultEventContext(RandomEventManager manager, Random random) {
        this(manager, random, null);
    }

    public DefaultEventContext(RandomEventManager manager, Random random, @Nullable CommandSender trialExecutor) {
        this.manager = manager;
        this.random = random;
        this.trialExecutor = trialExecutor;
        this.startedAtMillis = System.currentTimeMillis();
        this.selectedPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        this.selectedWorlds = new ArrayList<>(Bukkit.getWorlds());
    }

    /** Creates a trial context whose action result messages are sent to the initiating sender. */
    public static DefaultEventContext trialRun(RandomEventManager manager, CommandSender executor) {
        return new DefaultEventContext(manager, new Random(), Objects.requireNonNull(executor, "executor"));
    }

    @Override
    public @Nullable CommandSender trialExecutor() {
        return trialExecutor;
    }

    @Override
    public RandomEventManager manager() {
        return manager;
    }

    @Override
    public Random getRandom() {
        return random;
    }

    @Override
    public Collection<Player> getSelectedPlayers() {
        return List.copyOf(selectedPlayers);
    }

    @Override
    public void setSelectedPlayers(Collection<? extends Player> players) {
        selectedPlayers = new ArrayList<>(players);
    }

    @Override
    public Collection<World> getSelectedWorlds() {
        return List.copyOf(selectedWorlds);
    }

    @Override
    public void setSelectedWorlds(Collection<World> worlds) {
        selectedWorlds = new ArrayList<>(worlds);
    }

    @Override
    public long elapsedMillis() {
        return System.currentTimeMillis() - startedAtMillis;
    }

    @Override
    public ParameterContainer parameters() {
        return parameters;
    }

    @Override
    public void stop() {
        stopped = true;
    }

    @Override
    public void skipNext() {
        if (!stopped) {
            skipNext = true;
        }
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public boolean consumeSkipNext() {
        if (stopped || !skipNext) {
            return false;
        }
        skipNext = false;
        return true;
    }

    @Override
    public void resetExecutionControl() {
        stopped = false;
        skipNext = false;
    }
}
