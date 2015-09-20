package net.dungeonrealms.commands;

import net.dungeonrealms.mechanics.ItemManager;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandAdd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;

        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(new ItemStack(Material.STICK));
        NBTTagCompound tag = nms.getTag() == null ? new NBTTagCompound() : nms.getTag();
        tag.set("type", new NBTTagString("weapon"));
        tag.set("damage", new NBTTagDouble(123));
        nms.setTag(tag);
        player.getInventory().addItem(CraftItemStack.asBukkitCopy(nms));
        //RANDOM TP BOOK
        player.getInventory().addItem(ItemManager.createRandomTeleportBook("Teleport Book"));

        return true;
    }
}
