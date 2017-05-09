package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 12/2/2015.
 */
public class CommandCheck extends BaseCommand {

	public CommandCheck(String command, String usage, String description) {
		super(command, usage, description);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;

		Player player = (Player) sender;

		if (!Rank.isGM(player))
			return true;
		

		if (player.getInventory().getItemInMainHand() == null) {
			player.sendMessage(ChatColor.RED + "There is nothing in your hand.");
			return true;
		}

		ItemStack inHand = player.getInventory().getItemInMainHand();

		NBTTagCompound tag = CraftItemStack.asNMSCopy(inHand).getTag();

		if (args.length == 1) {
			if(args[0].equalsIgnoreCase("attribute")){
				PersistentItem item = PersistentItem.constructItem(inHand);
				
				if (item instanceof ItemGear) {
					ItemGear gear = (ItemGear)item;
					player.sendMessage(ChatColor.GREEN + "" + gear.getAttributes().size() + " Item Attributes:");
					for (AttributeType t : gear.getAttributes().keySet())
						player.sendMessage(ChatColor.YELLOW + t.getNBTName() + " - " + gear.getAttributes().getAttribute(t).toString());
				} else {
					player.sendMessage(ChatColor.RED + "This item cannot have attributes.");
				}
			} else {
				player.sendMessage("Listing All NBT...");
				// get all the nbt tags of the item
				tag.c().forEach(key -> player.sendMessage(key + ": " + tag.get(key).toString()));
			}
		} else if (args.length == 2) { // check player attributes
			Player attributePlayer = Bukkit.getPlayer(args[1]);
			if (args[0].equalsIgnoreCase("attributes") && attributePlayer != null) {
				PlayerWrapper wrapper = PlayerWrapper.getWrapper(attributePlayer);
				wrapper.getAttributes().entrySet().forEach(entry -> player.sendMessage(entry.getKey() + ": " + entry.toString()));
				return true;
			}
		} else {
			player.sendMessage(ChatColor.RED + "EpochIdentifier: " + tag.getString("u"));
			return true;
		}
		return false;
	}
}