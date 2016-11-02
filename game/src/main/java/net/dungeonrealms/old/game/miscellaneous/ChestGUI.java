package net.dungeonrealms.old.game.miscellaneous;

import net.dungeonrealms.old.DungeonRealms;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Michiel on 18-5-2016.
 */
public class ChestGUI implements Listener {

    public static ChestGUI create(String title, int rows) {
        return new ChestGUI(title, rows);
    }

    private final Inventory inv;

    private ChestGUI(String title, int rows) {
        inv = Bukkit.createInventory(null, rows * 9, title);
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    private boolean willClose, willDestroy;

    public ChestGUI willClose() {
        willClose = true;
        return this;
    }

    public ChestGUI willDestroy() {
        willDestroy = true;
        return this;
    }

    private final List<Consumer<? super InventoryClickEvent>> clickListeners = new ArrayList<>();

    public ChestGUI clickListener(Consumer<? super InventoryClickEvent> consumer) {
        clickListeners.add(consumer);
        return this;
    }

    private final List<Consumer<? super HumanEntity>> closeListeners = new ArrayList<>();

    public ChestGUI closeListener(Consumer<? super HumanEntity> consumer) {
        closeListeners.add(consumer);
        return this;
    }

    @EventHandler
    void on(InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;
        e.setCancelled(true);
        int raw = e.getRawSlot();
        if (raw < inv.getSize() && raw >= 0) { // In top inventory
            if (willClose) e.getWhoClicked().closeInventory();
            clickListeners.forEach(c -> c.accept(e));
        }
    }

    @EventHandler
    void on(InventoryCloseEvent e) {
        if (!e.getInventory().equals(inv)) return;
        if (willDestroy) HandlerList.unregisterAll(this);
        closeListeners.forEach(c -> c.accept(e.getPlayer()));
    }
}
