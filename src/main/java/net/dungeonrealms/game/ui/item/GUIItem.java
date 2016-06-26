package net.dungeonrealms.game.ui.item;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.game.ui.GUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */

public abstract class GUIItem {
    private ItemStack item;

    @Setter
    @Getter
    private int slot;

    private GUI gui;

    public GUIItem(ItemStack item) {
        this.item = item;
    }

    public GUIItem(Material material) {
        this.item = new ItemStack(material);
    }

    public GUIItem(int id) {
        this.item = new ItemStack(id);
    }

    public GUIItem(Material material, byte data) {
        this.item = new ItemStack(material, 1, (short) data);
    }

    public GUIItem(Material material, short data) {
        this.item = new ItemStack(material, 1, data);
    }

    public GUIItem(int id, short data) {
        this.item = new ItemStack(id, 1, data);
    }

    public GUIItem(int id, byte data) {
        this.item = new ItemStack(id, 1, (short) data);
    }


    public void setDisplayName(String name) {

        ItemMeta meta = this.item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.item.setItemMeta(meta);

        if (gui != null)
            gui.set(slot, this);
    }

    public void setLore(List<String> lore) {
        ItemMeta meta = this.item.getItemMeta();
        List<String> finalLore = new ArrayList<>();
        String s;
        for (Iterator i$ = lore.iterator(); i$.hasNext(); finalLore.add(ChatColor.translateAlternateColorCodes('&', s))) {
            s = (String) i$.next();
        }
        meta.setLore(finalLore);
        this.item.setItemMeta(meta);

        if (gui != null)
            gui.set(slot, this);
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItemStack() {
        return this.item;
    }

    public void setGui(GUI gui) {
        this.gui = gui;
    }
}
