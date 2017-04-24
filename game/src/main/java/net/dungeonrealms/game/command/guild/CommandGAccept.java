package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.guild.GuildMechanics;
import org.bson.Document;
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

        Document guildInvitation = (Document) DatabaseAPI.getInstance().getData(EnumData.GUILD_INVITATION, player.getUniqueId());

        if (guildInvitation == null) {
            player.sendMessage(ChatColor.RED + "No pending guild invitation.");
            return true;
        }


        String guildName = guildInvitation.getString("guild");
        String referrer = guildInvitation.getString("referrer");
        long time = guildInvitation.getLong("time");

        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GUILD_INVITATION, null, true);

        if ((System.currentTimeMillis() - time) > 300000L) {
            player.sendMessage(ChatColor.RED + "Your invitation has expired.");
            return true;
        }

        GuildMechanics.getInstance().joinGuild(player, referrer, guildName);
        return false;
    }

}