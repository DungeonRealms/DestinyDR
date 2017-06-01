package net.dungeonrealms.game.command;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPack extends BaseCommand {
    public CommandPack() {
        super("Recieve the resource pack.", "/<command>", "Receive the resource pack", null, Lists.newArrayList("resourcepack", "texturepack"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        player.sendMessage(ChatColor.GREEN + "Attempting to send you the Resource Pack...");
        player.setResourcePack(Constants.RESOURCE_PACK);
        player.sendMessage(ChatColor.GRAY + "If you are still unable to receive the Resource Pack please try relogging and making sure Server Resource Packs are enabled.");
        return false;
    }
}
