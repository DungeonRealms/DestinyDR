package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.API;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.items.armor.Armor.ArmorTier;
import net.dungeonrealms.game.world.items.armor.Armor.EquipmentType;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.world.items.armor.ArmorGenerator;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by Chase on Oct 21, 2015
 */
public interface Monster {

	public void onMonsterAttack(Player p);

	public void onMonsterDeath();

	public EnumMonster getEnum();

	public default void checkItemDrop(int tier, EnumMonster monter, Entity ent) {
		Location loc = ent.getLocation();
		World world = ((CraftWorld) loc.getWorld()).getHandle();
		if (new Random().nextInt(100) <= 20) {
            double gem_drop_amount = 0;
            double drop_multiplier = 1;	
            boolean is_elite = false;
            // Elite = 1.5x money chance / item chance.
            if(ent.hasMetadata("elite"))
                is_elite = true;

            if (is_elite == true) {
                drop_multiplier = 1.5;
            }
            double gold_drop_multiplier = 1;
            
            switch(tier){
            	case 1 :
            		gem_drop_amount = (new Random().nextInt(3 - 1) + 1) * gold_drop_multiplier;
            		break;
            	case 2:
                    gem_drop_amount = (new Random().nextInt(12 - 2) + 2) * gold_drop_multiplier;
            		break;
            	case 3:
                    gem_drop_amount = (new Random().nextInt(30 - 10) + 10) * gold_drop_multiplier;
                    break;
            	case 4:
                    gem_drop_amount = (new Random().nextInt(40 - 20) + 20) * gold_drop_multiplier;
                    break;
            	case 5:
                    gem_drop_amount = (new Random().nextInt(150 - 75) + 75) * gold_drop_multiplier;
                    break;
            }
            
			ItemStack item = BankMechanics.gem.clone();
			item.setAmount((int) (gem_drop_amount * drop_multiplier));
			world.getWorld().dropItemNaturally(loc.add(0, 2, 0), item);
			return;
		}
		
		if(((LivingEntity) ent).getEquipment().getItemInHand().getType() == Material.BOW){
			if(RandomHelper.getRandomNumberBetween(1, 100) <= 25){
				ItemStack item = new ItemStack(Material.ARROW);
				int amount = (tier * 2); 
				item.setAmount(amount);
				world.getWorld().dropItemNaturally(loc.add(0, 2, 0), item);
			}
		}
		
		int chance = 0;
		switch(tier){
		case 1:
			chance = 125;
			break;
		case 2:
			chance = 75;
			break;
		case 3:
			chance = 30;
			break;
		case 4:
			chance = 20;
			break;
		case 5:
			chance = 5;
			break;
		}
		if (new Random().nextInt(1000) <= chance) {
			ItemStack[] loot = new ItemStack[5];
			ItemStack[] armor = ((LivingEntity) ent).getEquipment().getArmorContents();
			ItemStack weapon = ((LivingEntity) ent).getEquipment().getItemInHand();
			armor[3] = new ArmorGenerator().getArmor(EquipmentType.HELMET, ArmorTier.getByTier(tier), API.getArmorModifier());
			loot = new ItemStack[]{armor[0], armor[1], armor[2], armor[3], weapon};
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

			world.getWorld().dropItemNaturally(loc.add(0, 2, 0), armorToDrop);
			return;
		}
		
		if(new Random().nextInt(1000) <= 2){
			world.getWorld().dropItemNaturally(loc.add(0, 2, 0), ItemManager.createProtectScroll(tier));
		}
	}
}
