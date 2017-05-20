package net.dungeonrealms.game.player.inventory.menus.guis.support;

import lombok.Getter;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;

/**
 * Created by Rar349 on 5/19/2017.
 */
public abstract class SupportGUI extends GUIMenu {

    @Getter
    private final String otherName;
    @Getter
    private PlayerWrapper wrapper;

    public SupportGUI(Player viewer, String otherPlayer, int size, String title) {
        super(viewer,size, title);
        this.otherName = otherPlayer;
        if(!(this instanceof MainSupportGUI)) {
            setPreviousGUI(new MainSupportGUI(player,otherPlayer));
            setShouldOpenPreviousOnClose(true);
        }
    }



    @Override
    public void open(Player player, InventoryAction action) {
        SQLDatabaseAPI.getInstance().getUUIDFromName(otherName,false,(uuid) -> {
            if(uuid == null) {
                player.sendMessage(ChatColor.RED + "This person has never logged into dungeon realms!");
                return;
            }
            Rank.PlayerRank playerRank = Rank.getPlayerRank(uuid);
            if (!Rank.isDev(player) && (playerRank == Rank.PlayerRank.GM || playerRank == Rank.PlayerRank.DEV)) {
                player.sendMessage(ChatColor.RED + "You " + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "DO NOT" + ChatColor.RED + " have permission to manage this user.");
                return;
            }
            PlayerWrapper.getPlayerWrapper(uuid,false,true, (wrapper) -> {
                if(wrapper == null) {
                    player.sendMessage(ChatColor.RED + "Could not load the players wrapper!");
                    return;
                }
                if(wrapper.isPlaying() && Bukkit.getPlayer(uuid) == null) {
                    player.sendMessage(ChatColor.RED + "This player is currently on " + wrapper.getShardPlayingOn());
                    player.sendMessage(ChatColor.GRAY + "Please go onto that shard to use the support tools!");
                    return;
                }
                this.wrapper = wrapper;
                super.open(player, action);
            });
        });
    }
}
