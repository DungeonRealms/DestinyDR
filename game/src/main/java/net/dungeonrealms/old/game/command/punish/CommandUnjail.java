package net.dungeonrealms.old.game.command.punish;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.old.game.mastery.GamePlayer;
import net.dungeonrealms.old.game.world.teleportation.Teleportation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/25/2016
 */
public class CommandUnjail extends BaseCommand {


    public CommandUnjail(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isGM(player)) return false;
        }


        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "/unjail <player>");
            return true;
        }


        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " ain't online");
            return true;
        }


        GamePlayer gamePlayer = GameAPI.getGamePlayer(player);

        if (gamePlayer == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " ain't playin");
            return true;
        }


        if (!gamePlayer.isJailed()) {
            sender.sendMessage(ChatColor.RED + args[0] + " is free");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "You done unjailed " + args[0] + " m9");
        player.sendMessage(ChatColor.RED + "You have been unjailed");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 0.5F);
        player.teleport(Teleportation.Cyrennica);

        gamePlayer.setJailed(false);
        return false;
    }
}
