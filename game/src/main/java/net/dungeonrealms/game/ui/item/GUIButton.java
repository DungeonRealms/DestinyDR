package net.dungeonrealms.game.ui.item;

import net.dungeonrealms.game.ui.GUIButtonClickEvent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */

public abstract class GUIButton
        extends GUIItem {

    public GUIButton(ItemStack item) {
        super(item);
    }

    public GUIButton(Material material) {
        super(material);
    }

    public GUIButton(int id) {
        super(id);
    }

    public GUIButton(Material material, byte data) {
        super(material, data);
    }

    public GUIButton(Material material, short data) {
        super(material, data);
    }

    public GUIButton(int id, byte data) {
        super(id, data);
    }

    public GUIButton(int id, short data) {
        super(id, data);
    }


    public abstract void action(GUIButtonClickEvent paramGUIButtonClickEvent) throws Exception;
}
