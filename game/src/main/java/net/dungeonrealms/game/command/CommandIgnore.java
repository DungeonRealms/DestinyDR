package net.dungeonrealms.game.command;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class CommandIgnore extends BaseCommand {
    public CommandIgnore() {
        super("ignore", "/<command>", "Ignore a players messages.", Lists.newArrayList("block"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Invalid command usage: /ignore <player>");
            return true;
        }

        //Just so they cant light up the database calls for UUIDS.
        if (player.hasMetadata("last_ignore") && player.getMetadata("last_ignore").get(0).asLong() > System.currentTimeMillis()) {
            player.sendMessage(ChatColor.RED + "Please wait before using this command again.");
            return true;
        }
        //2.5s cooldown between ignores..
        player.setMetadata("last_ignore", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 2500));
        String name = args[0];
        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {

            GamePlayer gamePlayer = GameAPI.getGamePlayer(player);

            if (gamePlayer == null) return;

            String sUUID = DatabaseAPI.getInstance().getUUIDFromName(name);

            if (sUUID == null || sUUID.equals("")) {
                player.sendMessage(ChatColor.RED + "Player doesnt not exist.");
                return;
            }

            UUID uuid = UUID.fromString(sUUID);


            boolean alreadyIgnored = gamePlayer.getIgnoredPlayers().contains(uuid.toString());

            String playerRank = Rank.getInstance().getRank(uuid);
            if (Rank.isAtleastPMOD(playerRank)) {
                player.sendMessage(ChatColor.RED + "You cannot ignore that player.");
                return;
            }


            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {

                if (alreadyIgnored)
                    gamePlayer.getIgnoredPlayers().remove(uuid.toString());
                else
                    gamePlayer.getIgnoredPlayers().add(uuid.toString());

                //Once done then save to the database.
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.IGNORED, gamePlayer.getIgnoredPlayers(), true, (result) -> {
                    if (alreadyIgnored) {
                        player.sendMessage(ChatColor.GREEN + "You have removed " + name + " from your ignore list.");
                        player.sendMessage(ChatColor.GRAY + "You will now see that players private messages.");
                    } else {
                        player.sendMessage(ChatColor.RED + "You have ignored " + ChatColor.RED + ChatColor.BOLD + name + ChatColor.RED + "!");
                        player.sendMessage(ChatColor.GRAY + "You will no longer see that players private messages.");
                    }
                });
            });
        });
        return false;
    }
}
