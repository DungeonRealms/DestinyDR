package net.dungeonrealms.game.world.entities.types.monsters.boss;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.BowMobs.RangedWitherSkeleton;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;

/**
 * Created by Chase on Oct 18, 2015
 */
public class Mayel extends RangedWitherSkeleton implements Boss {

	/**
	 * @param world
	 */
	public Mayel(World world) {
		super(world);
	}

	public Location loc;

	public Mayel(World world, Location loc) {
		super(world);
		this.loc = loc;
		setArmor(getEnumBoss().tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 100;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + "Mayel The Cruel");
		this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().name));
		for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
			p.sendMessage(ChatColor.RED.toString() + "Mayel The Cruel" + ChatColor.RESET.toString() + ": " + getEnumBoss().greeting);
		}
		this.setSize(0.7F, 2.4F);
		this.fireProof = true;
		this.setSkeletonType(1);
	}

	@Override
	public void setArmor(int tier) {
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getWeapon();
		ItemStack boots = ItemGenerator.getNamedItem("mayelboot");
		ItemStack legs = ItemGenerator.getNamedItem("mayelpants");
		ItemStack chest = ItemGenerator.getNamedItem("mayelchest");
		ItemStack head = ItemGenerator.getNamedItem("mayelhelmet");
		LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
		this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(boots));
		this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(legs));
		this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(chest));
		this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(head));
		livingEntity.getEquipment().setItemInMainHand(weapon);
		livingEntity.getEquipment().setBoots(boots);
		livingEntity.getEquipment().setLeggings(legs);
		livingEntity.getEquipment().setChestplate(chest);
		livingEntity.getEquipment().setHelmet(head);
	}

	/**
	 * @return
	 */
	private ItemStack getWeapon() {
		return ItemGenerator.getNamedItem("mayelbow");
	}

	/**
	 * Called when entity fires a projectile.
	 */
	@Override
	public void a(EntityLiving entityliving, float f) {
		net.minecraft.server.v1_9_R2.ItemStack nmsItem = this.getEquipment(EnumItemSlot.MAINHAND);
		NBTTagCompound tag = nmsItem.getTag();
		DamageAPI.fireArrowFromMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entityliving.getBukkitEntity());
	}

	@Override
	public void onBossDeath() {
		for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
			p.sendMessage(ChatColor.RED.toString() + "Mayel The Cruel" + ChatColor.RESET.toString() + ": " + "No... how could it be?");
		}
		int droppedGems = 64 * this.getBukkitEntity().getWorld().getPlayers().size();
		for (int i = 0; i < droppedGems; i++){
			this.getBukkitEntity().getWorld().dropItemNaturally(this.getBukkitEntity().getLocation().add(0, 4, 0), BankMechanics.createGems(1));
		}
	}

	private boolean canSpawn = true;

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		if (canSpawn) {
			canSpawn = false;
			for (int i = 0; i < 5; i++) {
				Entity entity = SpawningMechanics.getMob(world, 1, EnumMonster.MayelPirate);
				int level = Utils.getRandomFromTier(2, "high");
				String newLevelName = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
				MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 1, level);
				EntityStats.createDungeonMob(entity, level, 1);
				if (entity == null) {
					return; //WTF?? UH OH BOYS WE GOT ISSUES
				}
				entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
				entity.setCustomName(newLevelName + API.getTierColor(1).toString() + ChatColor.BOLD + "Mayels Crew");
				entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + API.getTierColor(1).toString() + ChatColor.BOLD + "Mayels Crew"));
				Location location = new Location(world.getWorld(), getBukkitEntity().getLocation().getX() + new Random().nextInt(3), getBukkitEntity().getLocation().getY(), getBukkitEntity().getLocation().getZ() + new Random().nextInt(3));
				entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
				((EntityInsentient) entity).persistent = true;
				((LivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
				world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
				entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
			}
			for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
				p.sendMessage(ChatColor.RED.toString() + "Mayel The Cruel" + ChatColor.RESET.toString() + ": " + "Come to my call, brothers!");
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> canSpawn = true, 100L);
		}
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.Mayel;
	}
}
