package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Created by Kieran on 05-Dec-15.
 */
public class CommandPurchase extends BaseCommand {

    public CommandPurchase(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            return false;
        }

        if (args.length == 1) {
            /*TextComponent bungeeMessage = new TextComponent(ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE");
            bungeeMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://www.dungeonrealms.net/store"));
            bungeeMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to view shop!").create()));
            TextComponent test = new TextComponent(ChatColor.GRAY + "Thank you " + ChatColor.GOLD.toString() + ChatColor.UNDERLINE + args[0] + ChatColor.RESET + ChatColor.GRAY + " for donating @ shop ");
            test.addExtra(bungeeMessage);
            Bukkit.spigot().broadcast(test);*/
        } else {
            return false;
        }

        return true;
    }
}
