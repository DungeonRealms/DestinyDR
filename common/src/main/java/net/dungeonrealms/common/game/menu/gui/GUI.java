package net.dungeonrealms.common.game.menu.gui;


import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.game.menu.item.GUIButton;
import net.dungeonrealms.common.game.menu.item.GUIItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */

public abstract class GUI extends HashMap<Integer, GUIItem> implements Listener {

    protected Inventory inventory;
    private boolean moveable = false;
    private UUID holder;

    @Getter
    @Setter
    private boolean destroyOnExit = false;

    public GUI(String name, int size) {
        super(new HashMap<>());
        this.inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', name));
    }

    public GUI(String name, int size, UUID holder) {
        super(new HashMap<>());
        this.inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', name));
        this.holder = holder;
    }


    public GUI(JavaPlugin plugin, String name, int size, int destroy) {
        super(new HashMap<>());
        this.inventory = Bukkit.createInventory(null, size, name);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DestroyTask(this), destroy * 20);
    }

    public static GUI createGUI(String name, int size) {
        return new GUI(name, size) {
            @Override
            public Inventory getInventory() {
                return super.getInventory();
            }
        };
    }

    public static GUI createGUI(String name, int size, UUID holder) {
        return new GUI(name, size, holder) {
            @Override
            public Inventory getInventory() {
                return super.getInventory();
            }
        };
    }

    public void register(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onGUIButtonClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        GUI gui = null;

        if ((getHolder() != null && event.getWhoClicked().getUniqueId().equals(getHolder())) &&
                event.getInventory().getTitle().equalsIgnoreCase(getInventory().getTitle())) gui = this;
        else if (getHolder() == null && event.getInventory().getTitle().equalsIgnoreCase(getInventory().getTitle()))
            gui = this;

        if (gui == null) return;
        event.setCancelled(true);

        GUIItem item = gui.getShopItem(event.getRawSlot());
        if (item == null) return;

        if ((item instanceof GUIButton)) {
            try {
                GUIButtonClickEvent buttonEvent = new GUIButtonClickEvent(event, (Player) event.getWhoClicked(), gui, (GUIButton) item, event.getRawSlot());
                Bukkit.getPluginManager().callEvent(buttonEvent);
                ((GUIButton) item).action(buttonEvent);
            } catch (Exception e) {
                e.printStackTrace();

                if (event.getWhoClicked() instanceof Player)
                    event.getWhoClicked().sendMessage(ChatColor.RED + "An error has occurred while clicking button.");
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        GUI gui = null;

        if ((getHolder() != null && event.getPlayer().getUniqueId().equals(getHolder())) &&
                event.getInventory().getTitle().equalsIgnoreCase(getInventory().getTitle())) gui = this;
        else if (getHolder() == null && event.getInventory().getTitle().equalsIgnoreCase(getInventory().getTitle()))
            gui = this;

        if (gui == null) return;
        if (gui instanceof VolatileGUI) ((VolatileGUI) gui).onDestroy(event);
        if (gui.isDestroyOnExit()) gui.remove();
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        GUI gui = null;

        if ((getHolder() != null && event.getPlayer().getUniqueId().equals(getHolder())) &&
                event.getPlayer().getInventory().getTitle().equalsIgnoreCase(getInventory().getTitle())) gui = this;
        else if (getHolder() == null && event.getPlayer().getOpenInventory().getTitle().equalsIgnoreCase(getInventory().getTitle()))
            gui = this;

        if (gui == null) return;
        if (gui instanceof VolatileGUI) ((VolatileGUI) gui).onDestroy(event);
        if (gui.isDestroyOnExit()) gui.remove();
    }

    public UUID getHolder() {
        return holder;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public GUIItem getShopItem(int index) {
        return this.get(index);
    }

    public void remove() {
        HandlerList.unregisterAll(this);
    }

    public boolean isMoveable() {
        return this.moveable;
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }

    public void set(int index, GUIItem item) {
        this.put(index, item);
        this.inventory.setItem(index, item.getItemStack());
    }

    public int getSize() {
        return this.size();
    }

    public int getInventorySize() {
        return this.inventory.getSize();
    }

    public void add(GUIItem item) {
        this.put(this.size(), item);
        this.inventory.addItem(item.getItemStack());
    }

    @Override
    public GUIItem remove(Object key) {
        this.inventory.remove(getShopItem((Integer) key).getItemStack());
        return super.remove(key);
    }

    @Override
    public void clear() {
        super.clear();
        inventory.clear();
    }

    public class DestroyTask
            implements Runnable {
        private GUI shop;

        public DestroyTask(GUI shop) {
            this.shop = shop;
        }

        public void run() {
            HumanEntity player;
            for (Iterator i$ = GUI.this.inventory.getViewers().iterator(); i$.hasNext(); player.closeInventory()) {
                player = (HumanEntity) i$.next();
            }
            shop.remove();
        }
    }


}
