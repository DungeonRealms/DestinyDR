package net.dungeonrealms.banks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Created by Chase on Sep 18, 2015
 */
public class BankMechanics implements GenericMechanic {

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

    
    
    
	public static boolean takeGemsFromInventory(int amount, Player p) {
		Inventory i = p.getInventory();
		int paid_off = 0;

		if (amount <= 0) {
			return true; // It's free.
		}

		HashMap<Integer, ? extends ItemStack> invItems = i.all(Material.EMERALD);
		for (Map.Entry<Integer, ? extends ItemStack> entry : invItems.entrySet()) {
			int index = entry.getKey();
			ItemStack item = entry.getValue();
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
			if(!nms.hasTag() || !nms.getTag().hasKey("type") || !nms.getTag().getString("type").equalsIgnoreCase("money"))
				continue;
			int stackAmount = item.getAmount();

			if ((paid_off + stackAmount) <= amount) {
				p.getInventory().setItem(index, new ItemStack(Material.AIR));
				paid_off += stackAmount;
			} else {
				int to_take = amount - paid_off;
				p.getInventory().setItem(index, createGems(stackAmount - to_take));
				paid_off += to_take;
			}
			if (paid_off >= amount) {
				p.updateInventory();
				return true;
			}
		}
		
		if(paid_off> 0){
			p.getInventory().addItem(createGems(paid_off));
			paid_off = 0;
		}
		
		//TODO GEM POUCH

//		HashMap<Integer, ? extends ItemStack> gem_pouches = i.all(Material.INK_SACK);
//		for (Map.Entry<Integer, ? extends ItemStack> entry : gem_pouches.entrySet()) {
//			ItemStack item = entry.getValue();
//
//			if (!MoneyMechanics.isGemPouch(item)) {
//				continue;
//			}
//
//			int worth = MoneyMechanics.getGemPouchWorth(item);
//
//			if ((paid_off + worth) <= amount) {
//				paid_off += worth;
//				MoneyMechanics.setPouchWorth(item, 0);
//			} else {
//				int to_take = amount - paid_off;
//				paid_off += to_take;
//				MoneyMechanics.setPouchWorth(item, worth - to_take);
//			}
//
//			if (paid_off >= amount) {
//				p.updateInventory();
//				break;
//			}
//
//		}

		HashMap<Integer, ? extends ItemStack> bank_notes = i.all(Material.PAPER);
		for (Map.Entry<Integer, ? extends ItemStack> entry : bank_notes.entrySet()) {
			ItemStack item = entry.getValue();
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
			if(!nms.hasTag() || !nms.getTag().hasKey("type") || !nms.getTag().getString("type").equalsIgnoreCase("money"))
				continue;
			int bank_note_val = getNoteValue(item);
			int index = entry.getKey();

			if ((paid_off + bank_note_val) <= amount) {
				p.getInventory().setItem(index, new ItemStack(Material.AIR));
				paid_off += bank_note_val;
			} else {
				int to_take = amount - paid_off;
				paid_off += to_take;
				updateMoney(p, index, (bank_note_val - to_take));
			}

			if (paid_off >= amount) {
				p.updateInventory();
				return true;
			}

		}
		
		if(paid_off > 0){
			p.getInventory().addItem(createBankNote(paid_off));
			paid_off = 0;
		}
		return false;
	}
    
    
	public static void updateMoney(Player p, int slot, int new_amount) { 
		p.getInventory().setItem(slot, createBankNote(new_amount));
	}
    
    /**
	 * @param amount
	 * @return ItemStack
	 * @since 1.0
	 */
	private static ItemStack createGems(int amount) {
		ItemStack stack = gem.clone();
		stack.setAmount(amount);
		return stack;
	}

//	/**
//     * Checks player inventory for gems, and takes them from their inventory.
//     * Return false if player doesn't have amount specified.
//     *
//     * @param amount
//     * @param p
//     * @return boolean
//     */
//    public boolean takeGemsFromInventory(int amount, Player p) {
//        int cost = 0;
//        for (ItemStack stack : p.getInventory().getContents()) {
//            if (stack != null && stack.getType() != Material.AIR) {
//                if (stack.getType() == Material.EMERALD) {
//                    net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
//                    if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
//                        p.getInventory().remove(stack);
//                        cost += stack.getAmount();
//                        if (cost >= amount) {
//                            int leftover = cost - amount;
//                            ItemStack gems = stack.clone();
//                            gems.setAmount(leftover);
//                            p.getInventory().addItem(gems);
//                            return true;
//                        } else {
//                            ItemStack gems = BankMechanics.gem.clone();
//                            gems.setAmount(cost);
//                            p.getInventory().addItem(gems);
//                        }
//                    }
//                } else if (stack.getType() == Material.PAPER) {
//                    net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
//                    if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
//                        int tempcost = getNoteValue(stack);
//                        if (stack.getAmount() > 1) {
//                            stack.setAmount(stack.getAmount() - 1);
//                            p.updateInventory();
//                        } else {
//                            p.getInventory().remove(stack);
//                        }
//                        if (tempcost >= amount) {
//                            int leftover = tempcost - amount;
//                            if (leftover > 0) {
//                                ItemStack giveBack = BankMechanics.createBankNote(leftover);
//                                p.getInventory().addItem(giveBack);
//                            }
//                            return true;
//                        } else {
//                            p.getInventory().addItem(BankMechanics.createBankNote(tempcost));
//                        }
//                        continue;
//                    }
//                }
//            } else {
//                continue;
//            }
//        }
//        return false;
//    }


    /**
     * Pre loads an itemstack version of our currency
     *
     * @return
     */
    private static void loadCurrency() {
        ItemStack item = new ItemStack(Material.EMERALD, 1);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.GREEN + "Gem");
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
     *
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
     * Return the value of itemstack note
     *
     * @param stack
     * @return integer
     */
    public static int getNoteValue(ItemStack stack) {
        return CraftItemStack.asNMSCopy(stack).getTag().getInt("worth");
    }

    /**
     * Add gems to player database
     *
     * @param uuid
     * @param num
     */
    public static void addGemsToPlayerBank(UUID uuid, int num) {
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, EnumData.GEMS, num, true);
    }

    /**
     * Add gems to player database
     *
     * @param uuid
     * @param num
     */
    public static void addGemsToPlayerInventory(Player p, int num) {
        ItemStack gems = gem.clone();
        gems.setAmount(num);
        p.getInventory().addItem(gems);
    }

    /**
     * @param uniqueId
     */
    public static Storage getStorage(UUID uniqueId) {
        return storage.get(uniqueId);
    }

}
