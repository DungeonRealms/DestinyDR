package net.dungeonrealms.game.command.content;

import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.player.inventory.menus.staff.GUIItemEditor;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.md_5.bungee.api.ChatColor;

public class CommandItemEdit extends BaseCommand {

	public CommandItemEdit() {
		super("edit", "/<command> <edit/loadelite/saveelite> [args]", "Edit item commands.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || !Rank.isGM((Player) sender))
			return true;
		
		if (!DungeonRealms.isMaster() && !DungeonRealms.isBeta() && !Rank.isDev((Player) sender)) {
			sender.sendMessage(ChatColor.RED + "This command can only be run on the master shard.");
			return true;
		}
		
		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "Syntax: /edit <item/elite> [args...]");
			return true;
		}
		
		Player player = (Player) sender;
		EntityEquipment e = player.getEquipment();
		ItemStack held = e.getItemInMainHand();
		
		if (args[0].equalsIgnoreCase("item")) {
			if (!ItemGear.isCustomTool(held)) {
    			player.sendMessage(ChatColor.RED + "This is not an edittable item.");
    			return true;
    		}
    		
    		new GUIItemEditor(player, (ItemGear) PersistentItem.constructItem(held));
		} else if (args[0].equalsIgnoreCase("elite")) {
			if (args.length < 3) {
				player.sendMessage(ChatColor.RED + "Syntax: /edit elite <load/save> <name>");
				return true;
			}
			
			if (args[1].equalsIgnoreCase("load")) {
				Map<EquipmentSlot, ItemStack> eq = ItemGenerator.getEliteGear(args[2]);
				eq.values().forEach(player.getInventory()::addItem);
				player.sendMessage(ChatColor.GREEN + "Loaded.");
			} else if (args[1].equalsIgnoreCase("save")) {
				ItemGenerator.saveEliteGear(e, args[2]);
				player.sendMessage(ChatColor.GREEN + "Held equipment saved.");
			}
			
		} else {
			sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
		}
		
		return true;
	}

}
