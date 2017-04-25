package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMember;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CommandGDemote extends BaseCommand {

    public CommandGDemote(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        GuildWrapper gWrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());


         if (gWrapper == null) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gdemote <player>.");
            return true;
        }


        if (args.length == 0) {
            player.sendMessage(usage);
            return true;
        }

        if (!gWrapper.isOwner(player.getUniqueId()) && !Rank.isGM(player)) {
            player.sendMessage(ChatColor.RED + "You must be the " + ChatColor.BOLD + "GUILD OWNER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gdemote <player>.");
            return true;
        }

        String p_name = args[0];

        if (p_name.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot demote yourself in your own guild.");
            return true;
        }

        SQLDatabaseAPI.getInstance().getUUIDFromName(p_name, false, (uuid) -> {
            if(uuid == null) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " has never logged into Dungeon Realms!");
                return;
            }


            int memberAccountId = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);
            Player p = Bukkit.getPlayer(p_name);

            GuildMember guildMember = gWrapper.getMembers().get(memberAccountId);

            if (guildMember == null) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " is not in your guild.");
                return;
            }

            if (!guildMember.getRank().equals(GuildMember.GuildRanks.OFFICER)) {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + p_name + ChatColor.RED + " is not yet a " + ChatColor.UNDERLINE + "guild officer");
                player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.RED + "/gpromote " + p_name + ChatColor.GRAY + " to make them a guild officer.");
                return;
            }

            if (guildMember.getRank().equals(GuildMember.GuildRanks.OWNER)) {
                player.sendMessage(ChatColor.RED + "You can't demote the owner of a guild.");
                return;
            }

            guildMember.setRank(GuildMember.GuildRanks.MEMBER);

            player.sendMessage(ChatColor.RED + "You have " + ChatColor.UNDERLINE + "demoted" + ChatColor.RED + " " + p_name + " to the rank of " + ChatColor.BOLD + "GUILD MEMBER.");
            GameAPI.sendNetworkMessage("Guild", "demote", String.valueOf(gWrapper.getGuildID()), String.valueOf(guildMember.getAccountID()));
            gWrapper.sendGuildMessage(ChatColor.RED + " " + p_name + " has been " + ChatColor.UNDERLINE + "demoted" + ChatColor.RED + " to the rank of " + ChatColor.BOLD + "GUILD MEMBER.");

            if (p != null) {
                p.sendMessage("");
                p.sendMessage(ChatColor.RED + "You have been " + ChatColor.UNDERLINE + "demoted" + ChatColor.RED + " to the rank of " + ChatColor.BOLD + "GUILD MEMBER" + ChatColor.RED + " in " + gWrapper.getDisplayName());
                p.sendMessage("");
            }

        });

        return false;
    }
}
