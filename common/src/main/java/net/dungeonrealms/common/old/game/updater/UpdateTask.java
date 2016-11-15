package net.dungeonrealms.common.old.game.updater;

import org.bukkit.plugin.java.JavaPlugin;

public class UpdateTask
        implements Runnable {
    private JavaPlugin _plugin;

    public UpdateTask(JavaPlugin plugin) {
        this._plugin = plugin;
        this._plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this._plugin, this, 0L, 1L);
    }

    public void run() {
        for (UpdateType updateType : UpdateType.values()) {
            if (updateType.Elapsed()) {
                this._plugin.getServer().getPluginManager().callEvent(new UpdateEvent(updateType));
            }
        }
    }
}
