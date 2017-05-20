package net.dungeonrealms.game.player.inventory.menus.guis.support;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rar349 on 5/19/2017.
 */
public class RankSupportGUI extends SupportGUI {

    public RankSupportGUI(Player viewer, String other) {
        super(viewer,other,9,other + "'s Rank Management");
    }

    @Override
    protected void setItems() {
        int slot = 0;
        Rank.PlayerRank viewerRank = Rank.getPlayerRank(player.getUniqueId());
        for(Rank.PlayerRank rank : Rank.PlayerRank.values()) {
            if(!viewerRank.isAtLeast(rank)) continue; //Can not set someones rank higher than their own.
            setItem(slot++, new GUIItem(Material.BAKED_POTATO).setName(rank.getChatColor() + rank.getChatPrefix()).setLore(ChatColor.WHITE + "Set the users rank to: " + rank.getFullPrefix()).setClick((evt) -> {
                getWrapper().setRank(rank);
                getWrapper().setRankExpiration(0);
                Rank.getCachedRanks().put(getWrapper().getUuid(), rank);
                SQLDatabaseAPI.getInstance().addQuery(QueryType.SET_RANK, rank.getInternalName(), getWrapper().getAccountID());
                GameAPI.sendNetworkMessage("Rank", getWrapper().getUuid().toString(),rank.getInternalName());
            }));
        }
    }

}
