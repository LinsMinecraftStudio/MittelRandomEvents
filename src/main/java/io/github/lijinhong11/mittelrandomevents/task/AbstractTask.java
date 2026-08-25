package io.github.lijinhong11.mittelrandomevents.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Base implementation for Paper scheduled tasks managed by {@link TaskMaker}.
 *
 * <p>The scheduler supplies the task handle when the task starts. This class binds that handle,
 * prevents execution after cancellation and gives subclasses one protected {@link #run} method
 * for their actual work.
 */
public abstract class AbstractTask implements Consumer<ScheduledTask> {
    private volatile ScheduledTask task;
    private final AtomicBoolean cancelled = new AtomicBoolean();

    /**
     * Associates this wrapper with a Paper scheduled task.
     *
     * <p>If cancellation happened before Paper returned the handle, the handle is cancelled
     * immediately.
     *
     * @param task the scheduled task handle
     */
    public void bind(ScheduledTask task) {
        this.task = task;
        if (cancelled.get() && !task.isCancelled()) {
            task.cancel();
        }
    }

    /**
     * Receives one scheduler callback and delegates to {@link #run(ScheduledTask)} when active.
     *
     * @param task the callback's scheduled task handle
     */
    @Override
    public final void accept(ScheduledTask task) {
        bind(task);
        if (!cancelled.get()) {
            run(task);
        }
    }

    /**
     * Performs one unit of scheduled work.
     *
     * @param task the active scheduled task handle
     */
    protected abstract void run(ScheduledTask task);

    /**
     * Cancels this wrapper and its bound scheduler task, if any.
     */
    public void cancel() {
        cancelled.set(true);
        ScheduledTask current = task;
        if (current != null && !current.isCancelled()) {
            current.cancel();
        }
    }

    /**
     * Checks whether this wrapper has been cancelled.
     *
     * @return {@code true} after {@link #cancel()} has been called
     */
    public boolean isCancelled() {
        return cancelled.get();
    }
}
