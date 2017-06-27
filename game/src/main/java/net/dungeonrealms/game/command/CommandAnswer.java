package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.PlayerToggles.Toggles;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */
public class CommandAnswer extends BaseCommand {

    public CommandAnswer(String command, String usage, String description) {
        super(command, usage, description);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player p = (Player) sender;
        if (!Rank.isPMOD(p)) return false;

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(p);

        if ((args.length == 0) || (args.length < 2)) {
            sender.sendMessage(ChatColor.RED + "/answer [name] [message]");
            return false;
        }
        String other = args[0];

        StringBuilder message = new StringBuilder(args[1]);
        for (int arg = 2; arg < args.length; arg++) message.append(" ").append(args[arg]);


        GameAPI.sendNetworkMessage("PrivateMessage", p.getName(), p.getUniqueId().toString(), other, ChatColor.GREEN + "<ANSWERED> " + ChatColor.GOLD + wrapper.getChatName() + ChatColor.GOLD + ": " + message);
        GameAPI.sendNetworkMessage("PrivateMessage", p.getName(), p.getUniqueId().toString(), other, ChatColor.RED + "Type " + ChatColor.YELLOW + (DungeonRealms.isMaster() || wrapper.getToggles().getState(Toggles.VANISH) ? "/ask" : "/msg " + sender.getName().toLowerCase()) + " [message] " + ChatColor.RED + "to reply back.");
        GameAPI.sendNetworkMessage("BroadcastSoundPlayer", other, Sound.BLOCK_NOTE_PLING.toString(), "1.0f", "1.0f");
        GameAPI.sendStaffMessage(ChatColor.GREEN + "<ANSWERED: " + other + "> " + ChatColor.GOLD + "(" + DungeonRealms.getInstance().shardid + ") " + wrapper.getChatName() + ChatColor.YELLOW + ": " + message);

        if (!wrapper.getToggles().getState(Toggles.ENABLE_PMS)) {
            wrapper.getToggles().toggle(Toggles.ENABLE_PMS);
            sender.sendMessage(ChatColor.GRAY + "Your DND has been disabled so players can reply to your answer.");
        }

        return true;
    }

    public String[] getAliases() {
        return new String[]{"assist", "a2"};
    }
}
