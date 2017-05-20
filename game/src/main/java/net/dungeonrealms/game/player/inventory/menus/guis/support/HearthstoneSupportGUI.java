package net.dungeonrealms.game.player.inventory.menus.guis.support;

import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 5/19/2017.
 */
public class HearthstoneSupportGUI extends SupportGUI {

    public HearthstoneSupportGUI(Player viewer, String other) {
        super(viewer,other,45,other + "'s Hearthstone Management");
    }

    @Override
    protected void setItems() {
        int slot = 0;
        for(TeleportLocation loc : TeleportLocation.values()) {
            setItem(slot++,new GUIItem(Material.QUARTZ).setName(loc.getDisplayName()).setLore("Click to set their hearthstone location to: " + loc.getDisplayName()).setClick((evt)-> {
                getWrapper().setHearthstone(loc);
                getWrapper().runQuery(QueryType.UPDATE_HEARTH_STONE, loc.getDBString(), getWrapper().getCharacterID());
                player.sendMessage("Success!");
            }));
        }

    }
}
