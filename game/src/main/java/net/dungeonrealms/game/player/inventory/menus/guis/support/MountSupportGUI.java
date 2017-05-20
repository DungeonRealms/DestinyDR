package net.dungeonrealms.game.player.inventory.menus.guis.support;

import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 5/19/2017.
 */
public class MountSupportGUI extends SupportGUI {

    public MountSupportGUI(Player viewer, String other) {
        super(viewer,other,45,other + "'s Ecash Management");
    }

    @Override
    protected void setItems() {

    }
}
