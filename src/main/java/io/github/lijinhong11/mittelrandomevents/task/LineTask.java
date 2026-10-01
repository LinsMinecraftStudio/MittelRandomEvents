package io.github.lijinhong11.mittelrandomevents.task;

import io.github.lijinhong11.mittelrandomevents.api.event.EventContext;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLine;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.function.Supplier;

class LineTask extends AbstractTask {
    private final RandomEventLine line;
    private final Supplier<? extends EventContext> contextSupplier;

    LineTask(RandomEventLine line, Supplier<? extends EventContext> contextSupplier) {
        this.line = line;
        this.contextSupplier = contextSupplier;
    }

    @Override
    protected void run(ScheduledTask task) {
        EventContext context = contextSupplier.get();
        if (context != null) {
            line.execute(context);
        }
    }
}
