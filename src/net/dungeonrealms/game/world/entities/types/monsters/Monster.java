package net.dungeonrealms.game.world.entities.types.monsters;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.API;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.Item.ItemRarity;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 21, 2015
 */
public interface Monster {

	public void onMonsterAttack(Player p);

	public void onMonsterDeath(Player killer);

	public EnumMonster getEnum();

	public default void checkItemDrop(int tier, EnumMonster monter, Entity ent, Player killer) {
		int killerLuck = DamageAPI.calculatePlayerLuck(killer);
		Location loc = ent.getLocation();
		World world = ((CraftWorld) loc.getWorld()).getHandle();
		int gemRoll = new Random().nextInt(99);
		if (gemRoll <= (20 + (20 * killerLuck / 100))) {
			if (gemRoll > 20) {
				if (Boolean.valueOf(
				        DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, killer.getUniqueId()).toString())) {
					killer.sendMessage(ChatColor.GREEN + "Your " + killerLuck + "% Luck has resulted in a drop.");
				}
			}
			double gem_drop_amount = 0;
			double drop_multiplier = 1;
			boolean is_elite = false;
			// Elite = 1.5x money chance / item chance.
			if (ent.hasMetadata("elite")) {
				is_elite = true;
			}

			if (is_elite) {
				drop_multiplier = 1.5;
			}
			double gold_drop_multiplier = 1;

			switch (tier) {
			case 1:
				gem_drop_amount = (new Random().nextInt(8 - 1) + 1) * gold_drop_multiplier;
				break;
			case 2:
				gem_drop_amount = (new Random().nextInt(18 - 2) + 2) * gold_drop_multiplier;
				break;
			case 3:
				gem_drop_amount = (new Random().nextInt(34 - 10) + 10) * gold_drop_multiplier;
				break;
			case 4:
				gem_drop_amount = (new Random().nextInt(64 - 20) + 20) * gold_drop_multiplier;
				break;
			case 5:
				gem_drop_amount = (new Random().nextInt(175 - 75) + 75) * gold_drop_multiplier;
				break;
			}

			ItemStack item = BankMechanics.gem.clone();
			item.setAmount((int) (gem_drop_amount * drop_multiplier));
			world.getWorld().dropItemNaturally(loc.add(0, 1, 0), item);
			return;
		}

		if (((LivingEntity) ent).getEquipment().getItemInHand().getType() == Material.BOW) {
			int arrowRoll = new Random().nextInt(99);
			if (arrowRoll <= (25 + (25 * killerLuck / 100))) {
				ItemStack item = new ItemStack(Material.ARROW);
				int amount = (tier * 2);
				item.setAmount(amount);
				world.getWorld().dropItemNaturally(loc.add(0, 1, 0), item);
			}
		}

		int chance = 0;
		switch (tier) {
		case 1:
			chance = 100;
			break;
		case 2:
			chance = 60;
			break;
		case 3:
			chance = 30;
			break;
		case 4:
			chance = 15;
			break;
		case 5:
			chance = 6;
			break;
		}
		int armorRoll = new Random().nextInt(1000);
		if (armorRoll <= chance + (chance * killerLuck / 100)) {
			if (armorRoll > chance) {
				if (Boolean.valueOf(
				        DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, killer.getUniqueId()).toString())) {
					killer.sendMessage(ChatColor.GREEN + "Your " + killerLuck + "% Luck has resulted in a drop.");
				}
			}
			ItemStack[] loot = new ItemStack[5];
			ItemStack[] armor = ((LivingEntity) ent).getEquipment().getArmorContents();
			ItemStack weapon = ((LivingEntity) ent).getEquipment().getItemInHand();
			if (ent.hasMetadata("elite"))
			    armor[3] = new ItemGenerator().setType(ItemType.HELMET).setTier(ItemTier.getById(tier)).setRarity(Item.ItemRarity.UNIQUE).getItem();
			else
	            armor[3] = new ItemGenerator().setType(ItemType.HELMET).setTier(ItemTier.getById(tier)).setRarity(API.getItemRarity()).getItem();

			loot = new ItemStack[] { armor[0], armor[1], armor[2], armor[3], weapon };
			ItemStack armorToDrop;
			switch (new Random().nextInt(6)) {
			case 0:
				armorToDrop = loot[0];
				break;
			case 1:
				armorToDrop = loot[1];
				break;
			case 2:
				armorToDrop = loot[2];
				break;
			case 3:
				armorToDrop = loot[3];
				break;
			case 4:
				armorToDrop = loot[4];
				break;
			case 5:
				armorToDrop = loot[4];
				break;
			default:
				armorToDrop = loot[1];
				break;
			}
			RepairAPI.setCustomItemDurability(armorToDrop, RandomHelper.getRandomNumberBetween(200, 1000));
			world.getWorld().dropItemNaturally(loc.add(0, 1, 0), armorToDrop);
		}
	}
}
