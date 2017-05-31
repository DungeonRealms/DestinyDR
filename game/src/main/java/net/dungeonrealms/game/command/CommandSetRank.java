package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 09/06/2016.
 */
public class CommandSetRank extends BaseCommand {
    public CommandSetRank(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player && !Rank.isGM((Player) sender))
            return false;

        boolean isConsole = sender instanceof ConsoleCommandSender;
        PlayerWrapper executor = null;
        if (!isConsole) executor = PlayerWrapper.getWrapper((Player) sender);
        PlayerRank newRank = PlayerRank.getFromInternalName(args[1]);
        PlayerRank executorRank = isConsole ? PlayerRank.DEV : executor.getRank();

        if (newRank == null) {
            sender.sendMessage(ChatColor.RED + "Invalid usage: /setrank <name> <rank>");
            String ranks = "";
            for (int i = 0; i <= executorRank.ordinal(); i++) {
                PlayerRank r = PlayerRank.values()[i];
                ranks += r.getChatColor() + (r == PlayerRank.DEFAULT ? "DEFAULT" : r.getInternalName()) + ChatColor.GREEN + " | ";
            }

            sender.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Ranks: " + ranks);
            return true;
        }

        if (newRank.ordinal() > executorRank.ordinal()) {
            sender.sendMessage(ChatColor.RED + "You are not authorized to set a player to this rank.");
            return true;
        }

        SQLDatabaseAPI.getInstance().getUUIDFromName(args[0], false, uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms!");
                return;
            }

            PlayerWrapper.getPlayerWrapper(uuid, false, true, wrapper -> {

                PlayerRank currentRank = wrapper.getRank();

                if (currentRank.ordinal() > executorRank.ordinal()) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to change this user's rank.");
                    return;
                }

                if (newRank.isSUB())
                    wrapper.setRankExpiration(0);

                sender.sendMessage(ChatColor.GREEN + "Setting rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + (newRank == PlayerRank.DEFAULT ? "DEFAULT" : newRank.getInternalName()) + ChatColor.GREEN + ".");
                Player p = wrapper.getPlayer();
                wrapper.setRank(newRank);
                BungeeUtils.sendPlayerMessage(wrapper.getUsername(), "                 " + ChatColor.YELLOW + "Your rank is now: " + newRank.getPrefix());
                if (p != null && p.isOnline()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
                    Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> ScoreboardHandler.getInstance().updatePlayerName(p));
                }

                Rank.setRank(uuid, newRank.getInternalName(), done -> {
                    GameAPI.sendNetworkMessage("Rank", uuid.toString(), newRank.getInternalName());
                });
            });
        });

        return true;
    }

}
