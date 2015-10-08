package net.dungeonrealms.handlers;

import net.dungeonrealms.inventory.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Created by Nick on 10/2/2015.
 */
public class ClickHandler {

    static ClickHandler instance = null;

    public static ClickHandler getInstance() {
        if (instance == null) {
            instance = new ClickHandler();
        }
        return instance;
    }

    public void doGuildClick(InventoryClickEvent event) {
        String name = event.getInventory().getName();
        int slot = event.getRawSlot();
        if (slot == -999) return;
        if (name.startsWith("Guild - ")) {
            event.setCancelled(true);
            if (slot > 54) return;
            switch (slot) {
                case 0:
                    Menu.openPlayerGuildLog((Player) event.getWhoClicked());
                    break;
            }
        } else if (name.endsWith("- (Logs)")) {
            event.setCancelled(true);
            if (slot > 36) return;
            switch (slot) {
                case 0:
                    Menu.openPlayerGuildInventory((Player) event.getWhoClicked());
                    break;
            }
        }
    }
}
