package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
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

import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandGDeny extends BaseCommand {

    public CommandGDeny(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        PlayerWrapper playerWrapper = PlayerWrapper.getPlayerWrapper(player);
        GuildWrapper wrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
        if(wrapper == null) {
            player.sendMessage(ChatColor.RED + "No pending guild invitation.");
            return true;
        }

        if(playerWrapper == null) {
            player.sendMessage(ChatColor.RED + "An error occurred.");
            return true;
        }

        GuildMember member = wrapper.getMembers().get(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(player.getUniqueId()));

        if(member == null) {
            player.sendMessage(ChatColor.RED + "You do not have a pending guild invitation!");
            Constants.log.info("A person did /g deny. We found their guildwrapper but they were not in the guild!");
            return true;
        }

        if(member.isAccepted()) {
            player.sendMessage(ChatColor.RED + "You are already in the guild! Do /g leave");
            return true;
        }

        playerWrapper.setGuildID(0);
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "Declined invitation to '" + ChatColor.BOLD + wrapper.getName() + "'" + ChatColor.RED + "s guild.");
        wrapper.removePlayer(player.getUniqueId());
        SQLDatabaseAPI.getInstance().executeUpdate((rows) -> {
            wrapper.sendGuildMessage(ChatColor.RED.toString() + ChatColor.BOLD + player.getName() + ChatColor.RED.toString() + " has DECLINED your guild invitation.", false,GuildMember.GuildRanks.OFFICER);
            GameAPI.sendNetworkMessage("Guilds", "deny", DungeonRealms.getShard().getPseudoName(),String.valueOf(wrapper.getGuildID()), String.valueOf(member.getAccountID()));
        }, String.format("DELETE FROM guild_members WHERE account_id = '%s';", member.getAccountID()), true);

        return true;
    }

}