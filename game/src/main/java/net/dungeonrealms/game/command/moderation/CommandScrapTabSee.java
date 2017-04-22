package net.dungeonrealms.game.command.moderation;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.miscellaneous.ScrapTier;
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
                    PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
                        if(wrapper == null){
                            player.sendMessage(ChatColor.RED + "No PlayerWrapper found for " + name + " (" + uuid + ")");
                            return;
                        }

                        for (ScrapTier tier : ScrapTier.values()) {
                            Integer amount = wrapper.getCurrencyTab().getScrapCount(tier);

                            player.sendMessage(tier.getChatColor() + tier.getName() + " Scrap: " + (amount == -1 ? "Not Loaded" : amount));
                        }
                        player.sendMessage(ChatColor.RED + "Unlocked Access: " + wrapper.getCurrencyTab().hasAccess);
                    });
                } else {
                    player.sendMessage(ChatColor.RED + "Unable to find uuid for " + name);
                }
            });
        }
        return false;
    }
}
