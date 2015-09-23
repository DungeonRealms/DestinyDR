package net.dungeonrealms.commands;

import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mastery.FTPUtils;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.mechanics.XRandom;
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

import java.util.Random;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandAdd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;
        if (args.length > 0) {
            switch (args[0]) {
                case "realm":
                    new FTPUtils().downloadRealm(player.getUniqueId());
                    break;
                case "random":
                    player.sendMessage("Xorshift is: " + new XRandom().nextInt(Integer.parseInt(args[1])));
                    break;
                case "nbt":
                    net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(new ItemStack(Material.STICK));
                    NBTTagCompound tag = nms.getTag() == null ? new NBTTagCompound() : nms.getTag();
                    tag.set("type", new NBTTagString("weapon"));
                    tag.set("damage", new NBTTagDouble(123));
                    nms.setTag(tag);
                    player.getInventory().addItem(CraftItemStack.asBukkitCopy(nms));
                    //RANDOM TP BOOK
                    player.getInventory().addItem(ItemManager.createRandomTeleportBook("Teleport Book"));
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.WITCH_MAGIC, player.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 250);
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.PORTAL, player.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 4F, 400);
                    break;
                case "weapon":
                    player.getInventory().addItem(new ItemGenerator().next());
                    break;
                case "armor":
                    player.getInventory().addItem(new ArmorGenerator().next());
                    break;
                case "particle":
                    if (args[1] != null)
                        ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.getById(Integer.valueOf(args[1])), player.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 250);
                    break;
            }
        }

        return true;
    }
}
