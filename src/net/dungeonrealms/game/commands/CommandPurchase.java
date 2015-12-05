package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Created by Kieran on 05-Dec-15.
 */
public class CommandPurchase extends BasicCommand {

    public CommandPurchase(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            return false;
        }

        if (args.length == 1) {
            TextComponent bungeeMessage = new TextComponent(ChatColor.GREEN.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE");
            bungeeMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://shop.dungeonrealms.net"));
            bungeeMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to purchase!").create()));
            TextComponent test = new TextComponent(ChatColor.GRAY + "Thank you " + ChatColor.GREEN + args[0] + ChatColor.RESET + ChatColor.GRAY + " for donating @ store ");
            test.addExtra(bungeeMessage);
            Bukkit.spigot().broadcast(test);
        } else {
            return false;
        }

        return true;
    }
}
