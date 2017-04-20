package net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.base.DRGhast;
import net.dungeonrealms.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.InfernalAbyss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.EnumItemSlot;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalGhast extends DRGhast implements DungeonBoss {

    private InfernalAbyss boss;
    @Getter
    protected Map<String, Integer[]> attributes = new HashMap<>();

    /**
     * @param infernalAbyss
     */
    public InfernalGhast(InfernalAbyss infernalAbyss) {
        super(infernalAbyss.getWorld());
        this.boss = infernalAbyss;
        this.createEntity(100);
    }

    public void init(int hp) {
        this.getBukkitEntity().setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), hp));
        this.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), hp));
        maxHP = hp;
        HealthHandler.getInstance().setMonsterHPLive((LivingEntity) this.getBukkitEntity(), hp);
        this.getBukkitEntity().setPassenger(boss.getBukkitEntity());
        DamageAPI.setArmorBonus(getBukkitEntity(), 50);
        this.getBukkitEntity().setPassenger(boss.getBukkitEntity());
    }

    @Override
    public EnumDungeonBoss getEnumBoss() {
        return EnumDungeonBoss.InfernalGhast;
    }

    @Override
    public void onBossDeath() {
    }

    public void setArmor(ItemStack[] armor, ItemStack weapon) {
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

    private int maxHP = 0;

    @Override
    public void onBossAttack(EntityDamageByEntityEvent event) {
        LivingEntity en = (LivingEntity) event.getEntity();
        double totalHP = HealthHandler.getInstance().getMonsterMaxHPLive(en);
        if (totalHP < 10000) {
            totalHP = maxHP;
        }
        totalHP *= 0.5;
        double currHP = HealthHandler.getInstance().getMonsterHPLive(en);

        if (currHP <= totalHP) {
            this.getBukkitEntity().eject();
            this.getBukkitEntity().setPassenger(null);
            boss.doFinalForm(currHP);
            this.die();
        }
    }

	@Override
	public int getGemDrop() {
		return 0;
	}

	@Override
	public int getXPDrop() {
		return 0;
	}

	@Override
	public String[] getItems() {
		return null;
	}

	@Override
	public void addKillStat(PlayerWrapper gp) {
		
	}
}
