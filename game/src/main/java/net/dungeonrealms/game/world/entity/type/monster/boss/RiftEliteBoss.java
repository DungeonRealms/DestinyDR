package net.dungeonrealms.game.world.entity.type.monster.boss;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemArmorShield;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ItemWeaponMelee;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.base.DRGiant;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalGhast;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.MadBanditPyromancer;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeGiant;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.RangedGiant;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.StaffGiant;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftHumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Rar349 6/8/2017
 */
public class RiftEliteBoss extends StaffGiant implements DungeonBoss {

	private int ourTier;

    public RiftEliteBoss(World world) {
        super(world);
        this.fireProof = true;
    }


    @Override
    public void onBossAttacked(Player player) {

    }

    public void setOurTier(int newTier) {
        this.ourTier = newTier;
        super.setTier(newTier);
    }

    @Override
    public BossType getBossType() {
        return BossType.RiftEliteBoss;
    }

    @Override
    public int getTier() {
        return ourTier;
    }

    @Override
    public EnumMonster getEnum() {
        return EnumMonster.RiftElite;
    }

    @Override
    public void setArmor() {
        ItemArmor armor = (ItemArmor) new ItemArmor().setRarity(Item.ItemRarity.RARE).setTier(ourTier).setGlowing(true);
        ItemStack[] gear = armor.generateArmorSet();
        getBukkit().getEquipment().setArmorContents(gear);
        getBukkit().getEquipment().setItemInMainHand(new ItemArmorShield().setTier(ourTier).setRarity(Item.ItemRarity.getRandomRarity(true)).setGlowing(true).generateItem());
        getBukkit().getEquipment().setItemInOffHand(new ItemArmorShield().setTier(ourTier).setRarity(Item.ItemRarity.getRandomRarity(true)).setGlowing(true).generateItem());
    }


    @Override
    public void collide(Entity e) {}

    @Override
    public void a(EntityLiving entityLiving, float v) {
        //Don't shoot. We are just extending staff giant so that he doesn't path right on top of us. He needs to target us but doesn't need to attack us.
    }

    public Player getTarget() {
        try {
            EntityLiving living = getGoalTarget();
            CraftHumanEntity humanEnt = ((EntityHuman) living).getBukkitEntity();
            return (Player) humanEnt;
        } catch(Exception e) {
            return null;
        }
    }
}