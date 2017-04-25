package net.dungeonrealms.game.command.guild;

import com.avaje.ebean.SqlQuery;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.guild.GuildMember;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandGKick extends BaseCommand {

    public CommandGKick(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        GuildWrapper kickerWrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
        if (kickerWrapper == null) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gkick.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(usage);
            return true;
        }



        GuildMember kickerMember = kickerWrapper.getMembers().get(player);



        if (!kickerMember.getRank().isThisRankOrHigher(GuildMember.GuildRanks.OFFICER) && !Rank.isGM(player)) {
            player.sendMessage(ChatColor.RED + "You must be at least a guild " + ChatColor.BOLD + "OFFICER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gkick");
            return true;
        }

        String p_name = args[0];
        Player p = Bukkit.getPlayer(p_name);

        if (p_name.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot kick yourself to your own guild.");
            return true;
        }

        SQLDatabaseAPI.getInstance().getUUIDFromName(p_name, false, (uuid) -> {
            if(uuid == null){
                player.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realm");
                return;
            }

            Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);
            if(accountID == null) {
                Constants.log.info(player.getName() + " did /gkick but we could not retrieve the account id for " + p_name);
                return;
            }

            GuildMember otherMember = kickerWrapper.getMembers().get(accountID);

            if(otherMember == null) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " is not in your guild.");
                return;
            }

            if(otherMember.getRank().equals(GuildMember.GuildRanks.OWNER)) {
                player.sendMessage(ChatColor.RED + "You can't kick the owner of a guild.");
                return;
            }

            if(otherMember.getRank().equals(GuildMember.GuildRanks.OFFICER)) {
                player.sendMessage(ChatColor.RED + "You can't kick guild officers. You need to demote them first!");
                return;
            }

            kickerWrapper.getMembers().remove(accountID);

            player.sendMessage(ChatColor.GREEN + "Attemtping to kick " + p_name + " from your guild...");

            SQLDatabaseAPI.getInstance().executeQuery("DELETE FROM guild_members WHERE account_id = " + accountID.intValue(), true, (set) -> {
                if(set != null) {
                    player.sendMessage(ChatColor.RED + "An error occured while trying to kick " + p_name);
                    return;
                }

                player.sendMessage(ChatColor.GREEN + "You have successfully kicked " + p_name + " from your guild!");

                kickerWrapper.sendGuildMessage(ChatColor.YELLOW + p_name + ChatColor.RED + " has just been kicked from your guild!");

                if (p != null) {
                    p.sendMessage("");
                    p.sendMessage(ChatColor.RED + "You have been " + ChatColor.UNDERLINE + "kicked" + ChatColor.RED + " from " + kickerWrapper.getDisplayName());
                    p.sendMessage("");
                }
            });

        });

        return false;
    }
}
