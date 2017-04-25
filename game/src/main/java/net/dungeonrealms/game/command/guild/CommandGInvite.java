package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
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

public class CommandGInvite extends BaseCommand {

    public CommandGInvite(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);


        GuildWrapper guild = GuildDatabase.getAPI().getGuildWrapper(wrapper.getGuildID());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "/ginvite.");
            return true;
        }


        if (args.length == 0) {
            player.sendMessage(usage);
            return true;
        }


        if (!guild.isOwner(player.getUniqueId()) && !guild.getRank(player.getUniqueId()).equals(GuildMember.GuildRanks.OFFICER) && !Rank.isGM(player)) {
            player.sendMessage(ChatColor.RED + "You must be at least a guild " + ChatColor.BOLD + "OFFICER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/ginvite");
            return true;
        }
        String guildName = guild.getName();
        String displayName = guild.getDisplayName();
        String p_name = args[0];

        if (p_name.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot invite yourself to your own guild.");
            return true;
        }

        SQLDatabaseAPI.getInstance().getUUIDFromName(p_name, false, uuid -> {
            if (uuid == null) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " does not exist in our database.");
                return;
            }
            PlayerWrapper.getPlayerWrapper(uuid, false, true, foundPlayer -> {
                if (foundPlayer.getGuildID() != 0) {
                    //Already have a guild?
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " is already in a guild.");
                    return;
                }
                SQLDatabaseAPI.getInstance().executeUpdate(updated -> {
                    player.sendMessage(ChatColor.GRAY + "You have invited " + ChatColor.BOLD.toString() +
                            ChatColor.DARK_AQUA + p_name + ChatColor.GRAY + " to join your guild.");
                    //Send network message to invite them..
                    GameAPI.sendNetworkMessage("Guilds", "invite", guild.getGuildID() + "", guild.getDisplayName(), wrapper.getAccountID() + "", player.getName());
                }, QueryType.GUILD_INVITE.getQuery(wrapper.getAccountID(), guild.getGuildID(), "MEMBER", System.currentTimeMillis(), 0));
            });
        });
        return false;
    }
}
