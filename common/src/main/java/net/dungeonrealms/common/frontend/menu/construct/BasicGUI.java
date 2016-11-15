package net.dungeonrealms.common.frontend.menu.construct;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
public class BasicGUI implements IGUI
{
    @Getter
    private Inventory inventory;

    @Getter
    private String name;

    @Getter
    private int slots;

    public BasicGUI(InventoryHolder inventoryHolder, String name, int slots)
    {
        this.inventory = Bukkit.createInventory(inventoryHolder, slots, name);
        this.name = name;
        this.slots = slots;
    }

    public void addItem(ItemStack itemStack, int slot)
    {
        this.inventory.setItem(slot, itemStack);
    }

    public void addItems(Map<Integer, ItemStack> itemStacks)
    {
        for (int i : itemStacks.keySet())
            this.inventory.setItem(i, itemStacks.get(i));
    }

    public void fillSlots(boolean emptyOnly, int valueX, int valueY, EnumFillerType fillerType)
    {
        if (!emptyOnly)
        {
            for (int i = 0; i < this.slots; i++)
            {
                if ((i >= valueX && (i <= valueY)))
                    this.inventory.setItem(i, fillerType.getItemStack());
            }
        } else
        {
            for (int i = 0; i < this.slots; i++)
            {
                if (this.inventory.getContents()[i] == null)
                {
                    this.inventory.setItem(i, fillerType.getItemStack());
                }
            }
        }
    }

    @Override
    public Inventory openInventory(Player player)
    {
        player.openInventory(this.inventory);
        return this.inventory;
    }
}
