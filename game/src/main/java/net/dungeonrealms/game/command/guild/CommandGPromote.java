package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.guild.GuildMember;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandGPromote extends BaseCommand {

    public CommandGPromote(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        PlayerWrapper playerWrapper = PlayerWrapper.getPlayerWrapper(player);
        GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(playerWrapper.getGuildID());
        if (args.length == 0) {
            player.sendMessage(usage);
            return true;
        }

        if (wrapper == null) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gpromote <player>.");
            return true;
        }

//        String guildName =
//        String guildName = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());
//        String displayName = GuildDatabaseAPI.get().getDisplayNameOf(guildName);

//        if (!GuildDatabaseAPI.get().isOwner(player.getUniqueId(), guildName) && !Rank.isGM(player)) {
//            player.sendMessage(ChatColor.RED + "You must be the " + ChatColor.BOLD + "GUILD OWNER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gpromote <player>.");
//            return true;
//        }

        String p_name = args[0];

        if (p_name.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot promote yourself.");
            return true;
        }

//        if (DatabaseAPI.getInstance().getUUIDFromName(args[0]).equals("")) {
//            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " does not exist in our database.");
//            return true;
//        }

        SQLDatabaseAPI.getInstance().getUUIDFromName(p_name, false, uuid -> {
            if (uuid == null) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " does not exist in our database.");
                return;
            }

            if (wrapper.isMember(uuid)) {
                //Is already a member..
                if (wrapper.isOwner(uuid)) {
                    player.sendMessage(ChatColor.RED + "You can't promote the owner of a guild.");
                    return;
                }

                if (wrapper.getRank(uuid).isThisRankOrHigher(GuildMember.GuildRanks.OFFICER)) {
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " has already been promoted.");
                    return;
                }

                wrapper.promotePlayer(player, player.getName(), wrapper.getMember(uuid));
            } else {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " is not in your guild.");
                return;
            }


        });
        Player p = Bukkit.getPlayer(p_name);
//        UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));

//        if (!GuildDatabaseAPI.get().getGuildOf(p_uuid).equals(guildName)) {
//            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " is not in your guild.");
//            return true;
//        }

//        if (GuildDatabaseAPI.get().isOfficer(p_uuid, guildName)) {
//            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " has already been promoted.");
//            return true;
//        }

//        if (GuildDatabaseAPI.get().isOwner(p_uuid, guildName)) {
//            player.sendMessage(ChatColor.RED + "You can't promote the owner of a guild.");
//            return true;
//        }

//        GuildDatabaseAPI.get().promotePlayer(guildName, p_uuid);
//        GameAPI.updateGuildData(guildName);

//        player.sendMessage(ChatColor.DARK_AQUA + "You have " + ChatColor.UNDERLINE + "promoted" + ChatColor.DARK_AQUA + " " + p_name + " to the rank of " + ChatColor.BOLD + "GUILD OFFICER" + ChatColor.GREEN + ".");
//        GuildMechanics.getInstance().sendAlert(guildName, ChatColor.GREEN + " " + p_name + " has been " + ChatColor.UNDERLINE + "promoted" + ChatColor.GREEN + " to the rank of " + ChatColor.BOLD + "GUILD OFFICER" + ChatColor.GREEN + ".");


        if (p != null) {
            p.sendMessage("");
//            p.sendMessage(ChatColor.DARK_AQUA + "You have been " + ChatColor.UNDERLINE + "promoted" + ChatColor.DARK_AQUA + " to the rank of " + ChatColor.BOLD + "GUILD OFFICER" + ChatColor.DARK_AQUA + " in " + displayName);
            p.sendMessage("");
        } else {
            //TODO: SEND PROMOTE PACKET
        }

        return false;
    }
}
