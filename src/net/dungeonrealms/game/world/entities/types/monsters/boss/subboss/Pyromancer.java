package net.dungeonrealms.game.world.entities.types.monsters.boss.subboss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.boss.Boss;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.ItemGenerator;
import net.dungeonrealms.game.world.items.armor.ArmorGenerator;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 19, 2015
 */
public class Pyromancer extends EntitySkeleton implements Boss {

	public Location loc;

	public Pyromancer(World world, Location loc) {
		super(world);
		this.loc = loc;
		this.setSkeletonType(1);
		setArmor(getEnumBoss().tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 40;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss",
		        new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity()
		        .setCustomName(ChatColor.YELLOW.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);

	}

	protected void setArmor(int tier) {
		ItemStack[] armor = getArmor();
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getWeapon();
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(4, getHead());
	}

	/**
	 * @return
	 */
	private ItemStack getWeapon() {
		return new ItemGenerator().next(net.dungeonrealms.game.world.items.Item.ItemType.STAFF,
		        net.dungeonrealms.game.world.items.Item.ItemTier.getByTier(1));
	}

	/**
	 * Called when entity fires a projectile.
	 */
	@Override
	public void a(EntityLiving entityliving, float f) {
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = this.getEquipment(0);
		NBTTagCompound tag = nmsItem.getTag();
		DamageAPI.fireArrowFromMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entityliving.getBukkitEntity());

	}

	protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner("Steve");
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
	}

	private ItemStack[] getArmor() {
		return new ArmorGenerator().nextTier(getEnumBoss().tier);
	}

	@Override
	public void onBossDeath() {
		
		for (Player p : API.getNearbyPlayers(this.getBukkitEntity().getLocation(), 50)) {
			p.sendMessage(this.getCustomName() + ChatColor.RESET.toString() + ": " + getEnumBoss().death);
		}
//		641, 55, -457
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()-> world.getWorld().dropItemNaturally(this.getBukkitEntity().getLocation().add(0, 2, 0), ItemManager.createItem(Material.GLOWSTONE_DUST, ChatColor.GREEN + "Magical Dust", new String[] {ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "A strange substance that animates objects.", ChatColor.RED + "Dungeon Item"})), 10);
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
			this.getBukkitEntity().getWorld().createExplosion(new Location(this.getBukkitEntity().getWorld(), 641, 55, -457), 20);
		}
		, 20 * 40);
	}

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		LivingEntity en = (LivingEntity) event.getEntity();		
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.Pyromancer;
	}

}
