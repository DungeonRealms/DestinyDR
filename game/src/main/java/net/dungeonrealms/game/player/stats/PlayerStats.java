package net.dungeonrealms.game.player.stats;

import lombok.SneakyThrows;
import net.dungeonrealms.database.LoadableData;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.SaveableData;
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

import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Redone by Kneesnap in early 2017.
 */
public class PlayerStats implements LoadableData, SaveableData {
    private UUID playerUUID;
    public int freeResets;
    private int level;
    public int resetAmounts;
    public boolean reset = true;
    private Map<Stats, Integer> statMap = new HashMap<>();
    private Map<Stats, Integer> tempStatMap = new HashMap<>();
    private int characterID;
    
    public final static int POINTS_PER_LEVEL = 3;
    
    public PlayerStats(UUID playerUUID, int characterID) {
        this.playerUUID = playerUUID;
        this.characterID = characterID;
        
    }

    public void setPlayerLevel(int lvl) {
        level = lvl;
    }
    
    public void setTempStat(Stats s, int val) {
    	tempStatMap.put(s, val);
    }
    
    public int getTempStat(Stats s){
        Integer stored = tempStatMap.get(s);
        if(stored != null)return stored;
    	return 0;
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

    @SuppressWarnings("deprecation")
	ItemStack loadConfirmItem() {
        ItemStack stack = ItemManager.createItem(Material.INK_SACK, ChatColor.GREEN + "Confirm", new String[]{"Click to confirm your stat ", "point allocation.  If you ",
                "want to undo your changes, ", "press escape."});
        stack.setDurability(DyeColor.LIME.getDyeData());
        return stack;
    }


    public void lvlUp() {
        int lvl = level + 1;
        if (lvl == 10 || lvl == 50)
            addReset();
        setPlayerLevel(lvl);
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
    }

    /**
     * Resets the player stats.
     */
    public void unallocateAllPoints() {
        resetTemp();
        PlayerWrapper.getPlayerWrapper(playerUUID).calculateAllAttributes();
    }

    public int getLevel() {
        return level;
    }

    public void addReset() {
        resetAmounts++;
    }
    
    @Override
    @SneakyThrows
    public void extractData(ResultSet resultSet) {
    	for (Stats s : Stats.values())
    		setStat(s, resultSet.getInt(s.getDBField()));
        this.freeResets = resultSet.getInt("attributes.freeResets");
        this.resetAmounts = resultSet.getInt("attributes.resets_available");
    }

    @Override
    public String getUpdateStatement() {
    	String sql = "UPDATE attributes SET ";
    	for (Stats s : Stats.values())
    		sql += s.name().toLowerCase() + " = '" + getStat(s) + "', ";
    	
    	sql += "resets_available = '%s', freeResets = '%s' WHERE character_id = '%s';";
        return String.format(sql, resetAmounts, freeResets, this.characterID);
    }

	public double getEnergyRegen() {
		return getStat(Stats.INTELLECT) * 0.00015;
	}
	
	public double getRegen() {
		return getHPRegen();
	}
	
	public double getHPRegen() {
		return getStat(Stats.VITALITY) * 0.03;
	}

	public double getDPS() {
		return getStat(Stats.DEXTERITY) * 0.03;
	}
}