package net.dungeonrealms.old.game.command.guild;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.old.game.guild.GuildDatabaseAPI;
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

public class CommandGDeny extends BaseCommand {

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

        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GUILD_INVITATION, null, true, doAfter -> {
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "Declined invitation to '" + ChatColor.BOLD + guildDisplayName + "'" + ChatColor.RED + "s guild.");

            Player owner = Bukkit.getPlayer(referrer);
            BungeeUtils.sendPlayerMessage(owner.getName(), ChatColor.RED.toString() + ChatColor.BOLD + player.getName() + ChatColor.RED.toString() + " has DECLINED your guild invitation.");
            GameAPI.updatePlayerData(UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(referrer)));
        });

        return false;
    }

}