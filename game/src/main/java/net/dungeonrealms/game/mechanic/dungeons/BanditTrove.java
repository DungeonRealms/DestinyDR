package net.dungeonrealms.game.mechanic.dungeons;

import java.util.List;

import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BanditTrove extends Dungeon {
	public BanditTrove(List<Player> players) {
		super(DungeonType.BANDIT_TROVE, players);
	}

	@Override
	public ItemArmor getGeneralMobArmorSet() {
		return (ItemArmor) new ItemArmor().setTier(1).setMaxRarity(Item.ItemRarity.UNIQUE, 1);
	}

	@Override
	public ItemWeapon getGeneralMobWeapon() {
		//Want mostly common / uncommon since its meant to be easy?
		return (ItemWeapon) new ItemWeapon().setTier(1).setRarity(Item.ItemRarity.getRandomRarity());
	}
}
