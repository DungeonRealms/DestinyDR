package net.dungeonrealms.commands;

import net.dungeonrealms.anticheat.AntiCheat;
import net.dungeonrealms.donate.DonationEffects;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mastery.FTPUtils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
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
                case "guild":
                    Guild.getInstance().createGuild(args[1], args[2], player.getUniqueId());
                    break;
                case "proxy":
                    player.sendMessage(String.valueOf(AntiCheat.getInstance().isProxying(player.getUniqueId(), player.getAddress().getAddress())));
                    break;
                case "uploadrealm":
                    new FTPUtils().uploadRealm(player.getUniqueId());
                    break;
                case "realm":
                    new FTPUtils().downloadRealm(player.getUniqueId());
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
                case "bank":
                    net.minecraft.server.v1_8_R3.ItemStack nmsBank = CraftItemStack.asNMSCopy(new ItemStack(Material.ENDER_CHEST));
                    NBTTagCompound Banktag = nmsBank.getTag() == null ? new NBTTagCompound() : nmsBank.getTag();
                    Banktag.set("type", new NBTTagString("bank"));
                    nmsBank.setTag(Banktag);
                    player.getInventory().addItem(CraftItemStack.asBukkitCopy(nmsBank));
                    break;
                case "trail":
                    if (args[1] != null)
                        DonationEffects.PLAYER_PARTICLE_EFFECTS.put(player, ParticleAPI.ParticleEffect.getById(Integer.valueOf(args[1])));
                    break;
                case "gold":
                    DonationEffects.PLAYER_GOLD_BLOCK_TRAILS.add(player);
                    break;

            }
        }

        return true;
    }
}
