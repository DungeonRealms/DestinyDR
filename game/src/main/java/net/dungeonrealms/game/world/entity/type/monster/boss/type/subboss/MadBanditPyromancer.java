package net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.base.DRWitherSkeleton;
import net.minecraft.server.v1_9_R2.World;

public class MadBanditPyromancer extends DRWitherSkeleton implements DungeonBoss {

	public MadBanditPyromancer(World world) {
		super(world, null);
	}
	
	@Override
	public void onBossDeath(Player killer) {
		//TODO: The map will need to be edited to use new items.
		ItemStack key = ItemManager.createItem(Material.GLOWSTONE_DUST, ChatColor.GREEN + "Magical Dust", ChatColor.ITALIC + "A strange substance that animates objects.");
        VanillaItem vi = new VanillaItem(key);
        vi.setDungeon(true);
		GameAPI.giveOrDropItem(killer, vi.generateItem());
	}

	@Override
	public BossType getBossType() {
		return BossType.Pyromancer;
	}

	@Override
	public String[] getItems() {
		return null;
	}

	@Override
	public void addKillStat(GamePlayer gp) {}
}
