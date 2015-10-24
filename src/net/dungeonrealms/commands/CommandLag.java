package net.dungeonrealms.commands;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.mastery.Utils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Created by Kieran on 9/17/2015.
 */
public class CommandLag extends BasicCommand {

    public CommandLag(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (commandSender instanceof ConsoleCommandSender) {
            return false;
        }
        if (!(commandSender.isOp())) {
            commandSender.sendMessage("You're not OP.");
            return false;
        }

        long timeElapsed = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
        long totalSeconds = timeElapsed / 1000L;
        long totalMinutes = totalSeconds / 60L;
        long totalHours = totalMinutes / 60L;
        long totalDays = totalHours / 24L;

        long fmtSeconds = totalSeconds - totalMinutes * 60L;
        long fmtMinutes = totalMinutes - totalHours * 60L;
        long fmtHours = totalHours - totalDays * 24L;

        commandSender.sendMessage(ChatColor.GREEN + "Current Uptime: " + ChatColor.WHITE + fmtHours + "h " + fmtMinutes + "m " + fmtSeconds + "s");
        commandSender.sendMessage(ChatColor.GREEN + "RAM Usage: " + ChatColor.WHITE + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + "MB"
                + ChatColor.GREEN + "/" + ChatColor.WHITE + (Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB"));
        commandSender.sendMessage("");

        final Server server = Bukkit.getServer();
        List<World> worldList = server.getWorlds();

        for (World w : worldList) {
            String worldType = "World";
            switch (w.getEnvironment()) {
                case NETHER:
                    worldType = "Nether";
                    break;
                case THE_END:
                    worldType = "The End";
                    break;
            }
            int tileEntities = 0;
            try {
                for (Chunk chunk : w.getLoadedChunks()) {
                    tileEntities += chunk.getTileEntities().length;
                }
            } catch (ClassCastException ex) {
                Utils.log.info("Corrupted Chunk data on world " + w);
            }
            commandSender.sendMessage(ChatColor.GREEN + "World Type: " + ChatColor.WHITE + worldType + ChatColor.GREEN + " World Name: " + ChatColor.WHITE + w.getName());
            commandSender.sendMessage(ChatColor.YELLOW + "Loaded Chunks: " + ChatColor.WHITE + +w.getLoadedChunks().length + ChatColor.BLUE + " Current Entities: "
                    + ChatColor.WHITE + w.getEntities().size() + ChatColor.LIGHT_PURPLE + " Current TEs: " + ChatColor.WHITE + tileEntities);
            commandSender.sendMessage("");
        }
        return true;
    }
}
