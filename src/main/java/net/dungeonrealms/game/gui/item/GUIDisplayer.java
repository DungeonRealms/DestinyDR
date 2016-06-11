package net.dungeonrealms.game.gui.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */


public class GUIDisplayer
        extends GUIItem {

    public GUIDisplayer(ItemStack item) {
        super(item);
    }

    public GUIDisplayer(Material material) {
        super(material);
    }

    public GUIDisplayer(int id) {
        super(id);
    }

    public GUIDisplayer(Material material, byte data) {
        super(material, data);
    }

    public GUIDisplayer(Material material, short data) {
        super(material, data);
    }

    public GUIDisplayer(int id, byte data) {
        super(id, data);
    }

    public GUIDisplayer(int id, short data) {
        super(id, data);
    }
}
