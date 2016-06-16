package net.dungeonrealms.game.world.entities.types.monsters.boss;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
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
		this.getBukkitEntity().setMetadata("boss",
		        new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity()
		        .setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		for (Player p : API.getNearbyPlayers(loc, 50)) {
			p.sendMessage(this.getCustomName() + ChatColor.RESET.toString() + ": " + getEnumBoss().greeting);
		}
		this.setSize(0.7F, 2.4F);
		this.fireProof = true;
		this.setSkeletonType(1);

	}

	@Override
	public void setArmor(int tier) {
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getWeapon();
		// weapon.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("up_boots")));
		this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("up_leggings")));
		this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("up_chest")));
		this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("up_helmet")));
	}

	/**
	 * @return
	 */
	private ItemStack getWeapon() {
	    return ItemGenerator.getNamedItem("up_axe");
	}

	@Override
	public void onBossDeath() {
		say(this.getBukkitEntity(), getEnumBoss().death);
//		List<Player> list = API.getNearbyPlayers(this.getBukkitEntity().getLocation(), 50);
//		Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), ()->{
//			for(Player p : list){
//			p.teleport(Teleportation.Cyrennica);
//			}
//		}, 20*30);
//		for(Player p : list){
//			p.sendMessage("You will be teleported out in 30 seconds");
//		}
	}

	public boolean first = false;
	public boolean second = false;
	public boolean third = false;

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		LivingEntity en = (LivingEntity) event.getEntity();
		int health = HealthHandler.getInstance().getMonsterMaxHPLive(en);
		int hp = HealthHandler.getInstance().getMonsterHPLive(en);
		float tenPercentHP = (float) (health * .10);
		if (hp <= tenPercentHP) {
			if (!first || !second || !third) {
				for (Player p : API.getNearbyPlayers(en.getLocation(), 50)) {
					p.sendMessage(
					        this.getCustomName() + ChatColor.RESET.toString() + ": " + " Goragath give me strength!");
				}
				HealthHandler.getInstance().healMonsterByAmount(en,
				        HealthHandler.getInstance().getMonsterMaxHPLive(en));
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
