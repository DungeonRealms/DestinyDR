package net.dungeonrealms.commands;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.donate.DonationEffects;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mastery.RealmManager;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandAdd extends BasicCommand {

    public CommandAdd(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;
        if (args.length > 0) {
            switch (args[0]) {
                case "guild":
                    Guild.getInstance().createGuild(args[1], args[2], player.getUniqueId());
                    break;
                case "uploadrealm":
                    new RealmManager().uploadRealm(player.getUniqueId());
                    break;
                case "realm":
                    new RealmManager().downloadRealm(player.getUniqueId());
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
                    player.getInventory().addItem(ItemManager.createArmorScrap(1));
                    player.getInventory().addItem(ItemManager.createArmorScrap(2));
                    player.getInventory().addItem(ItemManager.createArmorScrap(3));
                    player.getInventory().addItem(ItemManager.createArmorScrap(4));
                    player.getInventory().addItem(ItemManager.createArmorScrap(5));
                    break;
                case "trail":
                    if (args[1] != null)
                        DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.put(player, ParticleAPI.ParticleEffect.getById(Integer.valueOf(args[1])));
                    break;
                case "gold":
                    DonationEffects.getInstance().PLAYER_GOLD_BLOCK_TRAILS.add(player);
                    break;
                    
                case "pick":
                	int tier = 1;
                	if(args.length == 2)
                		tier = Integer.parseInt(args[1]);
                	player.getInventory().addItem(ItemManager.createPickaxe(tier));
                	break;
                case "rod":
                	int rodTier = 1;
                	if(args.length == 2)
                		rodTier = Integer.parseInt(args[1]);
                	player.getInventory().addItem(ItemManager.createFishingPole(rodTier));
                	break;
            }
        }

        return true;
    }
}
