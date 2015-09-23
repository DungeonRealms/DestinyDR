package net.dungeonrealms.commands;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandAnalyze implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;

        if (args.length > 0) {
            switch (args[0]) {
                case "inhand":
                    if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR))
                        return false;
                    net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(player.getItemInHand());
                    NBTTagCompound tag = nms.getTag();
                    if (tag == null) {
                        player.sendMessage("None of our NBT..");
                        return true;
                    }
                    player.sendMessage(new String[]{
                            "Type: " + tag.getString("type"),
                            "Damage: " + tag.getDouble("damage"),
                            "Armor: " + tag.getInt("armor")
                    });
                    break;
                case "lookat":
                    break;
                default:
            }
        }

        return true;
    }
}
