package net.dungeonrealms.game.commands.guild;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.commands.BasicCommand;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandGDeny extends BasicCommand {

    public CommandGDeny(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        Document guildInvitation = (Document) DatabaseAPI.getInstance().getData(EnumData.GUILD_INVITATION, player.getUniqueId());

        if (guildInvitation == null) {
            player.sendMessage(ChatColor.RED + "No pending guild invitation.");
            return true;
        }

        String guildName = guildInvitation.getString("guild");
        String guildDisplayName = GuildDatabaseAPI.get().getDisplayNameOf(guildName);
        String referrer = guildInvitation.getString("referrer");


        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GUILD_INVITATION, null, true, true);

        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "Declined invitation to '" + ChatColor.BOLD + guildDisplayName + "'" + ChatColor.RED + "s guild.");

        if (Bukkit.getPlayer(referrer) != null) {
            Player owner = Bukkit.getPlayer(referrer);
            owner.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + player.getName() + ChatColor.RED.toString() + " has DECLINED your guild invitation.");
        } else {
            GameAPI.updatePlayerData(UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(referrer)));
        }

        return false;
    }

}