package net.dungeonrealms.game.player.stats;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.mastery.StatBoost;
import net.dungeonrealms.game.mastery.Stats;
import net.dungeonrealms.game.mechanic.ItemManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Chase on Nov 2, 2015
 */
public class PlayerStats {
    private UUID playerUUID;
    public int freeResets;
    private int level;
    public final static int POINTS_PER_LEVEL = 3;
    public int resetAmounts;
    public boolean reset = true;
    private Map<Stats, Integer> statMap = new HashMap<>();
    private Map<Stats, Integer> tempStatMap = new HashMap<>();

    public PlayerStats(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.resetAmounts = 0;
        this.freeResets = 0;
        loadPlayerStats();
    }


    public void setPlayerLevel(int lvl) {
        level = lvl;
    }

    /**
     * gets stat points from the database for UUID
     *
     * @since 1.0;
     */
    public void loadPlayerStats() {
        for(Stats s : Stats.values())
        	setStat(s, (int) DatabaseAPI.getInstance().getData(s.getData(), playerUUID));
        this.level = (int) DatabaseAPI.getInstance().getData(EnumData.LEVEL, playerUUID);
        this.resetAmounts = (int) DatabaseAPI.getInstance().getData(EnumData.RESETS, playerUUID);
        this.freeResets = (int) DatabaseAPI.getInstance().getData(EnumData.FREERESETS, playerUUID);
    }
    
    public void setTempStat(Stats s, int val) {
    	tempStatMap.put(s, val);
    }
    
    public int getTempStat(Stats s){ 
    	return tempStatMap.get(s);
    }
    
    public void setStat(Stats s, int val) {
    	statMap.put(s, val);
    }
    
    public int getStat(Stats s) {
    	return statMap.get(s);
    }
    
    public int getFreePoints() {
    	int usedPoints = 0;
    	for(Stats s : Stats.values())
    		usedPoints += getTempStat(s);
    	return (POINTS_PER_LEVEL  * (getLevel() + 2)) - usedPoints;
    }
    
    public void openMenu(Player player) {
    	Inventory inv = Bukkit.createInventory(null, 18, "Stat Points");
    	inv.setItem(0, loadStatsInfoItem());
    	inv.setItem(6, loadConfirmItem());
    	player.openInventory(inv);
    }

    public void allocatePoint(Stats s, Inventory inv) {
        if (getFreePoints() > 0)
            setTempStat(s, getTempStat(s) + 1);
        updateItems(inv);
    }
    
    private ItemStack loadStatItem(Stats s) {
    	DecimalFormat df = new DecimalFormat("##.###");
    	int temp = getTempStat(s);
    	int stat = getStat(s);
    	boolean buy = temp > 0;
    	
    	List<String> list = new ArrayList<>();
    	for(StatBoost boost : s.getStatBoosts()) {
    		String prefix = ChatColor.stripColor(boost.getType().getPrefix());
    		list.add(ChatColor.GOLD + prefix + ChatColor.AQUA + df.format(stat * boost.getMultiplier()) + boost.getType().getSuffix() + " " + (buy ? ChatColor.GREEN + "[+" + df.format(temp * boost.getMultiplier()) + "]" : ""));
    	}
    	String[] arr = new String[list.size()];
    	list.toArray(arr);
    	return ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.RED + s.name() + " Bonuses: " + getStat(s) + (buy ? ChatColor.GREEN + "[+" + temp + "]" : ""), arr);
    }
    
    private ItemStack loadDescItem(Stats s) {
    	List<String> list = new ArrayList<>();
    	
    	for(String desc : s.getDescription())
    		list.add(ChatColor.GRAY + desc);
    	
    	list.add(ChatColor.AQUA + "Allocated Points: " + getStat(s) + (getStat(s) > 0 ? ChatColor.GREEN + " [+" + getTempStat(s) + "]" : ""));
    	list.add(ChatColor.RED + "Free Points: " + getFreePoints());
    	String[] lore = new String[list.size()];
    	list.toArray(lore);
    	return ItemManager.createItem(Material.EMPTY_MAP, ChatColor.DARK_PURPLE + s.name(), lore);
    }

    public void updateItems(Inventory openInventory) {
    	for (int i = 2; i < Stats.values().length; i++) {
    		Stats s = Stats.values()[i - 2];
    		openInventory.setItem(i, loadStatItem(s));
    		openInventory.setItem(i + 9, loadDescItem(s));
    	}
    }

    public void removePoint(Stats s, Inventory inv) {
        int val = getTempStat(s);
        if (val > 0)
        	setTempStat(s, val - 1);
        updateItems(inv);
    }

    ItemStack loadStatsInfoItem() {

        return ItemManager.createItem(Material.ENCHANTED_BOOK, ChatColor.YELLOW + "Stat Point Info", new String[]{ChatColor.LIGHT_PURPLE + "Points to Allocate: " + getFreePoints(),
                ChatColor.AQUA + "LCLICK" + ChatColor.GRAY + " to allocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "1" + ChatColor.GRAY + " point",
                ChatColor.AQUA + "RCLICK" + ChatColor.GRAY + " to unallocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "1" + ChatColor.GRAY + " point",
                ChatColor.AQUA + "S-LCLICK" + ChatColor.GRAY + " to allocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "3" + ChatColor.GRAY + " points",
                ChatColor.AQUA + "S-RCLICK" + ChatColor.GRAY + " to unallocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "3" + ChatColor.GRAY + " points",
                ChatColor.AQUA + "MCLICK" + ChatColor.GRAY + " to allocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "custom" + ChatColor.GRAY + " points",
        });
    }

    ItemStack loadConfirmItem() {
        return ItemManager.createItemWithData(Material.INK_SACK, ChatColor.GREEN + "Confirm", new String[]{ChatColor.GRAY + "Click to confirm your stat ", ChatColor.GRAY + "point allocation.  If you ",
                ChatColor.GRAY + "want to undo your changes, ", ChatColor.GRAY + "press escape."}, DyeColor.LIME.getDyeData());
    }


    public void lvlUp() {
        int lvl = level + 1;
        if (lvl == 10 || lvl == 50)
            addReset();
        setPlayerLevel(lvl);
    }

    /**
     * Called to sync database with players server stats
     */

    public void updateDatabase() {
        DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.LEVEL, level, true);
        for (Stats s : Stats.values())
        	DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, s.getData(), getStat(s), true);
        DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.RESETS, resetAmounts, true);
        DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.FREERESETS, freeResets, true);
    }

    /**
     * Resets temp stats
     */
    public void resetTemp() {
        for (Stats s : Stats.values())
        	setTempStat(s, 0);
    }
    
    public void confirmStats() {
    	for(Stats s : Stats.values()) {
    		setStat(s, getStat(s) + getTempStat(s));
    		setTempStat(s, 0);
    	}
    	updateDatabase();
    }

    /**
     * Resets the player stats.
     *
     * @since 1.0
     */
    public void unallocateAllPoints() {
        resetTemp();
        updateDatabase();
        GameAPI.getGamePlayer(Bukkit.getPlayer(playerUUID)).calculateAllAttributes();
    }

    public int getLevel() {
        return level;
    }

    public void addReset() {
        resetAmounts++;
        DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.RESETS, resetAmounts, true);
    }


	public double getEnergyRegen() {
		return getStat(Stats.INTELLECT) * 0.00015;
	}
	
	public double getHPRegen() {
		return getStat(Stats.VITALITY) * 0.03;
	}
}
