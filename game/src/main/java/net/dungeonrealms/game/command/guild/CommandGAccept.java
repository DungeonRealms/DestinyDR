package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
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


public class CommandGAccept extends BaseCommand {

    public CommandGAccept(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        // No guilds on the event shard.
        if (DungeonRealms.getInstance().isEventShard) {
            player.sendMessage(ChatColor.RED + "You cannot accept a guild invitation on this shard.");
            return false;
        }


        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null)return true;


        GuildWrapper guild = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
        if(guild == null) {
            player.sendMessage(ChatColor.RED + "You have no pending guild invites!");
            return false;
        }
        GuildMember member = guild.getMembers().get(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(player.getUniqueId()));

        if(member == null) {
            player.sendMessage(ChatColor.RED + "You have no pending guild invites!");
            return false;
        }

        if(member.isAccepted()) {
            player.sendMessage(ChatColor.RED + "You have no pending guild invites!");
//            player.sendMessage(ChatColor.RED + "You already accepted this guild invitation!");
            return false;
        }

        member.setAccepted(true);
        player.sendMessage(ChatColor.DARK_AQUA + "You have joined '" + ChatColor.BOLD + guild.getDisplayName() + "'" + ChatColor.DARK_AQUA + ".");
        player.sendMessage(ChatColor.GRAY + "To chat with your new guild, use " + ChatColor.BOLD + "/g" + ChatColor.GRAY + " OR " + ChatColor.BOLD + " /g <message>");
        guild.sendGuildMessage(player.getName() + ChatColor.GRAY.toString() + " has " +
                ChatColor.UNDERLINE + "joined" + ChatColor.GRAY + " your guild.");
        GameAPI.sendNetworkMessage("Guild", "accept", String.valueOf(guild.getGuildID()), String.valueOf(member.getAccountID()));
        return false;
    }

}