package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.game.mechanic.TutorialIsland;
import net.dungeonrealms.game.player.chat.GameChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 9/3/2016
 */

public class CommandWelcome extends BaseCommand {

    public CommandWelcome(String command, String usage, String description) {
        super(command, usage, description);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "/welcome <newplayer>");
            return false;
        }

        Player player = (Player) sender;

        if (PunishAPI.getInstance().isMuted(player.getUniqueId())) {
            player.sendMessage(PunishAPI.getInstance().getMutedMessage(player.getUniqueId()));
            return true;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            player.sendMessage(ChatColor.RED + playerName + " is not online.");
            return true;
        }

        if (!TutorialIsland.onTutorialIsland(target.getLocation())) {
            player.sendMessage(ChatColor.RED + playerName + " is not new.");
            return true;
        }

        List<String> welcomes = TutorialIsland.getInstance().getWelcomes(player.getUniqueId());

        if (welcomes.contains(playerName.toLowerCase())) {
            player.sendMessage(ChatColor.RED + "You have already welcomed " + playerName);
            return true;
        }

        welcomes.add(playerName.toLowerCase());
        player.sendMessage(ChatColor.GOLD + "You have welcomed " + target.getName() + " for " + ChatColor.AQUA + "2 E-Cash" + ChatColor.GOLD + "!");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1.0F, 1.0F);

        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, 2, true);
        Bukkit.getOnlinePlayers().forEach(newPlayer -> newPlayer.sendMessage(GameChat.getPreMessage(player, true) + "Welcome " + target.getName()));
        return false;
    }

}
