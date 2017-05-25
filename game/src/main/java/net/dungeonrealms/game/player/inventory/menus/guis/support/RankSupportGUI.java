package net.dungeonrealms.game.player.inventory.menus.guis.support;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 5/19/2017.
 */
public class RankSupportGUI extends SupportGUI {

    public RankSupportGUI(Player viewer, String other) {
        super(viewer,other,27,other + "'s Rank Management");
    }

    @Override
    protected void setItems() {
        int slot = 0;
        PlayerRank viewerRank = Rank.getPlayerRank(player.getUniqueId());
        for(PlayerRank rank : PlayerRank.values()) {
            if(!viewerRank.isAtLeast(rank)) continue; //Can not set someones rank higher than their own.
            setItem(slot++, new GUIItem(Material.BAKED_POTATO).setName(rank.getChatColor() + rank.getChatPrefix()).setLore(ChatColor.WHITE + "Set the users rank to: " + rank.getFullPrefix()).setClick((evt) -> {
                getWrapper().setRank(rank);
                Player p = getWrapper().getPlayer();

                if (p != null && p.isOnline()) {
                    p.sendMessage("                 " + ChatColor.YELLOW + "Your rank is now: " + rank.getPrefix());
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
                }
                getWrapper().setRankExpiration(0);
                Rank.getCachedRanks().put(getWrapper().getUuid(), rank);
                SQLDatabaseAPI.getInstance().addQuery(QueryType.SET_RANK, rank.getInternalName(), getWrapper().getAccountID());
                GameAPI.sendNetworkMessage("Rank", getWrapper().getUuid().toString(), rank.getInternalName());
            }));
        }
    }

}
