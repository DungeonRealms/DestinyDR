package net.dungeonrealms.game.command.moderation;

import com.google.common.collect.Lists;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.data.ScrapTier;

import net.dungeonrealms.game.player.inventory.menus.guis.support.CharacterSelectionGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandScrapTabSee extends BaseCommand {

    public CommandScrapTabSee() {
        super("scraptabsee", "/command <name>", "View a players scraptab.", Lists.newArrayList("currencytabsee", "scrapsee"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (Rank.isTrialGM(player)) {
            //View the scrap of thier target

            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Invalid usage: /scrapsee <name>");
                return true;
            }

            String name = args[0];


            SQLDatabaseAPI.getInstance().getUUIDFromName(name, false, uuid -> {
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "Unable to find uuid for " + name);
                    return;
                }

                Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);
                if(accountID == null) {
                    sender.sendMessage(org.bukkit.ChatColor.RED + "This player has never logged in with Dungeon Realms");
                    return;
                }


                new CharacterSelectionGUI(player,accountID, (charID) -> {
                    PlayerWrapper.getPlayerWrapper(uuid, charID,false, true, (wrapper) -> {
                        if (wrapper == null) {
                            player.sendMessage(ChatColor.RED + "No PlayerWrapper found for " + name + " (" + uuid + ")");
                            return;
                        }
                        if(wrapper.getCurrencyTab() == null) {
                            player.sendMessage(ChatColor.RED + "Unlocked Access: false");
                            return;
                        }
                        player.sendMessage(ChatColor.RED + "Unlocked Access: " + (wrapper.getCurrencyTab() != null ? wrapper.getCurrencyTab().hasAccess : "false"));
                        if(!wrapper.getCurrencyTab().hasAccess) {
                            return;
                        }
                        for (ScrapTier tier : ScrapTier.values()) {
                            Integer amount = wrapper.getCurrencyTab() == null ? -1 : wrapper.getCurrencyTab().getScrapCount(tier);

                            player.sendMessage(tier.getChatColor() + tier.getName() + " Scrap: " + (amount == -1 ? "Not Loaded" : amount));
                        }
                    });
                }).open(player,null);
            });
        }
        return false;
    }
}
