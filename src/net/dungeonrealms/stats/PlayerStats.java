package net.dungeonrealms.stats;

import java.text.DecimalFormat;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;

/**
 * Created by Chase on Nov 2, 2015
 */
public class PlayerStats {
	public int freePoints;
	 public int strPoints;
	 public int tempstrPoints;
	 public int dexPoints;
	 public int tempdexPoints;
	 public int vitPoints;
	 public int tempvitPoints;
	 public int intPoints;
	 public int tempintPoints;
	 public int tempFreePoints;
	 public int level;
	 UUID playerUUID;
	 final static int POINTS_PER_LEVEL = 6;
	 public boolean reset = true;

	public PlayerStats(UUID playerUUID) {
		this.playerUUID = playerUUID;
		this.freePoints = 0;
		this.tempFreePoints = 0;
		this.strPoints = 0;
		this.dexPoints = 0;
		this.vitPoints = 0;
		this.intPoints = 0;
		this.level = 1;
		this.tempstrPoints = 0;
		this.tempdexPoints = 0;
		this.tempvitPoints = 0;
		this.tempintPoints = 0;
		loadPlayerStats();
	}

	/**
	 * gets stat points from the database for UUID
	 * 
	 * @since 1.0;
	 */
	public void loadPlayerStats() {
		this.freePoints = (int) DatabaseAPI.getInstance().getData(EnumData.BUFFER_POINTS, playerUUID);
		this.intPoints = (int) DatabaseAPI.getInstance().getData(EnumData.INTELLECT, playerUUID);
		this.dexPoints = (int) DatabaseAPI.getInstance().getData(EnumData.DEXTERITY, playerUUID);
		this.strPoints = (int) DatabaseAPI.getInstance().getData(EnumData.STRENGTH, playerUUID);
		this.vitPoints = (int) DatabaseAPI.getInstance().getData(EnumData.VITALITY, playerUUID);
		this.level = (int) DatabaseAPI.getInstance().getData(EnumData.LEVEL, playerUUID);
		Utils.log.info(freePoints + " free Points");
		Utils.log.info(intPoints + " INT Points");
		Utils.log.info(dexPoints + " DEX Points");
		Utils.log.info(strPoints + " STR Points");
		Utils.log.info(vitPoints + " VIT Points");
		Utils.log.info(level + " Level");
	}

	public void allocatePoint(String type, Player p, Inventory inv) {
		if (tempFreePoints > 0) {
			if (type.equalsIgnoreCase("dex")) {
				tempdexPoints += 1;
			} else if (type.equalsIgnoreCase("int")) {
				tempintPoints += 1;
			} else if (type.equalsIgnoreCase("vit")) {
				tempvitPoints += 1;
			} else if (type.equalsIgnoreCase("str")) {
				tempstrPoints += 1;
			}
			tempFreePoints =(tempFreePoints - 1);
		}
		updateItems(inv, p);
		// p.openInventory(getInventory(p));

	}

	public void updateItems(Inventory openInventory, Player p) {
		PlayerStats stats = StatsManager.getPlayerStats(p);
		ItemStack confirmItem = loadConfirmItem();
		ItemStack dexItem = loadDexItem();
		ItemStack dexStatsItem = loadDexStatsItem();
		ItemStack intItem = loadIntItem();
		ItemStack intStatsItem = loadIntStatsItem();
		ItemStack statsInfoItem = loadStatsInfoItem();
		ItemStack strItem = loadStrItem();
		ItemStack strStatsItem = loadStrStatsItem();
		ItemStack vitItem = loadVitItem();
		ItemStack vitStatsItem = loadVitStatsItem();
		openInventory.setItem(2, strItem);
		openInventory.setItem(3, dexItem);
		openInventory.setItem(4, intItem);
		openInventory.setItem(5, vitItem);
		openInventory.setItem(6, confirmItem);
		openInventory.setItem(11, strStatsItem);
		openInventory.setItem(12, dexStatsItem);
		openInventory.setItem(13, intStatsItem);
		openInventory.setItem(14, vitStatsItem);
		openInventory.setItem(15, statsInfoItem);
		
	}

	public void removePoint(String type, Player p, Inventory inv) {
		if (type.equalsIgnoreCase("dex")) {
			if (tempdexPoints > 0) {
				tempdexPoints = (tempdexPoints - 1);
				tempFreePoints =(tempFreePoints + 1);
			}
		} else if (type.equalsIgnoreCase("int")) {
			if (tempintPoints > 0) {
				tempintPoints = (tempintPoints - 1);
				tempFreePoints =(tempFreePoints + 1);
			}
		} else if (type.equalsIgnoreCase("vit")) {
			if (tempvitPoints > 0) {
				tempvitPoints = (tempvitPoints - 1);
				tempFreePoints =(tempFreePoints + 1);
			}
		} else if (type.equalsIgnoreCase("str")) {
			if (tempstrPoints > 0) {
				tempstrPoints= (tempstrPoints - 1);
				tempFreePoints =(tempFreePoints + 1);
			}
		}
		updateItems(inv, p);
	}

	ItemStack loadVitStatsItem() {

		int vit =  vitPoints;
		int aPoints = tempvitPoints; // allocated points
		boolean spent = (aPoints > 0) ? true : false;
		
		return ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.RED + "Vitality Bonuses: " + vit + (spent ? ChatColor.GREEN + " [+" + aPoints + "]" : ""), new String[]{	ChatColor.GOLD + "HP: " + ChatColor.AQUA + df.format(vit * 0.034) + "%" + (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.034) + "%]" : ""),
				ChatColor.GOLD + "HP REGEN: " + ChatColor.AQUA + df.format(vit * 0.03) + " HP/s"
						+ (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.03) + " HP/s]" : ""),
//				ChatColor.GOLD + "ELE RESIST: " + ChatColor.AQUA + df.format(vit * 0.04) + "%" + (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.04) + "%]" : ""),
				ChatColor.GOLD + "SWORD DMG: " + ChatColor.AQUA + df.format(vit * 0.01) + "%" + (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.01) + "%]" : "")});
	}
	
	public double getVitHP(){
		return (vitPoints * 0.034);
	}
	
	public double getHPRegen(){
		return (vitPoints * 0.03);
	}
	
//	public double getEleResist(){
//		return vitPoints * 0.04;
//	}
	
	public double getSwordDMG(){
		return (vitPoints * 0.01);
	}
	
	  ItemStack loadVitItem() {
		int points = vitPoints;
		boolean spent = tempvitPoints > 0;
		
		return ItemManager.createItem(Material.EMPTY_MAP, ChatColor.DARK_PURPLE + "Vitality", new String[]{ChatColor.GRAY + "Adds health, hp regen, ", ChatColor.GRAY + "elemental resistance, and ", ChatColor.GRAY + "sword damage.",
				ChatColor.AQUA + "Allocated Points: " + vitPoints + (spent ? ChatColor.GREEN + " [+" + tempvitPoints + "]" : ""),
				ChatColor.RED + "Free Points: " + tempFreePoints});
	}

	  ItemStack loadStrStatsItem() {
		int str = strPoints;
		int aPoints = tempstrPoints; // allocated points
		boolean spent = (aPoints > 0) ? true : false;
		
		return ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.RED + "Strength Bonuses: " + str + (spent ? ChatColor.GREEN + " [+" + aPoints + "]" : ""), new String[]{ChatColor.GOLD + "ARMOR: " + ChatColor.AQUA + df.format(str * 0.03) + "%" + (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.03) + "%]" : ""),
				ChatColor.GOLD + "BLOCK: " + ChatColor.AQUA + df.format(str * 0.017) + "%" + (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.017) + "%]" : ""),
				ChatColor.GOLD + "AXE DMG: " + ChatColor.AQUA + df.format(str * 0.015) + "%" + (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.015) + "%]" : ""),
				ChatColor.GOLD + "POLEARM DMG: " + ChatColor.AQUA + df.format(str * 0.023) + "%"
						+ (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.023) + "%]" : "")});
	}
	  
	  public double getBlock(){
		  return strPoints * 0.017;
	  }
	  
	  public double getAxeDMG(){
		  return strPoints * .015;
	  }
	  
	  public double getPolearmDMG(){
		  return strPoints * 0.023;
	  }

	  ItemStack loadStrItem() {
		int points = strPoints;
		boolean spent = tempstrPoints > 0;
		return ItemManager.createItem(Material.EMPTY_MAP, ChatColor.DARK_PURPLE + "Strength", new String[]{ChatColor.GRAY + "Adds armor, block chance, axe ", ChatColor.GRAY + "damage, and polearm damage.",
				ChatColor.AQUA + "Allocated Points: " + strPoints + (spent ? ChatColor.GREEN + " [+" + tempstrPoints + "]" : ""),
				ChatColor.RED + "Free Points: " + tempFreePoints});
	}

	  ItemStack loadStatsInfoItem() {
		
		return ItemManager.createItem(Material.ENCHANTED_BOOK, ChatColor.YELLOW + "Stat Point Info", new String[]{ChatColor.LIGHT_PURPLE + "Points to Allocate: " + tempFreePoints,
				ChatColor.AQUA + "LCLICK" + ChatColor.GRAY + " to allocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "1" + ChatColor.GRAY + " point",
				ChatColor.AQUA + "RCLICK" + ChatColor.GRAY + " to unallocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "1" + ChatColor.GRAY + " point"});
	}

	  ItemStack loadIntStatsItem() {
		int in = intPoints;
		int aPoints = tempintPoints; // allocated points
		boolean spent = (aPoints > 0) ? true : false;
		
		return  ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.RED + "Intellect Bonuses: " + in + (spent ? ChatColor.GREEN + " [+" + aPoints + "]" : ""), new String[]{
				ChatColor.GOLD + "ENERGY REGEN: " + ChatColor.AQUA + df.format(in * 0.015) + "%"
						+ (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.015) + "%]" : ""),
				ChatColor.GOLD + "ELE DMG: " + ChatColor.AQUA + df.format(in * 0.05) + (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.05) + "%]" : ""),
				ChatColor.GOLD + "CRIT CHANCE: " + ChatColor.AQUA + df.format(in * 0.025) + "%"
						+ (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.025) + "%]" : ""),
				ChatColor.GOLD + "STAFF DMG: " + ChatColor.AQUA + df.format(in * 0.02) + "%" + (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.02) + "%]" : "")});
	}

	  public double getEnergyRegen(){
		  return intPoints * 0.015;
	  }
	  
	  public double getCritChance(){
		  return intPoints * 0.025;
	  }
	  
	  public double getStaffDMG(){
		  return intPoints * 0.02;
	  }
	  
	  ItemStack loadIntItem() {
		int points = intPoints;
		boolean spent = tempintPoints > 0;
		
		return ItemManager.createItem(Material.EMPTY_MAP, ChatColor.DARK_PURPLE + "Intellect", new String[]{ChatColor.GRAY + "Adds energy regeneration,  ", ChatColor.GRAY + "elemental damage, critical ",
				ChatColor.GRAY + "hit chance, and staff damage.",
				ChatColor.AQUA + "Allocated Points: " + intPoints + (spent ? ChatColor.GREEN + " [+" + tempintPoints + "]" : ""),
				ChatColor.RED + "Free Points: " + tempFreePoints});
	}

	  DecimalFormat df = new DecimalFormat("##.###");

	  ItemStack loadDexItem() {
		int points = dexPoints;
		boolean spent = tempdexPoints > 0;
		
		return ItemManager.createItem(Material.EMPTY_MAP, ChatColor.DARK_PURPLE + "Dexterity", new String[]{ChatColor.GRAY + "Adds DPS%, dodge chance, armor ", ChatColor.GRAY + "penetration, and bow damage.",
				ChatColor.AQUA + "Allocated Points: " + dexPoints + (spent ? ChatColor.GREEN + " [+" + tempdexPoints + "]" : ""),
				ChatColor.RED + "Free Points: " + tempFreePoints});
		
	}

	  ItemStack loadDexStatsItem() {
	  		int dex = dexPoints;
	  		int aPoints = tempdexPoints; // allocated points
	  		boolean spent = (aPoints > 0) ? true : false;
	  		return ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.RED + "Dexterity Bonuses: " + dex + (spent ? ChatColor.GREEN + " [+" + aPoints + "]" : ""), new String[] {ChatColor.GOLD + "DPS: " + ChatColor.AQUA + df.format(dex * 0.03) + "%" + (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.03) + "%]" : ""),
				ChatColor.GOLD + "DODGE: " + ChatColor.AQUA + df.format(dex * 0.017) + "%"
						+ (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.017) + "%]" : ""),
		ChatColor.GOLD + "ARMOR PEN: " + ChatColor.AQUA + df.format(dex * 0.02) + "%" + (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.02) + "%]" : ""),
		ChatColor.GOLD + "BOW DMG: " + ChatColor.AQUA + df.format(dex * 0.015) + "%" + (spent ? ChatColor.GREEN + " [+" + df.format(aPoints * 0.015) + "%]" : "")});
		
	  }
	  
	  public double getDPS(){
		  return dexPoints * 0.03;
	  }
	  
	  public double getDodge(){
		  return dexPoints * 0.017;
	  }
	  
	  public double getArmorPen(){
		  return dexPoints * 0.02;
	  }
	  
	  public double getBowDMG(){
		  return dexPoints * 0.015;
	  }

	  	ItemStack loadConfirmItem() {
	  		return ItemManager.createItemWithData(Material.INK_SACK ,ChatColor.GREEN + "Confirm", new String[] {ChatColor.GRAY + "Click to confirm your stat ", ChatColor.GRAY + "point allocation.  If you ",
				ChatColor.GRAY + "want to undo your changes, ", ChatColor.GRAY + "press escape."}, DyeColor.LIME.getDyeData());
	  	}
	  	
	  
	  	public void lvlUp() {
			freePoints += PlayerStats.POINTS_PER_LEVEL * level;
			level += 1;
		}

		 public void onLogOff() {
			DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.INTELLECT, intPoints, false);
			DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.STRENGTH, strPoints, false);
			DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.VITALITY, vitPoints, false);
			DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.DEXTERITY, dexPoints, false);
			DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.BUFFER_POINTS, freePoints, false);
			DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.LEVEL, level, false);
		}

		/**
		 * Resets temp stats
		 */
		public void resetTemp() {
			tempFreePoints = freePoints;
			tempdexPoints = 0;
			tempintPoints = 0;
			tempstrPoints = 0;
			tempvitPoints = 0;			
		}

}
