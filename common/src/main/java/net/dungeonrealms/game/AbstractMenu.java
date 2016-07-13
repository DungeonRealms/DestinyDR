package net.dungeonrealms.game;


import net.dungeonrealms.game.menu.GUI;
import net.dungeonrealms.game.menu.item.GUIDisplayer;
import net.dungeonrealms.game.menu.item.GUIItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */


public abstract class AbstractMenu extends GUI {

    private final int size;

    public AbstractMenu(JavaPlugin plugin, String name, int size, UUID uuid) {
        super(name, size, uuid);
        this.size = size;

        register(plugin);
    }

    public AbstractMenu(JavaPlugin plugin, String name, int size) {
        super(name, size);
        this.size = size;

        register(plugin);
    }

    public static int round(int num) {
        return (num > 54) ? 54 : (num % 9 == 0) ? num : ((num / 9) + 1) * 9;
    }

    public void fillSpace(int slot1, int slot2) {
        for (int i = 0; i < this.size; i++) {
            if ((i >= slot1) && (i <= slot2)) {
                set(i, getSpaceFillerItem());
            }
        }
    }

    public void fillSpace(GUIItem item, int slot1, int slot2) {
        for (int i = 0; i < this.size; i++) {
            if ((i >= slot1) && (i <= slot2)) {
                set(i, item);
            }
        }
    }

    public void fillSpace(Iterator<GUIItem> iterator, int slot1, int slot2) {
        for (int i = 0; i < this.size; i++) {
            if ((i >= slot1) && (i <= slot2) && (iterator.hasNext())) {
                set(i, iterator.next());
            }
        }
    }

    public void fillEmptySpaces(GUIItem item) {
        for (int i = 0; i < size; i++)
            if (!containsKey(i)) set(i, item);
    }

    public abstract void open(Player player) throws Exception;

    public GUIItem getSpaceFillerItem() {
        GUIItem g = new GUIDisplayer(Material.THIN_GLASS);
        g.setDisplayName(" ");

        return g;
    }

}
