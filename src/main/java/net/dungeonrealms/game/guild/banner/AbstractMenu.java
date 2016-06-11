package net.dungeonrealms.game.guild.banner;


import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.gui.GUI;
import net.dungeonrealms.game.gui.item.GUIDisplayer;
import net.dungeonrealms.game.gui.item.GUIItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */


public abstract class AbstractMenu extends GUI {

    protected DungeonRealms plugin = DungeonRealms.getInstance();

    private int size;


    public AbstractMenu(String name, int size, UUID uuid) {
        super(name, size, uuid);
        this.size = size;

        register(plugin);
    }

    public AbstractMenu(String name, int size) {
        super(name, size);
        this.size = size;

        register(plugin);
    }

    protected void fillSpace(int slot1, int slot2) {
        for (int i = 0; i < this.size; i++) {
            if ((i >= slot1) && (i <= slot2)) {
                set(i, getSpaceFillerItem());
            }
        }
    }


    protected void fillSpace(GUIItem item, int slot1, int slot2) {
        for (int i = 0; i < this.size; i++) {
            if ((i >= slot1) && (i <= slot2)) {
                set(i, item);
            }
        }
    }

    protected void fillSpace(Iterator<GUIItem> iterator, int slot1, int slot2) {
        for (int i = 0; i < this.size; i++) {
            if ((i >= slot1) && (i <= slot2) && (iterator.hasNext())) {
                set(i, iterator.next());
            }
        }
    }

    protected void fillEmptySpaces(GUIItem item) {
        for (int i = 0; i < size; i++)
            if (!containsKey(i)) set(i, item);
    }

    public static int round(int num) {
        return (num > 54) ? 54 : (num % 9 == 0) ? num : ((num / 9) + 1) * 9;
    }

    public abstract void open(Player player) throws Exception;

    protected GUIItem getSpaceFillerItem() {
        GUIItem g = new GUIDisplayer(Material.STAINED_GLASS_PANE, (byte) 15);
        g.setDisplayName(" ");

        return g;
    }

}
