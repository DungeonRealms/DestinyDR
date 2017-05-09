package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.world.item.DamageAPI;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A simple staff item.
 * @author Kneesnap
 */
public class ItemWeaponStaff extends ItemWeaponRanged {
	public ItemWeaponStaff() {
		super(ItemType.STAFF);
	}
	
	public ItemWeaponStaff(ItemStack item) {
		super(item);
	}
	
	public static boolean isStaff(ItemStack item) {
		return isType(item, ItemType.STAFF);
	}

	@Override
	public int getShootDelay() {
		return 100;
	}
	
	@Override
	public Sound getShootSound() {
		return Sound.BLOCK_DISPENSER_LAUNCH;
	}

	@Override
	public void fireProjectile(Player player, boolean takeDurability) {
		DamageAPI.fireStaffProjectile(player, this, takeDurability);
	}
}
