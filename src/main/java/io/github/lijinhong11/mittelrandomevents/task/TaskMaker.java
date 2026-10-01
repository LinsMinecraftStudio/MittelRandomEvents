package io.github.lijinhong11.mittelrandomevents.task;

import io.github.lijinhong11.mittellib.utils.task.CronTaskManager;
import io.github.lijinhong11.mittelrandomevents.MittelRandomEvents;
import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLine;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.bukkit.Bukkit;

/**
 * Owns all repeating {@code RandomEventLine} tasks.
 *
 * <p>This class follows the lifecycle pattern used by SuperMines: it creates Paper scheduled
 * tasks, keeps their handles, replaces tasks when a line is restarted and cancels every task on
 * plugin shutdown.
 */
public class TaskMaker {
    private final CronTaskManager cronTaskManager = new CronTaskManager(MittelRandomEvents.getInstance());
    private final Map<String, LineTask> lineTasks = new ConcurrentHashMap<>();
    private volatile boolean closing;

    /**
     * Enables task creation after construction or a reload.
     */
    public void startup() {
        closing = false;
    }

    /**
     * Starts every line using the supplied context factory.
     *
     * @param lines lines to schedule
     * @param contextSupplier factory for one fresh context per execution
     */
    public void startLines(Iterable<RandomEventLine> lines, Supplier<? extends EventContext> contextSupplier) {
        for (RandomEventLine line : lines) {
            startLine(line, contextSupplier);
        }
    }

    /**
     * Starts a repeating task for a line.
     *
     * @param line the line to execute
     * @param contextSupplier supplier used to create a fresh runtime context for each execution
     * @throws IllegalArgumentException if the line interval is invalid
     * @throws RuntimeException if Paper rejects task scheduling
     */
    public void startLine(RandomEventLine line, Supplier<? extends EventContext> contextSupplier) {
        if (closing) {
            return;
        }
        LineTask previous = lineTasks.remove(line.id());
        if (previous != null) {
            previous.cancel();
        }
        cronTaskManager.cancel(line.id());
        if (line.cron() == null && line.intervalSeconds() == 0) {
            return;
        }

        LineTask task = new LineTask(line, contextSupplier);
        lineTasks.put(line.id(), task);
        if (line.cron() != null) {
            cronTaskManager.schedule(line.id(), line.cron(), () -> {
                EventContext context = contextSupplier.get();
                if (context != null) {
                    line.execute(context);
                }
            });
        } else {
            long periodTicks = Math.max(1L, line.intervalSeconds() * 20L);
            ScheduledTask handle = Bukkit.getGlobalRegionScheduler()
                    .runAtFixedRate(MittelRandomEvents.getInstance(), task, periodTicks, periodTicks);
            task.bind(handle);
        }
    }

    /**
     * Cancels and removes a line task.
     *
     * @param lineId the ID of the line task to cancel
     */
    public void cancelLine(String lineId) {
        LineTask task = lineTasks.remove(lineId);
        if (task != null) {
            task.cancel();
        }
        cronTaskManager.cancel(lineId);
    }

    /**
     * Replaces the existing task for a line with a newly scheduled task.
     *
     * @param line the line to restart
     * @param contextSupplier supplier for each execution context
     */
    public void restartLine(RandomEventLine line, Supplier<? extends EventContext> contextSupplier) {
        cancelLine(line.id());
        startLine(line, contextSupplier);
    }

    /** Executes a line once on Paper's global region scheduler. */
    public void runOnce(RandomEventLine line, Supplier<? extends EventContext> contextSupplier) {
        if (closing) {
            return;
        }
        Bukkit.getGlobalRegionScheduler().run(MittelRandomEvents.getInstance(), task -> {
            EventContext context = contextSupplier.get();
            if (context != null) {
                line.execute(context);
            }
        });
    }

    /**
     * Checks whether a line currently has an active task.
     *
     * @param lineId the line ID to inspect
     * @return {@code true} when the line task exists and has not been cancelled
     */
    public boolean isRunning(String lineId) {
        LineTask task = lineTasks.get(lineId);
        return task != null && !task.isCancelled();
    }

    /**
     * Prevents new tasks and cancels all currently managed tasks.
     */
    public void close() {
        closing = true;
        cronTaskManager.shutdown();
        lineTasks.values().forEach(LineTask::cancel);
        lineTasks.clear();
    }

    /**
     * Cancels all tasks and returns the task maker to a state where lines can be started again.
     */
    public void reload() {
        close();
        closing = false;
    }
}
