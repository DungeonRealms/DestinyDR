package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.guild.db.GuildDatabase;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 10/15/2015.
 */
public class CommandAccept extends BasicCommand {

    public CommandAccept(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;
        if (args.length > 0) {
            switch (args[0]) {
                case "guild":
                    assert args[1] != null : "arg[1] is null!";
                    if (!GuildDatabase.getAPI().isGuildNull(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You must leave your current guild to accept invitations to others!");
                        return true;
                    }
                    String guildAccepting = args[1].toUpperCase();
                    List<String> guildInvitations = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.GUILD_INVITES, player.getUniqueId());
                    if (guildInvitations.size() <= 0) {
                        player.sendMessage(ChatColor.RED + "You have no pending invitations!");
                        return true;
                    }
                    String invitation = guildInvitations.stream().filter(rs -> rs.startsWith(guildAccepting)).findFirst().get();

                    if (invitation == null || invitation.equals("")) {
                        player.sendMessage(ChatColor.RED + "You have no pending invitations to a guild with this name!");
                        return true;
                    }

                    String guildName = invitation.split(",")[0];

                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.GUILD_INVITES, guildName + "," + invitation.split(",")[1], true);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GUILD, guildName, true);


                    //NetworkAPI.getInstance().sendNetworkMessage("guild", "update", guildName);
                    ///NetworkAPI.getInstance().sendNetworkMessage("guild", "message", player.getName() + " has joined the Guild!");
                    player.sendMessage(ChatColor.GREEN + "Congratulations! You have successfully joined " + ChatColor.AQUA + guildName);


                    break;
            }
        }

        return true;
    }
}
