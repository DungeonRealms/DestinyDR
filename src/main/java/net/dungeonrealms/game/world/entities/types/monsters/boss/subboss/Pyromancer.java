package net.dungeonrealms.game.world.entities.types.monsters.boss.subboss;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.StaffMobs.StaffSkeleton;
import net.dungeonrealms.game.world.entities.types.monsters.boss.Boss;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Chase on Oct 19, 2015
 */
public class Pyromancer extends StaffSkeleton implements Boss {

	public Pyromancer(World world) {
		super(world);
		this.setSkeletonType(1);
		this.setSize(0.7F, 2.4F);
		this.fireProof = true;
		setArmor(getEnumBoss().tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 40;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity().setCustomName(ChatColor.YELLOW.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		this.persistent = true;
		((LivingEntity)this.getBukkitEntity()).setRemoveWhenFarAway(false);
	}

	@Override
	public void onBossDeath() {
		for (Player p : API.getNearbyPlayers(this.getBukkitEntity().getLocation(), 50)) {
			p.sendMessage(this.getCustomName() + ChatColor.RESET.toString() + ": " + getEnumBoss().death);
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()-> world.getWorld().dropItemNaturally(this.getBukkitEntity().getLocation().add(0, 2, 0), ItemManager.createItem(Material.GLOWSTONE_DUST, ChatColor.GREEN + "Magical Dust", new String[] {ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "A strange substance that animates objects.", ChatColor.RED + "Dungeon Item"})), 10);
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
			this.getBukkitEntity().getWorld().getBlockAt(641, 55, -457).setType(Material.REDSTONE_TORCH_ON);
			Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () ->
			this.getBukkitEntity().getWorld().getBlockAt(641, 55, -457).setType(Material.AIR), 20);

		}, 20 * 5);
	}

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.Pyromancer;
	}

}
