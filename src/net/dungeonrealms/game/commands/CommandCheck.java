package net.dungeonrealms.game.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.API;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.Item.AttributeType;
import net.dungeonrealms.game.world.items.armor.Armor.ArmorAttributeType;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Created by Nick on 12/2/2015.
 */
public class CommandCheck extends BasicCommand {

    public CommandCheck(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if(!player.isOp()) {
            return true;
        }

        if (player.getItemInHand() == null) {
            player.sendMessage(ChatColor.RED + "There is nothing in your hand.");
            return true;
        }

        ItemStack inHand = player.getItemInHand();

        NBTTagCompound tag = CraftItemStack.asNMSCopy(inHand).getTag();
        
        
        if(args.length == 1){
        	if(args[0].equalsIgnoreCase("nbt")){
        		if(API.isWeapon(inHand)){
        			for(AttributeType type : Item.AttributeType.values()){
        				if(tag.hasKey(type.getNBTName())){
        					sender.sendMessage(type.getName() + ": " + tag.getInt(type.getNBTName()));
        				}else{
        					sender.sendMessage("Doesn't contain " + type.getName());
        				}
        			}
        		}else if(API.isArmor(inHand)){
        			for(ArmorAttributeType type : ArmorAttributeType.values()){
        				if(tag.hasKey(type.getNBTName())){
        					sender.sendMessage(type.getName() + ": " + tag.getInt(type.getNBTName()));
        				}else{
        					sender.sendMessage("Doesn't contain " + type.getName());
        				}
        			}
        		}
        	}
        }else{
        	player.sendMessage(ChatColor.RED + "EpochIdentifier: " + tag.getString("u"));
        }
        return false;
    }
}
