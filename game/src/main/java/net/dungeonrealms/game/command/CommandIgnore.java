package net.dungeonrealms.game.command;

import com.google.common.collect.Lists;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.command.friend.CooldownCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class CommandIgnore extends BaseCommand implements CooldownCommand {
    public CommandIgnore() {
        super("ignore", "/<command>", "Ignore a players messages.", Lists.newArrayList("block"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (checkCooldown(player)) return true;

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

        if (name.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "You can not add yourself as a friend!");
            return false;
        }

        SQLDatabaseAPI.getInstance().getUUIDFromName(name, false, (uuid) -> {
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "That player has never played on Dungeon Realms.");
                return;
            }

            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
            if (wrapper == null) return;

            if (Rank.isPMOD(uuid)) {
                player.sendMessage(ChatColor.RED + "You cannot ignore that player.");
                return;
            }


            boolean alreadyIgnored = wrapper.getIgnoredFriends().containsKey(uuid);
            if (alreadyIgnored) {
                player.sendMessage(ChatColor.GREEN + "You have removed " + name + " from your ignore list.");
                player.sendMessage(ChatColor.GRAY + "You will now see that players private messages.");
                wrapper.ignorePlayer(uuid, alreadyIgnored);
            } else {
                player.sendMessage(ChatColor.RED + "You have ignored " + ChatColor.RED + ChatColor.BOLD + name + ChatColor.RED + "!");
                player.sendMessage(ChatColor.GRAY + "You will no longer see that players private messages.");
                wrapper.ignorePlayer(uuid, alreadyIgnored);
            }
            //Ignored.
//            wrapper.ignorePlayer(uuid);
//            wrapper.getIgnoredFriends().put(uuid, SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid));
//            wrapper.getFriendsList().remove(uuid);
//            wrapper.getPendingFriends().remove(uuid);
//            PlayerWrapper.getPlayerWrapper(uuid, false, true, wrap -> {
//                if (wrap == null) {
//                    player.sendMessage(ChatColor.RED + "Unable to load that player.");
//                    return;
//                }
//                if (wrap.getPlayerRank().isAtleast(Rank.PlayerRank.PMOD)) {
//                    player.sendMessage(ChatColor.RED + "You cannot ignore that player!");
//                    return;
//                }
//
//
//            });


        });

//        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
//
//            GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
//
//            if (gamePlayer == null) return;
//
//            String sUUID = DatabaseAPI.getInstance().getUUIDFromName(name);
//
//            if (sUUID == null || sUUID.equals("")) {
//                player.sendMessage(ChatColor.RED + "Player doesnt not exist.");
//                return;
//            }
//
//            UUID uuid = UUID.fromString(sUUID);
//
//
//            boolean alreadyIgnored = gamePlayer.getIgnoredPlayers().contains(uuid.toString());
//
//            String playerRank = Rank.getInstance().getRank(uuid);
//            if (Rank.isAtleastPMOD(playerRank)) {
//                player.sendMessage(ChatColor.RED + "You cannot ignore that player.");
//                return;
//            }
//
//
//            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
//
//                if (alreadyIgnored)
//                    gamePlayer.getIgnoredPlayers().remove(uuid.toString());
//                else
//                    gamePlayer.getIgnoredPlayers().add(uuid.toString());
//
//                //Once done then save to the database.
//                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.IGNORED, gamePlayer.getIgnoredPlayers(), true, (result) -> {
//                    if (alreadyIgnored) {
//                        player.sendMessage(ChatColor.GREEN + "You have removed " + name + " from your ignore list.");
//                        player.sendMessage(ChatColor.GRAY + "You will now see that players private messages.");
//                    } else {
//                        player.sendMessage(ChatColor.RED + "You have ignored " + ChatColor.RED + ChatColor.BOLD + name + ChatColor.RED + "!");
//                        player.sendMessage(ChatColor.GRAY + "You will no longer see that players private messages.");
//                    }
//                });
//            });
//        });
        return false;
    }

    @Override
    public String getName() {
        return "ignore";
    }
}
