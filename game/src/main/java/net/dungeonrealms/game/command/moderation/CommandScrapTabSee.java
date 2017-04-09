package net.dungeonrealms.game.command.moderation;

import com.google.common.collect.Lists;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mechanic.data.ScrapTier;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

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


            Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
                String id = DatabaseAPI.getInstance().getUUIDFromName(name);
                if (id != null && !id.equals("")) {
                    UUID uuid = UUID.fromString(id);
                    Document document = DatabaseAPI.getInstance().getDocument(uuid);
                    if (document == null) {
                        player.sendMessage(ChatColor.RED + "No document loaded for " + name);
                        return;
                    }

                    Document currencyDoc = (Document) document.get("currencytab");
                    if (currencyDoc == null) {
                        player.sendMessage(ChatColor.RED + "No currency tab in the database for " + name);
                        return;
                    }
                    Bukkit.getLogger().info("Loaded currency tab: " + currencyDoc.toString());

                    for (ScrapTier tier : ScrapTier.values()) {

                        int found;

                        Integer amount = currencyDoc.getInteger(tier.getDbData().getKey().split("\\.")[1]);

                        found = amount == null ? -1 : amount;
                        player.sendMessage(tier.getChatColor() + tier.getName() + " Scrap: " + (found == -1 ? "Not Loaded" : found));
                    }
                    player.sendMessage(ChatColor.RED + "Unlocked Access: " + currencyDoc.getBoolean("access"));
                } else {
                    player.sendMessage(ChatColor.RED + "Unable to find uuid for " + name);
                }
            });
        }
        return false;
    }
}
