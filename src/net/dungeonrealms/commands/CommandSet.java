/**
 *
 */
package net.dungeonrealms.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.LootManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.spawning.SpawningMechanics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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
			case "invlevel":
				int invlvl = Integer.parseInt(args[1]);
				Utils.log.info(invlvl + " level");
				DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "inventory.level", invlvl,
				        true);
				break;
			case "spawner":
				if (args.length < 3) {
					player.sendMessage("/set spawner monster tier (* on monster for elite chance)");
					player.sendMessage("/set spawner goblin 2");
					return false;
				}
				int tier = Integer.parseInt(args[2]);
				String text = (player.getLocation().getX() + "," + player.getLocation().getY() + ","
				        + player.getLocation().getZ() + "=" + args[1] + ":" + tier);
				SpawningMechanics.SPANWER_CONFIG.add(text);
				DungeonRealms.getInstance().getConfig().set("spawners", SpawningMechanics.SPANWER_CONFIG);
				break;
			case "loot":
				if (args.length == 2) {
					int lootTier = Integer.parseInt(args[1]);
						String data = player.getLocation().getX() + "," + player.getLocation().getY() + ","
					        + player.getLocation().getZ() + ":" + lootTier;
						LootManager.spawnerConfig.add(data);
					Utils.log.info(LootManager.spawnerConfig.get(LootManager.spawnerConfig.size() - 1));
					Utils.log.info(LootManager.spawnerConfig.size()+ " size");
					DungeonRealms.getInstance().getConfig().set("loot", LootManager.spawnerConfig);
				}
				break;
			}
		}
		return true;
	}
}
