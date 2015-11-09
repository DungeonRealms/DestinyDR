package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.items.armor.Armor.ArmorTier;
import net.dungeonrealms.items.armor.Armor.EquipmentType;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.miscellaneous.RandomHelper;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
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
		if (world.random.nextInt(100) <= 25) {
			ItemStack item = BankMechanics.gem.clone();
			item.setAmount(((tier * 10) - 8) + new Random().nextInt(5));
			world.getWorld().dropItemNaturally(loc.add(0, 2, 0), item);
		}

		if (RandomHelper.getRandomNumberBetween(1, 100) <= 5) {
			ItemStack[] loot = new ItemStack[5];
			ItemStack[] armor = ((LivingEntity) ent).getEquipment().getArmorContents();
			ItemStack weapon = ((LivingEntity) ent).getEquipment().getItemInHand();
			armor[3] = new ArmorGenerator().getArmor(EquipmentType.HELMET, ArmorTier.getByTier(tier), ArmorGenerator.getRandomItemModifier());
			loot = new ItemStack[]{armor[0], armor[1], armor[2], armor[3], weapon};
			int number = RandomHelper.getRandomNumberBetween(0, 4);
			ItemStack armorToDrop = loot[number];
			world.getWorld().dropItemNaturally(loc.add(0, 2, 0), armorToDrop);
		}
	}
}
