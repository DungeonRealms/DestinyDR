package net.dungeonrealms.game.player.inventory.menus.guis.support;

import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 5/19/2017.
 */
public class BankSupportGUI extends SupportGUI {

    public BankSupportGUI(Player viewer, String other) {
        super(viewer, other, 45, other + "'s Bank Management");
    }

    @Override
    protected void setItems() {
        setItem(4, new GUIItem(Material.BAKED_POTATO).setName(ChatColor.GOLD + ChatColor.BOLD.toString() + "Set Gems").setLore(ChatColor.GRAY + "Click here to set " + getOtherName() + "'s gems.","",ChatColor.GREEN + "Current Gems: " + ChatColor.BOLD + getWrapper().getGems()).setClick((evt) -> {
            Chat.listenForMessage(player, customAmount -> {
                if (!customAmount.getMessage().equalsIgnoreCase("cancel") && !customAmount.getMessage().equalsIgnoreCase("exit")) {
                    try {
                        int amount = Integer.parseInt(customAmount.getMessage());
                        getWrapper().setGems(amount);
                        getWrapper().runQuery(QueryType.SET_GEMS, getWrapper().getGems(), getWrapper().getCharacterID());
                        player.sendMessage(ChatColor.GREEN + "Successfully set " + getOtherName() + "'s gems to " + customAmount);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + customAmount.getMessage() + " is not a valid number.");
                    }
                }
            });
        }));
    }
}
