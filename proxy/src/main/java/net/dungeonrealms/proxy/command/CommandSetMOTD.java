package net.dungeonrealms.proxy.command;

import net.dungeonrealms.common.Constants;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;

public class CommandSetMOTD extends Command {
    public CommandSetMOTD() {
        super("setmotd");
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if (sender.getName().equals("CONSOLE") || Arrays.asList(Constants.DEVELOPERS).contains(sender.getName())) {

            if (strings.length <= 0) {
                sender.sendMessage(ChatColor.RED + "Please enter a valid MOTD!");
                return;
            }

            StringBuilder query = new StringBuilder("");

            for (int i = 0; i < strings.length; i++) {
                query.append(strings[i].replace("_", " "));
            }

            String MOTD = query.toString();

            Constants.MOTD = MOTD;
            sender.sendMessage(ChatColor.GRAY + "MOTD Set: " + MOTD);
        }
    }
}