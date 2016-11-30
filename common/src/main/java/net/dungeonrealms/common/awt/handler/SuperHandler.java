package net.dungeonrealms.common.awt.handler;

import org.bukkit.event.Listener;

import java.util.logging.Logger;

/**
 * Created by Giovanni on 29-10-2016.
 */
public interface SuperHandler {
    SuperHandler prepare();

    interface Handler {
        void prepare();

        default Logger getLogger() {
            return Logger.getLogger("DungeonRealms");
        }
    }

    interface ListeningHandler extends Handler, Listener {
    }
}
