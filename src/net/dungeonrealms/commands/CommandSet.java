/**
 *
 */
package net.dungeonrealms.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Chase on Sep 22, 2015
 */
public class CommandSet implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender)
            return false;
        Player player = (Player) s;
        if (args.length > 0) {
            switch (args[0]) {
                case "level":
                    int lvl = Integer.parseInt(args[1]);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.netLevel", lvl, true);
                    s.sendMessage("Level set to " + lvl);
                    break;
                case "gems":
                    int gems = Integer.parseInt(args[1]);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.gems", gems, true);
                    s.sendMessage("Gems set to " + gems);
                    break;
                case "inv":
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "inventory.player", "",
                            true);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "inventory.storage", "",
                            true);
                    break;
                case "spawner":
                    if (args.length < 3) {
                        player.sendMessage("/set spawner monster,monster tier (* on monster for elite chance)");
                        player.sendMessage("/set spawner goblin,troll*,bandit 2");
                        return false;
                    }
                    String[] monsters = args[1].split(",");
                    int tier = Integer.parseInt(args[2]);
                    File file = new File(DungeonRealms.getInstance().getDataFolder() + "\\global_spawns.dat");
                    try {
                        if (!file.exists())
                            file.createNewFile();
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                        writer.newLine();
                        writer.write(player.getLocation().getX() + "," + player.getLocation().getY() + ","
                                + player.getLocation().getZ() + "=" + args[1] + ":" + tier);
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        return true;
    }
}
