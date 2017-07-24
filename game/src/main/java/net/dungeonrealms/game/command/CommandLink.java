package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandLink extends BaseCommand {
    public CommandLink() {
        super("link", "/command <usage>", "Link command");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        if(wrapper.getEnjinId() > 0) {
            player.sendMessage(ChatColor.RED + "This account is already linked to the following profile:");
            player.sendMessage(ChatColor.GRAY + ChatColor.UNDERLINE.toString() + "http://www.dungeonrealms.net/profile/" + wrapper.getEnjinId());
            return true;
        }

        if(args.length != 1) {
            player.sendMessage(ChatColor.RED + "You can acquire your link key via the website:");
            player.sendMessage(ChatColor.GRAY + ChatColor.UNDERLINE.toString() + "http://www.dungeonrealms.net/link");
            return true;
        }

        String linkKey = args[0];
        if(!linkKey.matches("[A-Za-z0-9]+") || linkKey.length() != 32) {
            player.sendMessage(ChatColor.RED + "The link key that you have provided is incorrect!");
            player.sendMessage(ChatColor.GRAY + "You may acquire your link key via the website: " + ChatColor.UNDERLINE + "http://www.dungeonrealms.net/link");
            return true;
        }

        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_ENJIN_ID_FROM_LINK.getQuery(linkKey), true, (set) -> {
            try {
                if(set == null || !set.first()) {
                    player.sendMessage(ChatColor.RED + "We were unable to find that link key!");
                    player.sendMessage(ChatColor.GRAY + "Please verify the key is correct and try again.");
                    return;
                }

                int enjinId = set.getInt("enjin_id");
                wrapper.setEnjinId(enjinId);

                SQLDatabaseAPI.getInstance().executeUpdate((set2) -> {
                    System.out.println("Successfully updated the enjin id to " + enjinId + " for " + wrapper.getAccountID());
                }, QueryType.UPDATE_ENJIN_ID.getQuery(enjinId > 0 ? enjinId : null, wrapper.getAccountID()));

                SQLDatabaseAPI.getInstance().executeUpdate((set2) -> {
                    System.out.println("Successfully deleted the link key: " + linkKey);
                }, QueryType.DELETE_LINK_ID.getQuery(linkKey));

                Purchaseables.LOOT_AURA.setNumberOwned(wrapper, Purchaseables.LOOT_AURA.getNumberOwned(wrapper) + 1);

                player.sendMessage(ChatColor.GREEN + "Success! You have linked your account with the profile:");
                player.sendMessage(ChatColor.GRAY + ChatColor.UNDERLINE.toString() + "http://www.dungeonrealms.net/profile/" + wrapper.getEnjinId());
                player.sendMessage(ChatColor.AQUA + "You have received " + ChatColor.BOLD + "1x Loot Aura" + ChatColor.AQUA + " for linking your account.");
            } catch(Exception e) {
                player.sendMessage(ChatColor.RED  + "Something went wrong!");
                player.sendMessage(ChatColor.GRAY + "Please try again later!");
                e.printStackTrace();
            }
        });

        return false;
    }
}
