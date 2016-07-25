package net.dungeonrealms.game.commands.guild;

import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.common.game.database.type.EnumOperators;
import net.dungeonrealms.game.guild.GuildMechanics;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandGAccept extends BasicCommand {

    public CommandGAccept(String command, String usage, String description) {
        super(command, usage, description);
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