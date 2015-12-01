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
		if (world.random.nextInt(100) <= 20) {
            double gem_drop_amount = 0;
            double drop_multiplier = 1;
            boolean is_elite = false;
            // Elite = 1.5x money chance / item chance.
            if(ent.hasMetadata("elite"))
                is_elite = true;

            if (is_elite == true) {
                drop_multiplier = 1.5;
            }
            double gold_drop_multiplier = 1;  //TODO GEM DROP ENCHANTS?
            
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
                    gem_drop_amount = (new Random().nextInt(50 - 20) + 20) * gold_drop_multiplier;
                    break;
            	case 5:
                    gem_drop_amount = (new Random().nextInt(200 - 75) + 75) * gold_drop_multiplier;
                    break;
            }
            
			ItemStack item = BankMechanics.gem.clone();
			item.setAmount((int) (gem_drop_amount * drop_multiplier));
			world.getWorld().dropItemNaturally(loc.add(0, 2, 0), item);
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
			chance = 200;
			break;
		case 2:
			chance = 100;
			break;
		case 3:
			chance = 75;
			break;
		case 4:
			chance = 40;
			break;
		case 5:
			chance = 10;
			break;
		}
		if (RandomHelper.getRandomNumberBetween(0, 1000) <= chance) {
			ItemStack[] loot = new ItemStack[5];
			ItemStack[] armor = ((LivingEntity) ent).getEquipment().getArmorContents();
			ItemStack weapon = ((LivingEntity) ent).getEquipment().getItemInHand();
			armor[3] = new ArmorGenerator().getArmor(EquipmentType.HELMET, ArmorTier.getByTier(tier), API.getArmorModifier());
			loot = new ItemStack[]{armor[0], armor[1], armor[2], armor[3], weapon};
			int number = RandomHelper.getRandomNumberBetween(0, 4);
			ItemStack armorToDrop = loot[number];
			world.getWorld().dropItemNaturally(loc.add(0, 2, 0), armorToDrop);
		}
		
		if(RandomHelper.getRandomNumberBetween(1, 500) <= 2){
			world.getWorld().dropItemNaturally(loc.add(0, 2, 0), ItemManager.createProtectScroll(tier));
		}
	}
}
