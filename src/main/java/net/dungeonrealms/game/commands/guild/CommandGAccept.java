package net.dungeonrealms.game.commands.guild;

import com.mongodb.BasicDBList;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
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


        BasicDBList invitations = (BasicDBList) DatabaseAPI.getInstance().getData(EnumData.GUILD_INVITE, player.getUniqueId());


        if (invitations.size() == 0) {
            player.sendMessage(ChatColor.RED + "No pending guilds invites.");
            return true;
        }


       // invitations.stream().forEach();


        return false;
    }

}