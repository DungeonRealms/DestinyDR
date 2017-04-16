package net.dungeonrealms.game.world.entity.util;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EnumItemSlot;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Created by Chase on Sep 18, 2015
 */
public class EntityStats {

    public static Random random = new Random();

    public static class Stats {
        public int def;
        public int hp;
        public int atk;
        public int spd;

        public Stats(int def, int hp, int atk, int spd) {
            this.def = def;
            this.hp = hp;
            this.atk = atk;
            this.spd = spd;
        }

        public static Stats getRandomStats(int lvl, int tier) {
            int lvldef;
            int lvlatk;
            int lvlhp;
            int lvlspd;
            switch (tier) {
                case 1:
                    lvldef = (lvl + 5) + (random.nextInt(5) - 3);
                    lvlhp = Math.abs((lvl * 2) + (random.nextInt(30) - 15));
                    lvlatk = (lvl + 5) + (random.nextInt(5) - 3);
                    lvlspd = (lvl + 5) + (random.nextInt(5) - 3);
                    break;
                case 2:
                    lvldef = (lvl + 20) + (random.nextInt(20) - 10);
                    lvlhp = Math.abs((lvl * 5) + (random.nextInt(50) - 35));
                    lvlatk = (lvl + 20) + (random.nextInt(20) - 10);
                    lvlspd = (lvl + 20) + (random.nextInt(20) - 10);
                    break;
                case 3:
                    lvldef = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlhp = Math.abs((lvl * 10) + (random.nextInt(75) - 50));
                    lvlatk = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlspd = (lvl + 40) + (random.nextInt(35) - 20);
                    break;
                case 4:
                    lvldef = (lvl + 60) + (random.nextInt(55) - 35);
                    lvlhp = Math.abs((lvl * 20) + (random.nextInt(100) - 70));
                    lvlatk = (lvl + 60) + (random.nextInt(55) - 35);
                    lvlspd = (lvl + 60) + (random.nextInt(55) - 35);
                    break;
                case 5:
                    lvldef = (lvl + 85) + (random.nextInt(80) - 50);
                    lvlhp = Math.abs((lvl * 50) + (random.nextInt(150) - 100));
                    lvlatk = (lvl + 85) + (random.nextInt(80) - 50);
                    lvlspd = (lvl + 85) + (random.nextInt(80) - 50);
                    break;
                default:
                    lvldef = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlhp = Math.abs((lvl * 50) + (random.nextInt(75) - 50));
                    lvlatk = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlspd = (lvl + 40) + (random.nextInt(35) - 20);
                    break;
            }
            return new Stats(lvldef, lvlhp, lvlatk, lvlspd);
        }
    }

    public static Stats getMonsterStats(Entity entity) {
        int hp = entity.getBukkitEntity().getMetadata("maxHP").get(0).asInt();
        int def = entity.getBukkitEntity().getMetadata("def").get(0).asInt();
        int spd = entity.getBukkitEntity().getMetadata("spd").get(0).asInt();
        int atk = entity.getBukkitEntity().getMetadata("attack").get(0).asInt();
        return new Stats(def, hp, atk, spd);
    }

    public static void setMonsterElite(Entity entity, EnumNamedElite namedElite, int tier, EnumMonster monster, int lvl, boolean isDungeon) {
        //TODO confirm working for elites of all types
        if (namedElite == EnumNamedElite.NONE) {
        	ItemTier iTier = ItemTier.getByTier(tier);
            ItemWeapon weapon = new ItemWeapon();
            weapon.setTier(iTier).setRarity(isDungeon ? ItemRarity.UNIQUE : ItemRarity.getRandomRarity(true));
            
            //TODO: Move to enum.
            switch (monster) {
                case Zombie:
                case LordsGuard:
                	weapon.setType(random.nextBoolean() ? ItemType.SWORD : ItemType.AXE);
                    break;
                
                // SWORD //
                case Golem:
                case Spider1:
                case Spider2:
                case Undead:
                case Enderman:
                case Silverfish:
                case GreaterAbyssalDemon:
                	weapon.setType(ItemType.SWORD);
                	break;
                	
                // AXE //
                case Goblin:
                case Tripoli1:
                case Tripoli:
                case Bandit:
                case Bandit1:
                case PassiveBandit:
                	weapon.setType(ItemType.AXE);
                    break;
                
                // STAFF //
                case Blaze:
                case Mage:
                case FireImp:
                case StaffZombie:
                case Daemon2:
                	weapon.setType(ItemType.STAFF);
                    break;
                
                // POLEARM //
                case Monk:
                case Lizardman:
                case Daemon:
                	weapon.setType(ItemType.POLEARM);
                    break;
                    
                // BOW //
                case Skeleton:
                case Skeleton1:
                case Skeleton2:
                case PassiveSkeleton1:
                	weapon.setType(ItemType.BOW);
                    break;
            }
            
            
            ItemArmor a = (ItemArmor) new ItemArmor().setRarity(isDungeon ? ItemRarity.UNIQUE : ItemRarity.getRandomRarity(true)).setTier(iTier);
            ItemStack[] armor = a.generateArmorSet();
            ItemStack weaponItem = weapon.generateItem();
            
            // Apply glow.
            for (ItemStack i : armor)
                if (i != null && i.getType() != Material.AIR)
                	EnchantmentAPI.addGlow(i);
            
            EnchantmentAPI.addGlow(weaponItem);
            
            //Actually keep my gear?
            if (monster != EnumMonster.LordsGuard) {
                LivingEntity livingEntity = (LivingEntity) entity.getBukkitEntity();
                livingEntity.getEquipment().setArmorContents(armor);
                livingEntity.getEquipment().setItemInMainHand(weaponItem);
            }
        }
        ((LivingEntity) entity.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, lvl);
        entity.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), HealthHandler.getMonsterMaxHPOnSpawn((LivingEntity) entity.getBukkitEntity())));
        HealthHandler.setMonsterHP((LivingEntity) entity.getBukkitEntity(), HealthHandler.getMonsterMaxHP((LivingEntity) entity.getBukkitEntity()));
    }

    public static void setMonsterRandomStats(Entity entity, int lvl, int tier) {
        MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, lvl);
        int maxHp = HealthHandler.getMonsterMaxHPOnSpawn((LivingEntity) entity.getBukkitEntity());
        entity.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHp));
        HealthHandler.setMonsterHP((LivingEntity) entity.getBukkitEntity(), maxHp);
    }

    public static void createDungeonMob(Entity entity, int level, int tier) {
        LivingEntity livingEntity = (LivingEntity) entity.getBukkitEntity();
        ItemTier iTier = ItemTier.getByTier(tier);
        ItemArmor a = (ItemArmor) new ItemArmor().setRarity(ItemRarity.UNIQUE).setTier(iTier);
        ItemStack[] armor = a.generateArmorSet();
        ItemStack weapon = new ItemWeapon().setTier(iTier).setRarity(ItemRarity.UNIQUE).generateItem();
        entity.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        entity.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor[0]));
        entity.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor[1]));
        entity.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor[2]));
        livingEntity.getEquipment().setItemInMainHand(weapon);
        livingEntity.getEquipment().setBoots(armor[0]);
        livingEntity.getEquipment().setLeggings(armor[1]);
        livingEntity.getEquipment().setChestplate(armor[2]);
        MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, level);
        int maxHp = HealthHandler.getMonsterMaxHPOnSpawn(livingEntity);
        livingEntity.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHp));
        HealthHandler.setMonsterHP(livingEntity, maxHp);
    }

    /**
     * @param entity
     * @param level
     * @param tier
     */
    public static void setBossRandomStats(Entity entity, int level, int tier) {
        final CraftEntity bukkitEntity = entity.getBukkitEntity();
        bukkitEntity.setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
        bukkitEntity.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), HealthHandler.getMonsterMaxHPOnSpawn((LivingEntity) bukkitEntity)));
        bukkitEntity.setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), tier));
        bukkitEntity.setMetadata("level", new FixedMetadataValue(DungeonRealms.getInstance(), level));
        LivingEntity ent = (LivingEntity) bukkitEntity;
        for (ItemStack i : ent.getEquipment().getArmorContents()) {
            if (i != null && i.getType() != Material.AIR && bukkitEntity != null) {
                EnchantmentAPI.addGlow(i);
            }
        }
        if (ent.getEquipment().getItemInMainHand() != null && ent.getEquipment().getItemInMainHand().getType() != Material.AIR)
            EnchantmentAPI.addGlow(ent.getEquipment().getItemInMainHand());

        ((LivingEntity) bukkitEntity).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, true));
        HealthHandler.setMonsterHP(ent, HealthHandler.getMonsterMaxHP(ent));
    }

}