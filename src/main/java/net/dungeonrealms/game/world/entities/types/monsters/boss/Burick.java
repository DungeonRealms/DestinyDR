package net.dungeonrealms.game.world.entities.types.monsters.boss;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.MeleeMobs.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Chase on Oct 19, 2015
 */
public class Burick extends MeleeWitherSkeleton implements Boss {

	public Location loc;

	public Burick(World world) {
		super(world);
	}

	public Burick(World world, Location loc) {
		super(world);
		this.loc = loc;
		setArmor(getEnumBoss().tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 100;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + "Burick The Fanatic");
		this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().name));
		for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
			p.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET.toString() + ": " + "Ahahaha! You dare try to kill ME?! I am Burick, disciple of Goragath! None of you will leave this place alive!");
		}
		this.setSize(0.7F, 2.4F);
		this.fireProof = true;
		this.setSkeletonType(1);
	}

	@Override
	public void setArmor(int tier) {
		ItemStack weapon = getWeapon();
		ItemStack boots = ItemGenerator.getNamedItem("up_boots");
		ItemStack legs = ItemGenerator.getNamedItem("up_leggings");
		ItemStack chest = ItemGenerator.getNamedItem("up_chest");
		ItemStack head = ItemGenerator.getNamedItem("up_helmet");
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
	    return ItemGenerator.getNamedItem("up_axe");
	}

	@Override
	public void onBossDeath() {
		for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
			p.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET.toString() + ": " + "I will have my revenge!");
		}
		int droppedGems = 64 * this.getBukkitEntity().getWorld().getPlayers().size();
		for (int i = 0; i < droppedGems; i++){
			this.getBukkitEntity().getWorld().dropItemNaturally(this.getBukkitEntity().getLocation().add(0, 4, 0), BankMechanics.createGems(1));
		}
	}

	private boolean firstHeal = false;
	private boolean secondHeal = false;
	private boolean thirdHeal = false;
	private CopyOnWriteArrayList<Entity> spawnedMobs = new CopyOnWriteArrayList<>();
	private boolean canAddsRespawn = true;

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		for (Entity entity : spawnedMobs) {
			if (!entity.isAlive()) {
				spawnedMobs.remove(entity);
			}
		}
		if (spawnedMobs.size() > 0) {
			event.setDamage(0);
			event.setCancelled(true);
			return;
		}
		LivingEntity en = (LivingEntity) event.getEntity();
		int health = HealthHandler.getInstance().getMonsterMaxHPLive(en);
		int hp = HealthHandler.getInstance().getMonsterHPLive(en);
		float tenPercentHP = (float) (health * .10);
		if (hp <= (float) (health * 0.5)) {
			if (canAddsRespawn) {
				for (int i = 0; i < 4; i++) {
					Entity entity = SpawningMechanics.getMob(world, 1, EnumMonster.Acolyte);
					int level = Utils.getRandomFromTier(3, "high");
					String newLevelName = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
					MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 3, level);
					EntityStats.createDungeonMob(entity, level, 3);
					if (entity == null) {
						return; //WTF?? UH OH BOYS WE GOT ISSUES
					}
					entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
					entity.setCustomName(newLevelName + API.getTierColor(3).toString() + ChatColor.BOLD + "Burick's Acolyte");
					entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + API.getTierColor(3).toString() + ChatColor.BOLD + "Burick's Acolyte"));
					Location location = new Location(world.getWorld(), getBukkitEntity().getLocation().getX() + new Random().nextInt(3), getBukkitEntity().getLocation().getY(), getBukkitEntity().getLocation().getZ() + new Random().nextInt(3));
					entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
					((EntityInsentient) entity).persistent = true;
					((LivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
					world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
					entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
					spawnedMobs.add(entity);
				}
				for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
					p.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET.toString() + ": " + "Come to my aid, Acolytes!");
				}
			}
		}
		if (hp <= tenPercentHP) {
			if (!firstHeal && !secondHeal && !thirdHeal) {
				for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
					p.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET.toString() + ": " + "Goragath give me strength!");
				}
				HealthHandler.getInstance().healMonsterByAmount(en, HealthHandler.getInstance().getMonsterMaxHPLive(en));
				HealthHandler.getInstance().setMonsterHPLive(en, HealthHandler.getInstance().getMonsterMaxHPLive(en));
				canAddsRespawn = true;
				if (!firstHeal) {
					firstHeal = true;
				}
				else if (!secondHeal) {
					secondHeal = true;
				}
				else if (!thirdHeal) {
					thirdHeal = true;
				}
			}
		}
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.Burick;
	}

}
