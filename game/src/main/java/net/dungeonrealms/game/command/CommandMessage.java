package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.util.ChatUtil;
import net.dungeonrealms.database.punishment.PunishAPI;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.player.chat.Chat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Kieran Quigley (Proxying) on 01-Jul-16.
 */
public class CommandMessage extends BaseCommand {

    public CommandMessage(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        if (args.length < 2)
            return false;

        Player player = (Player) sender;

        if (PunishAPI.isMuted(player.getUniqueId())) {
            player.sendMessage(PunishAPI.getMutedMessage(player.getUniqueId()));
            return true;
        }

        String playerName = args[0];
        String message = String.join(" ", Arrays.asList(args));
        message = message.substring(playerName.length() + 1);
        
        if(ChatUtil.containsIllegal(message)){
            player.sendMessage(ChatColor.RED + "Message contains illegal characters.");
            return true;
        }
        
        
        // Achievements
        Achievements.getInstance().giveAchievement(player.getUniqueId(), EnumAchievements.SEND_A_PM);
        if (args[0].equalsIgnoreCase(player.getName()))
        	Achievements.getInstance().giveAchievement(player.getUniqueId(), EnumAchievements.MESSAGE_YOURSELF);
        if (DungeonRealms.getInstance().getDevelopers().contains(playerName))
            Achievements.getInstance().giveAchievement(player.getUniqueId(), EnumAchievements.PM_DEV);
        
        Chat.sendPrivateMessage(player, playerName, message.trim());
        
        return true;
    }

}
