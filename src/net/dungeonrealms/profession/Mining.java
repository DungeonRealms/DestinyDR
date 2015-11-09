package net.dungeonrealms.profession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;

/**
 * Created by Chase on Oct 27, 2015
 */
public class Mining implements GenericMechanic {

	public static int T1Exp = 100000;
	public static int T2Exp = 250000;
	public static int T3Exp = 400000;
	public static int T4Exp = 550000;
	public static int T5Exp = 700000;

	public static int getMaxXP(int tier) {
		switch (tier) {
		case 1:
			return T1Exp;
		case 2:
			return T2Exp;
		case 3:
			return T3Exp;
		case 4:
			return T4Exp;
		case 5:
			return T5Exp;
		default:
			return -1;
		}
	}

	/**
	 * Checks if itemstack is our pickaxe
	 *
	 * @param stack
	 * @return boolean
	 * @since 1.0
	 */
	public static boolean isDRPickaxe(ItemStack stack) {
		if(stack.getType() == Material.WOOD_PICKAXE || stack.getType() == Material.STONE_PICKAXE || stack.getType() == Material.IRON_PICKAXE || stack.getType() == Material.GOLD_PICKAXE || stack.getType() == Material.DIAMOND_PICKAXE){
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		return !(nms == null || nms.getTag() == null) && nms.getTag().hasKey("type")
		        && nms.getTag().getString("type").equalsIgnoreCase("pick");
		}
		return false;
	}

	/**
	 * Returns tier of our pick itemstack.
	 *
	 * @param stack
	 * @return Integer
	 */
	public static int getPickTier(ItemStack stack) {
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		return nms.getTag().getInt("itemTier");
	}

	/**
	 * Check amount of exp pick gets from block mined
	 *
	 * @param stackInHand
	 * @param type
	 * @return integer
	 * @since 1.0
	 */
	public static int getExperienceGain(ItemStack stackInHand, Material type) {
		int pickTier = Mining.getPickTier(stackInHand);
		int multiplier = 1;
		int gain = 0;
		switch (type) {
		// TODO INCORPORATE LOWER GAIN FOR USING HIGHER PICK ON LOWER TIER;
		case COAL_ORE:
			gain = 100;
			break;
		case EMERALD_ORE:
			gain = 200;
			break;
		case IRON_ORE:
			gain = 300;
			break;
		case DIAMOND_ORE:
			gain = 400;
			break;
		case GOLD_ORE:
			gain = 500;
			break;
		default:
			return 0;
		}
		gain = (gain * multiplier);
		int guildExp = (int) (gain * (1.0f / 100.0f));
		// I Don't Fucking Know, but here's the exxp to give guilds.
		return gain;
	}

	/**
	 * Returns the respawn time of ore in seconds
	 *
	 * @param oreType
	 * @return integer
	 * @since 1.0
	 */
	public static int getOreRespawnTime(Material oreType) {
		switch (oreType) {
		case COAL_ORE:
			return 120;
		case EMERALD_BLOCK:
			return 300;
		case IRON_ORE:
			return 600;
		case DIAMOND_ORE:
			return 1200;
		case GOLD_ORE:
			return 2400;
		}
		return 0;
	}

	/**
	 * Adds experienceGain to players pick
	 *
	 * @param stackInHand
	 * @param experienceGain
	 * @param p
	 * @since 1.0
	 */
	public static void addExperience(ItemStack stackInHand, int experienceGain, Player p) {
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stackInHand);
		int currentXP = nms.getTag().getInt("XP");
		int maxXP = nms.getTag().getInt("maxXP");
		int tier = nms.getTag().getInt("itemTier");
		currentXP += experienceGain;
		if (currentXP > maxXP) {
			upgradePickaxe(tier, p);
			return;
		}
		nms.getTag().setInt("XP", currentXP);
		stackInHand = CraftItemStack.asBukkitCopy(nms);
		p.setItemInHand(stackInHand);
		ItemMeta meta = stackInHand.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
        lore.add(currentXP + "/" + Mining.getMaxXP(tier));
        String expBar = "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
        double percentDone = 100.0 * currentXP / maxXP;
        double percentDoneDisplay = (percentDone / 100) * 50.0D;
        int display = (int) percentDoneDisplay;
        if (display <= 0) {
        	display = 1;
        }
        if (display > 50) {
        	display = 50;
        }
        String newexpBar = ChatColor.GREEN.toString() + expBar.substring(0, display) + ChatColor.RED.toString()
        	        + expBar.substring(display, expBar.length());
        lore.add(newexpBar);
        
        lore.add(ChatColor.GREEN.toString() + currentXP + "/" + maxXP);
        lore.add(" ");
        lore.add(newexpBar);
        lore.add(" ");
        
        switch (tier) {
        case 1:
            lore.add(ChatColor.GRAY.toString() + ChatColor.UNDERLINE + "A pick made out of Wood");
            break;
        case 2:
            lore.add(ChatColor.GRAY.toString() + ChatColor.UNDERLINE + "A pick made out of Stone");
            break;
        case 3:
            lore.add(ChatColor.GRAY.toString() + ChatColor.UNDERLINE + "A pick made out of Iron");
            break;
        case 4:
            lore.add(ChatColor.GRAY.toString() + ChatColor.UNDERLINE + "A pick made out of Diamond");
            break;
        case 5:
            lore.add(ChatColor.GRAY.toString() + ChatColor.UNDERLINE + "A pick made out of Gold");
            break;
        default:
            break;
    }
        meta.setLore(lore);
		stackInHand.setItemMeta(meta);
		p.setItemInHand(stackInHand);
	}

	/**
	 * Sets players item in hand to upgraded Tier
	 *
	 * @param tier
	 * @param p
	 * @since 1.0
	 */
	private static void upgradePickaxe(int tier, Player p) {
		if (tier < 6)
			p.setItemInHand(ItemManager.createPickaxe(tier + 1));
	}

	/**
	 * @param type
	 * @return
	 */
	public static int getBlockTier(Material type) {
		switch (type) {
		case COAL_ORE:
			return 1;
		case EMERALD_ORE:
			return 2;
		case IRON_ORE:
			return 3;
		case DIAMOND_ORE:
			return 4;
		case GOLD_ORE:
			return 5;
		default:
			return 0;
		}
	}

	private HashMap<Location, Material> ORE_LOCATIONS = new HashMap<>();

	public void loadOreLocations() {
		int count = 0;
        ArrayList<String> CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("orespawns");
			for(String line : CONFIG) {
				if (line.contains("=")) {
					try {
						String[] cords = line.split("=")[0].split(",");
						Location loc = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(cords[0]),
						        Double.parseDouble(cords[1]), Double.parseDouble(cords[2]));

						String material_data = line.split("=")[1];
						Material m = Material.getMaterial(material_data);

						ORE_LOCATIONS.put(loc, m);

						count++;
					} catch (NullPointerException npe) {
						npe.printStackTrace();
						continue;
					}
				}
			}
			Utils.log.info("[Profession] " + count + " ORE SPAWN locations have been LOADED.");
	}

	private static Mining instance;
	
	public static Mining getInstance() {
		if (instance == null)
			instance = new Mining();
		return instance;

	}

	@Override
	public EnumPriority startPriority() {
		return EnumPriority.CATHOLICS;
	}

	@Override
	public void startInitialization() {
		loadOreLocations();
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), this::placeOre);
	}

	/**
	 * Place all ore from ORE_LOCATIONS
	 * @since 1.0;
	 */
	private void placeOre() {
		ORE_LOCATIONS.keySet().stream().forEach(loc -> loc.getWorld().getBlockAt(loc).setType(ORE_LOCATIONS.get(loc)));
	}

	@Override
	public void stopInvocation() {

	}
}
