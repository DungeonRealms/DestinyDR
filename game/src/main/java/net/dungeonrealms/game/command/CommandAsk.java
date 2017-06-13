package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.punishment.PunishAPI;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */
public class CommandAsk extends BaseCommand {

    public CommandAsk() {
        super("ask", "/<command> <question>", "Ask a staff member a question.", Arrays.asList("a"));
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player p = (Player) sender;

        if (PunishAPI.isMuted(p.getUniqueId())) {
            p.sendMessage(PunishAPI.getMutedMessage(p.getUniqueId()));
            return true;
        }
        
        if (args.length == 0) {
        	sender.sendMessage("/ask <message>");
        	return true;
        }
        
        p.sendMessage(ChatColor.GOLD + "Your question has been sent to an online staff member.");
        GameAPI.sendStaffMessage("&e<QUESTION> &6({SERVER}&6) " + PlayerWrapper.getWrapper(p).getChatName() + ":&e " + String.join(" ", args));

        return true;
    }
}
