package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.player.chat.Chat;
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
        if (!(sender instanceof Player)) {
            return false;
        }
        if (args.length < 2) {
            return false;
        }

        Player player = (Player) sender;

        if (PunishAPI.isMuted(player.getUniqueId())) {
            player.sendMessage(PunishAPI.getMutedMessage(player.getUniqueId()));
            return true;
        }

        String playerName = args[0];
        String message = String.join(" ", Arrays.asList(args));
        message = message.replace(playerName, "");
        if (DungeonRealms.getInstance().getDevelopers().contains(playerName)) {
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.PM_DEV);
        }
        String finalMessage = message;

        Chat.sendPrivateMessage(player, playerName, finalMessage.trim());
        return true;
    }

}
