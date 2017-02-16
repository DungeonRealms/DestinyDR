package net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.InfernalAbyss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.Item.ItemType;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.DamageSource;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalLordsGuard extends MeleeWitherSkeleton {

    public boolean died = false;
    @Getter
    protected Map<String, Integer[]> attributes = new HashMap<>();

    public InfernalLordsGuard(World world, int tier) {
        super(world, 4, EnumMonster.LordsGuard, EnumEntityType.HOSTILE_MOB);
        this.setSkeletonType(1);
        this.fireProof = true;
        this.setOnFire(Integer.MAX_VALUE);
        this.getBukkitEntity().setCustomNameVisible(true);
        int level = 40;
        MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, 4, level);
//		this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), lordsguard));
//		EntityStats.setMonsterElite(this, EnumNamedElite.NONE, 4, EnumMonster.LordsGuard, level, true);
        this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "The Infernal Lords Guard");
//		for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
//			p.sendMessage(ChatColor.RED.toString() + "The Infernal Lords Guard" + ChatColor.RESET.toString() + ": " + "I shall protect you my lord.");
//		}
        this.setSize(0.7F, 4.4F);
        this.setSkeletonType(1);
        setArmor(4);
//		System.out.println("Main called: " + world.getWorld().getName());
    }

    /**
     * @return
     */
    private ItemStack getWeapon() {
        return new ItemGenerator().setType(ItemType.SWORD).setTier(ItemTier.TIER_4).setRarity(random.nextDouble() <= 0.75 ? ItemRarity.UNCOMMON : ItemRarity.RARE).generateItem().getItem();
    }

    @Override
    public void setWeapon(int tier) {
        ItemStack weapon = getWeapon();
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        ((LivingEntity) this.getBukkitEntity()).getEquipment().setItemInMainHand(weapon);
    }

    @Override
    public void setArmor(int tier) {
        ItemStack[] armor = getArmor();
        ItemStack weapon = getWeapon();
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor[0]));
        this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor[1]));
        this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor[2]));
        this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(armor[3]));
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        livingEntity.getEquipment().setItemInMainHand(weapon);
        livingEntity.getEquipment().setBoots(armor[0]);
        livingEntity.getEquipment().setLeggings(armor[1]);
        livingEntity.getEquipment().setChestplate(armor[2]);
        livingEntity.getEquipment().setHelmet(armor[3]);
    }

    private ItemStack[] getArmor() {
        return new ItemGenerator().setTier(ItemTier.getByTier(4)).setRarity(random.nextDouble() <= 0.75 ? ItemRarity.UNCOMMON : ItemRarity.RARE).getArmorSet();
    }

//	@Override
//	public EnumDungeonBoss getEnumBoss() {
//		return EnumDungeonBoss.LordsGuard;
//	}

    @Override
    public void onMonsterDeath(Player killer) {
        for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
            p.sendMessage(ChatColor.RED.toString() + "The Infernal Lords Guard" + ChatColor.RESET.toString() + ": " + "I have failed you...");
        }
//		for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
//			p.sendMessage(ChatColor.RED.toString() + "The Infernal Abyss" + ChatColor.RESET.toString() + ": " + "I'll handle this on my own then!");
//		}
//		boss.setLocation(locX, locY, locZ, 1, 1);
//		int maxHP = boss.getBukkitEntity().getMetadata("maxHP").get(0).asInt() / 2;
//		boss.getBukkitEntity().setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHP));
//		boss.isInvulnerable(DamageSource.FALL);
//		boss.finalForm = true;
        super.onMonsterDeath(null);
    }

//	@Override
//	public void onBossAttack(EntityDamageByEntityEvent event) {
//		//LivingEntity en = (LivingEntity) event.getEntity();
//	}

}
