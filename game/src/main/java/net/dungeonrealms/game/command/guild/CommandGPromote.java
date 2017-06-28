package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.guild.GuildMember;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
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

        String p_name = args[0];

        if (p_name.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot promote yourself.");
            return true;
        }

        GuildMember.GuildRanks rank = wrapper.getRank(player.getUniqueId());
        if (rank == null || !rank.isThisRankOrHigher(GuildMember.GuildRanks.OWNER)) {
            player.sendMessage(ChatColor.RED + "You must be the owner to promote someone!");
            return true;
        }
//        if(wrapper.getRank(player.getUniqueId()).)
        SQLDatabaseAPI.getInstance().getUUIDFromName(p_name, false, uuid -> {
            if (uuid == null) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " does not exist in our database.");
                return;
            }

            if (wrapper.isMember(uuid)) {
                //Is already a member..
                if (wrapper.isOwner(uuid)) {
                    player.sendMessage(ChatColor.RED + "You can't promote the owner of the guild!");
                    return;
                }

                if (wrapper.getRank(uuid).isThisRankOrHigher(GuildMember.GuildRanks.OFFICER)) {
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " has already been promoted.");
                    return;
                }

                wrapper.promotePlayer(player, p_name, wrapper.getMember(uuid));
            } else {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " is not in your guild.");
                return;
            }
        });

        return false;
    }
}
