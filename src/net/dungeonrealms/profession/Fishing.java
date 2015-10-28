package net.dungeonrealms.profession;

import java.util.Collections;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.mastery.Utils;

/**
 * Created by Chase on Oct 28, 2015
 */
public class Fishing {

	public static int T1Exp = 2500;
	public static int T2Exp = 5000;
	public static int T3Exp = 7500;
	public static int T4Exp = 9000;
	public static int T5Exp = 10000;

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
	 * Check if itemstack is a DR fishing pole.
	 * 
	 * @param stack
	 * @return boolean
	 */
	public static boolean isDRFishingPole(ItemStack stack) {
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		return nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("rod");
	}

	/**
	 * @param tier
	 * @return
	 */
	public static int getChance(int tier) {
		switch (tier) {
		case 1:
			return 10;
		case 2:
			return 20;
		case 3:
			return 30;
		case 4:
			return 40;
		case 5:
			return 50;
		}
		return 0;
	}

	/**
	 * @return
	 */
	public static int getSize(int tier) {
		int size = 0;
		switch (tier) {
		case 1:
			size = new Random().nextInt(4) + new Random().nextInt(2);
			break;
		case 2:
			size = new Random().nextInt(8) + new Random().nextInt(4);
			break;
		case 3:
			if (new Random().nextBoolean())
				size = new Random().nextInt(12) + new Random().nextInt(6) + new Random().nextInt(3);
			else
				size = new Random().nextInt(12) + new Random().nextInt(6) - new Random().nextInt(3);
			break;
		case 4:
			if (new Random().nextBoolean())
				size = new Random().nextInt(16) + new Random().nextInt(8) + new Random().nextInt(6);
			else
				size = new Random().nextInt(16) + new Random().nextInt(8) - new Random().nextInt(6);
			break;
		case 5:
			if (new Random().nextBoolean())
				size = new Random().nextInt(20) + new Random().nextInt(10) + new Random().nextInt(9);
			else
				size = new Random().nextInt(20) + new Random().nextInt(10) - new Random().nextInt(9);
			break;
		}
		if(size <= 0)
			size = new Random().nextInt(5);
		return size;
	}

	/**
	 * 
	 * 
	 * @param tier
	 * @return String
	 * 
	 */
	public static String getFish(int tier) {
		// TODO MAKE MORE FISH AND CHANCES FOR POLES
		return "Bass";
	}

	/**
	 * @param stack
	 * @return
	 */
	public static ItemStack getFishItem(ItemStack stack) {
		ItemStack fish = new ItemStack(Material.RAW_FISH);
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		int tier = nms.getTag().getInt("itemTier");
		ItemMeta meta = fish.getItemMeta();
		Utils.log.info(tier + "tier");
		int size = Fishing.getSize(tier);
		meta.setDisplayName(size + "in. " + Fishing.getFish(tier));
		fish.setItemMeta(meta);
		nms = CraftItemStack.asNMSCopy(fish);
		nms.getTag().setInt("size", size);
		return CraftItemStack.asBukkitCopy(nms);
	}

	/**
	 * @param stack
	 */
	public static void gainExp(ItemStack stack, Player p) {
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		int xp = nms.getTag().getInt("XP");
		int maxXP = nms.getTag().getInt("maxXP");
		xp += 25;
		nms.getTag().setInt("XP", xp);

		double percentDone = 100.0 * xp / maxXP;
		double percentDoneDisplay = (percentDone / 100) * 50.0D;
		int display = (int) percentDoneDisplay;
		if (display <= 0) {
			display = 1;
		}
		if (display > 50) {
			display = 50;
		}
		String expBar = "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
		String newexpBar = ChatColor.GREEN.toString() + expBar.substring(0, display) + ChatColor.RED.toString()
		        + expBar.substring(display, expBar.length());
		ItemMeta meta = stack.getItemMeta();
		meta.setLore(Collections.singletonList(newexpBar));
		stack.setItemMeta(meta);
		p.setItemInHand(stack);

	}

	/**
	 * @param rodStack
	 * @return
	 */
	public static int getRodTier(ItemStack rodStack) {
		return CraftItemStack.asNMSCopy(rodStack).getTag().getInt("itemTier");
	}

}
