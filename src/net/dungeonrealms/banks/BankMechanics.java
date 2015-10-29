package net.dungeonrealms.banks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Created by Chase on Sep 18, 2015
 */
public class BankMechanics implements GenericMechanic{

    public static ItemStack gem;
    public static ItemStack banknote;
    public static HashMap<UUID, Storage> storage = new HashMap<>();

    private static BankMechanics instance = null;

    public static BankMechanics getInstance() {
        if (instance == null) {
            instance = new BankMechanics();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    public void startInitialization() {
        loadCurrency();
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * 
     * Checks player inventory for gems, and takes them from their inventory.
     * Return false if player doesn't have amount specified.
     * 
     * @param amount
     * @param p
     * @return boolean
     */
    public boolean takeGemsFromInventory(int amount, Player p) {
    	int cost = 0;
    	for (ItemStack stack : p.getInventory().getContents()) {
    		if (stack != null && stack.getType() != Material.AIR){
    		if (stack.getType() == Material.EMERALD) {
    			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
    			if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")){
    				p.getInventory().remove(stack);
    				cost += stack.getAmount();
    				if (cost >= amount) {
    					int leftover = cost - amount;
    					ItemStack gems = stack.clone();
    					gems.setAmount(leftover);
    					p.getInventory().addItem(gems);
    					return true;
    				} else {
    					ItemStack gems = BankMechanics.gem.clone();
    					gems.setAmount(cost);
    					p.getInventory().addItem(gems);
    				}
    			}
    		}else if(stack.getType() == Material.PAPER){
    			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
    			if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")){
    				 int tempcost = getNoteValue(stack);
    				 if(stack.getAmount() > 1){
    					 stack.setAmount(stack.getAmount() - 1);
    					 p.updateInventory();
    				 }else{
    					 p.getInventory().remove(stack);
    				 }
    				 if (tempcost >= amount) {
     					int leftover = tempcost - amount;
     					if(leftover > 0){
     						ItemStack giveBack = BankMechanics.createBankNote(leftover);
     						p.getInventory().addItem(giveBack);
     					}
     					return true;
     				} else {
     					p.getInventory().addItem(BankMechanics.createBankNote(tempcost));
     				}
  					continue;
    			}
    		}
    		}else{
    			continue;
    		}
    	}
		return false;
    }
    
    
    /**
     * Pre loads an itemstack version of our currency
     * @return
     */
    private static void loadCurrency() {
        ItemStack item = new ItemStack(Material.EMERALD, 1);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add("The currency of Andalucia");
        meta.setLore(lore);
        meta.setDisplayName("Gem");
        item.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nms.getTag() == null ? new NBTTagCompound() : nms.getTag();
        tag.setString("type", "money");
        nms.setTag(tag);
        gem = CraftItemStack.asBukkitCopy(nms);

        ItemStack item2 = new ItemStack(Material.PAPER, 1);
        ItemMeta meta2 = item2.getItemMeta();
        List<String> lore2 = new ArrayList<>();
        lore2.add(ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Value: " + ChatColor.WHITE.toString());
        meta2.setLore(lore2);
        meta2.setDisplayName(ChatColor.GREEN.toString() + "Bank Note");
        item2.setItemMeta(meta2);
        net.minecraft.server.v1_8_R3.ItemStack nms2 = CraftItemStack.asNMSCopy(item2);
        NBTTagCompound tag2 = nms2.getTag() == null ? new NBTTagCompound() : nms2.getTag();
        tag2.setString("type", "money");
        tag2.setInt("worth", 0);
        nms2.setTag(tag2);
        banknote = CraftItemStack.asBukkitCopy(nms2);
    }

    /**
     * Creates a new Bank Note for the set amount
     * @param amount
     * @return
     */
    public static ItemStack createBankNote(int amount) {
        
        ItemStack stack = BankMechanics.banknote.clone();
        ItemMeta meta = stack.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.BOLD.toString() + "Value: " + ChatColor.WHITE.toString()
                + amount);
        meta.setLore(lore);
        stack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nms1 = CraftItemStack.asNMSCopy(stack);
        nms1.getTag().setInt("worth", amount);
        return CraftItemStack.asBukkitCopy(nms1);
    }
    /**
     * 
     * Return the value of itemstack note
     * 
     * @param stack
     * @return integer
      */
    public static int getNoteValue(ItemStack stack){
    	return CraftItemStack.asNMSCopy(stack).getTag().getInt("worth");
    }

    /**
     * Add gems to player database
     * 
     * @param uuid
     * @param num
     */
    public static void addGemsToPlayer(UUID uuid, int num) {
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, "info.gems", num, true);
    }

    /**
     * @param uniqueId
     */
    public static Storage getStorage(UUID uniqueId) {
        return storage.get(uniqueId);
    }

}
