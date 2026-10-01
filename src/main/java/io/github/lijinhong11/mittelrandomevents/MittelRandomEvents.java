package io.github.lijinhong11.mittelrandomevents;

import io.github.lijinhong11.mittellib.MittelLib;
import io.github.lijinhong11.mittellib.configuration.MittelConfig;
import io.github.lijinhong11.mittellib.message.SyncLanguageManager;
import io.github.lijinhong11.mittelrandomevents.api.event.DefaultRandomEventManager;
import io.github.lijinhong11.mittelrandomevents.api.event.RandomEventManager;
import io.github.lijinhong11.mittelrandomevents.api.line.DefaultRandomEventLineManager;
import io.github.lijinhong11.mittelrandomevents.api.line.RandomEventLineManager;
import io.github.lijinhong11.mittelrandomevents.builtin.BuiltInActionType;
import io.github.lijinhong11.mittelrandomevents.command.MRECommands;
import io.github.lijinhong11.mittelrandomevents.context.DefaultEventContext;
import io.github.lijinhong11.mittelrandomevents.data.RandomEventDataManager;
import io.github.lijinhong11.mittelrandomevents.data.RandomEventLineDataManager;
import io.github.lijinhong11.mittelrandomevents.hook.MittelRandomEventsExpansion;
import io.github.lijinhong11.mittelrandomevents.task.TaskMaker;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.List;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class MittelRandomEvents extends JavaPlugin {
    @Getter
    private static MittelRandomEvents instance;

    private SyncLanguageManager languageManager;
    private TaskMaker taskMaker;
    private RandomEventManager eventManager;
    private RandomEventLineManager lineManager;
    private RandomEventDataManager eventDataManager;
    private RandomEventLineDataManager lineDataManager;

    private MittelConfig pluginConfig;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        pluginConfig = MittelConfig.load(this, "config.yml");
        languageManager = MittelLib.getInstance().getLanguageManager(this);
        taskMaker = new TaskMaker();
        eventManager = new DefaultRandomEventManager();
        lineManager = new DefaultRandomEventLineManager();

        for (BuiltInActionType actionType : BuiltInActionType.values()) {
            eventManager.registerActionType(actionType);
        }

        new MittelRandomEventsExpansion(this).register();

        eventDataManager = new RandomEventDataManager(eventManager);
        lineDataManager = new RandomEventLineDataManager(eventManager, lineManager);
        taskMaker.startup();
        startLoadedLines();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, c -> c.registrar()
                .register(MRECommands.get(), List.of("mre", "randomevents")));

        getLogger().info("MittelRandomEvents is enabled!");
    }

    @Override
    public void onDisable() {
        if (taskMaker != null) {
            taskMaker.close();
        }
        if (lineDataManager != null) {
            lineDataManager.saveAndClose();
        }
        if (eventDataManager != null) {
            eventDataManager.saveAndClose();
        }
        getLogger().info("MittelRandomEvents is disabled!");
    }

    /**
     * Reloads events first and lines second so line references always resolve against current
     * event objects.
     */
    public void reloadData() {
        taskMaker.close();
        eventDataManager.reloadData();
        lineDataManager.reloadData();
        taskMaker.startup();
        startLoadedLines();
    }

    public void saveData() {
        if (eventDataManager != null) {
            eventDataManager.saveData();
        }
        if (lineDataManager != null) {
            lineDataManager.saveData();
        }
    }

    private void startLoadedLines() {
        taskMaker.startLines(lineManager.lines(), () -> new DefaultEventContext(eventManager));
    }
}
