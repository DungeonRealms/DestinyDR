package net.dungeonrealms.commands;

import com.mongodb.client.result.UpdateResult;
import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.core.Callback;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 10/31/2015.
 */
public class CommandGlobalChat extends BasicCommand {

    public CommandGlobalChat(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof ConsoleCommandSender) return false;

        Player player = (Player) sender;

        boolean gChat = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());

        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "toggles.globalChat", !gChat, true, new Callback<UpdateResult>(UpdateResult.class) {
            @Override
            public void callback(Throwable failCause, UpdateResult result) {
                if ((Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You have disabled global chat!");
                } else {
                    player.sendMessage(ChatColor.GREEN + "You have enabled global chat!");
                }
            }
        });

        return false;
    }
}
