package net.dungeonrealms.game.menu;

import net.dungeonrealms.game.menu.item.GUIButton;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */

public class GUIButtonClickEvent
        extends Event
        implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private InventoryClickEvent evnt;
    private GUIButton button;
    private Player player;
    private Inventory inv;
    private GUI gui;
    private int slot;

    public GUIButtonClickEvent(InventoryClickEvent evnt, Player player, GUI gui, GUIButton button, int slot) throws Exception {
        this.evnt = evnt;
        this.player = player;
        this.gui = gui;
        this.button = button;
        this.slot = slot;
        this.inv = this.evnt.getInventory();
    }

    public GUI getGui() {
        return gui;
    }

    public InventoryClickEvent getClickEvent() {
        return this.evnt;
    }

    public Inventory getInventory() {
        return this.inv;
    }

    public int getSlot() {
        return this.slot;
    }

    public GUIButton getClickedButton() {
        return this.button;
    }

    public Player getWhoClicked() {
        return this.player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public boolean isCancelled() {
        return this.evnt.isCancelled();
    }

    public void setCancelled(boolean cancelled) {
        this.evnt.setCancelled(cancelled);
    }
}
