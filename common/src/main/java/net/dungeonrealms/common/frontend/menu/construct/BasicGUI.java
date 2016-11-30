package net.dungeonrealms.common.frontend.menu.construct;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.dungeonrealms.common.frontend.menu.construct.action.GUIAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class BasicGUI implements IGUI {
    @Getter
    private Inventory inventory;

    @Getter
    private String name;

    @Getter
    private int slots;

    @Getter
    private Map<Integer, GUIAction> actionMap;

    public BasicGUI(InventoryHolder inventoryHolder, String name, int slots) {
        this.inventory = Bukkit.createInventory(inventoryHolder, slots, name);
        this.name = name;
        this.slots = slots;
        this.actionMap = Maps.newHashMap();
    }

    public BasicGUI addAction(GUIAction action, int index) {
        this.actionMap.put(index, action);
        return this;
    }

    public void addItem(ItemStack itemStack, int slot) {
        this.inventory.setItem(slot, itemStack);
    }

    public void addItems(Map<Integer, ItemStack> itemStacks) {
        for (int i : itemStacks.keySet())
            this.inventory.setItem(i, itemStacks.get(i));
    }

    public void fillSlots(boolean emptyOnly, int valueX, int valueY, EnumFillerType fillerType) {
        if (!emptyOnly) {
            for (int i = 0; i < this.slots; i++) {
                if ((i >= valueX && (i <= valueY)))
                    this.inventory.setItem(i, fillerType.getItemStack());
            }
        } else {
            for (int i = 0; i < this.slots; i++) {
                if (this.inventory.getContents()[i] == null) {
                    this.inventory.setItem(i, fillerType.getItemStack());
                }
            }
        }
    }

    @Override
    public Inventory openInventory(Player player) {
        player.openInventory(this.inventory);
        return this.inventory;
    }

    @EventHandler
    public void onAction(InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase(this.name)) {
            if (event.getInventory().getContents() != null) {
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                    if (this.actionMap.containsKey(event.getRawSlot())) // It's an action item
                    {
                        this.actionMap.get(event.getRawSlot()).perform((Player) event.getWhoClicked(), event);
                    }
                }
            }
        }
    }
}
