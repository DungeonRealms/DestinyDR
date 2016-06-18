package net.dungeonrealms.game.world.entities.types.monsters.boss;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.MeleeMobs.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Chase on Oct 19, 2015
 */
public class Burick extends MeleeWitherSkeleton implements Boss {

	public Location loc;

	public Burick(World world, Location loc) {
		super(world);
		this.loc = loc;
        this.getAttributeInstance(GenericAttributes.c).setValue(1d);
		this.setSkeletonType(1);
		setArmor(getEnumBoss().tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 100;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		for (Player p : API.getNearbyPlayers(loc, 50)) {
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
		for (Player p : API.getNearbyPlayers(loc, 50)) {
			p.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET.toString() + ": " + "I will have my revenge!");
		}
		int droppedGems = 64 * this.getBukkitEntity().getWorld().getPlayers().size();
		for (int i = 0; i < droppedGems; i++){
			this.getBukkitEntity().getWorld().dropItemNaturally(this.getBukkitEntity().getLocation().add(0, 4, 0), BankMechanics.createGems(1));
		}
	}

	private boolean first = false;
	private boolean second = false;
	private boolean third = false;

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		LivingEntity en = (LivingEntity) event.getEntity();
		int health = HealthHandler.getInstance().getMonsterMaxHPLive(en);
		int hp = HealthHandler.getInstance().getMonsterHPLive(en);
		float tenPercentHP = (float) (health * .10);
		if (hp <= tenPercentHP) {
			if (!first || !second || !third) {
				for (Player p : API.getNearbyPlayers(loc, 50)) {
					p.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET.toString() + ": " + "Goragath give me strength!");
				}
				HealthHandler.getInstance().healMonsterByAmount(en, HealthHandler.getInstance().getMonsterMaxHPLive(en));
				if (!first)
					first = true;
				else if (!second)
					second = true;
				else if (!third)
					third = true;
			}
		}
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.Burick;
	}

}
