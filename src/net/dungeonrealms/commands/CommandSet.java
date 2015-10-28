/**
 *
 */
package net.dungeonrealms.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.mechanics.LootManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.profession.Fishing;
import net.dungeonrealms.profession.Mining;
import net.dungeonrealms.spawning.SpawningMechanics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.dungeonrealms.spawning.SpawningMechanics.getSpawners;

/**
 * Created by Chase on Sep 22, 2015
 */
public class CommandSet extends BasicCommand {

	public CommandSet(String command, String usage, String description) {
		super(command, usage, description);
	}

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
			case "invlevel":
				int invlvl = Integer.parseInt(args[1]);
				DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "inventory.level", invlvl, true);
				break;
			case "spawner":
				if (args.length < 4) {
					player.sendMessage("/set spawner monster tier (* on monster for elite chance), (MOBS TO SPAWN x2)");
					player.sendMessage("/set spawner goblin 2 2(spawns 4)");
					return false;
				}
				int tier = 0;
				int spawnAmount = 0;
				try{
				 tier = Integer.parseInt(args[2]);
				 spawnAmount = Integer.parseInt(args[3]);
				}catch(Exception exc){
					return false;
				}
				String text = (player.getLocation().getX() + "," + player.getLocation().getY() + ","
				        + player.getLocation().getZ() + "=" + args[1] + ":" + tier + ";" + spawnAmount);
				player.sendMessage("Line " + (SpawningMechanics.SPAWNER_CONFIG.size() + 2) + " added "  + args[1] + " tier " + tier);
				SpawningMechanics.SPAWNER_CONFIG.add(text);
				DungeonRealms.getInstance().getConfig().set("spawners", SpawningMechanics.SPAWNER_CONFIG);
				DungeonRealms.getInstance().saveConfig();
				SpawningMechanics.loadSpawner(text);
				break;
			case "loot":
				if (args.length == 2) {
					int lootTier = Integer.parseInt(args[1]);
					String data = player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + ":" + lootTier;
					LootManager.spawnerConfig.add(data);
					DungeonRealms.getInstance().getConfig().set("loot", LootManager.spawnerConfig);
				}
				break;
			case "kill":
				player.getWorld().getLivingEntities().forEach(org.bukkit.entity.Entity::remove);
				getSpawners().forEach(net.dungeonrealms.spawning.MobSpawner::kill);
				break;
				
			case "pick":
				ItemStack stack = player.getItemInHand();
				if(stack != null){
					if(Mining.isDRPickaxe(stack)){
						int pickTier = Mining.getPickTier(stack);
						int xp = Mining.getMaxXP(pickTier) / 2;
						net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
						nms.getTag().setInt("XP", xp);
						player.setItemInHand(CraftItemStack.asBukkitCopy(nms));
					}
				}
				break;
			case "rod":
				ItemStack rodStack = player.getItemInHand();
				if(rodStack != null){
					if(Fishing.isDRFishingPole(rodStack)){
						int rodTier = Fishing.getRodTier(rodStack);
						int xp = Mining.getMaxXP(rodTier) / 2;
						net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(rodStack);
						nms.getTag().setInt("XP", xp);
						player.setItemInHand(CraftItemStack.asBukkitCopy(nms));
					}
				}
				break;
			}
		}
		return true;
	}
}
