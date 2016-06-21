package net.dungeonrealms.game.world.entities.utils;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.EnumNamedElite;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

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

    public static void setMonsterElite(Entity entity , EnumNamedElite namedElite, int tier, EnumMonster monster, boolean isDungeon) {
        //TODO confirm working for elites of all types
        if (namedElite == EnumNamedElite.NONE) {
            ItemType weaponType;
            switch (monster) {
                case Zombie:
                    weaponType = ItemType.SWORD;
                    break;
                case Bandit:
                case Bandit1:
                    weaponType = ItemType.AXE;
                    break;
                case FireImp:
                case Daemon2:
                    weaponType = ItemType.STAFF;
                    break;
                case Daemon:
                    weaponType = ItemType.POLEARM;
                    break;
                case Skeleton:
                case Skeleton1:
                case Skeleton2:
                    weaponType = ItemType.BOW;
                    break;
                case Silverfish:
                case GreaterAbyssalDemon:
                    weaponType = ItemType.SWORD;
                    break;
                case Tripoli1:
                case Tripoli:
                    weaponType = ItemType.AXE;
                    break;
                case Monk:
                    weaponType = ItemType.POLEARM;
                    break;
                case Lizardman:
                    weaponType = ItemType.POLEARM;
                    break;
                case Undead:
                    weaponType = ItemType.SWORD;
                    break;
                case Blaze:
                    weaponType = ItemType.STAFF;
                    break;
                case Spider1:
                case Spider2:
                    weaponType = ItemType.SWORD;
                    break;
                case Mage:
                    weaponType = ItemType.STAFF;
                    break;
                case Golem:
                    weaponType = ItemType.SWORD;
                    break;
                case Goblin:
                    weaponType = ItemType.AXE;
                    break;
                case Enderman:
                    weaponType = ItemType.SWORD;
                    break;
                default:
                    weaponType = ItemType.getRandomWeapon();
                    break;
            }
            Item.ItemRarity rarity = API.getItemRarity(true);
            if (isDungeon) {
                rarity = Item.ItemRarity.UNIQUE;
            }
            ItemStack[] armor = new ItemGenerator().setRarity(rarity).setTier(ItemTier.getByTier(tier)).getArmorSet();
            ItemStack weapon = new ItemGenerator().setType(weaponType).setRarity(API.getItemRarity(true)).setTier(ItemTier.getByTier(tier)).generateItem().getItem();
            entity.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
            entity.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor[0]));
            entity.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor[1]));
            entity.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor[2]));
            entity.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(armor[3]));
            LivingEntity livingEntity = (LivingEntity) entity.getBukkitEntity();
            livingEntity.getEquipment().setItemInMainHand(weapon);
            livingEntity.getEquipment().setBoots(armor[0]);
            livingEntity.getEquipment().setLeggings(armor[1]);
            livingEntity.getEquipment().setChestplate(armor[2]);
            livingEntity.getEquipment().setHelmet(armor[3]);
        }
        entity.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), HealthHandler.getInstance().getMonsterMaxHPOnSpawn((LivingEntity) entity.getBukkitEntity())));
        HealthHandler.getInstance().setMonsterHPLive((LivingEntity) entity.getBukkitEntity(), HealthHandler.getInstance().getMonsterMaxHPLive((LivingEntity) entity.getBukkitEntity()));
    }
    
    public static void setMonsterRandomStats(Entity entity, int lvl, int tier) {
        int maxHp = HealthHandler.getInstance().getMonsterMaxHPOnSpawn((LivingEntity) entity.getBukkitEntity());
        if (!entity.getBukkitEntity().hasMetadata("dungeon")) {
            switch (tier) {
                case 1:
                    if (maxHp >= 50) {
                        maxHp = 15 + (random.nextInt(25) - 10);
                    }
                    break;
                case 2:
                    if (maxHp > 300) {
                        maxHp -= 100;
                    }
                    break;
                default:
                    break;
            }
        }
        entity.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHp));
        HealthHandler.getInstance().setMonsterHPLive((LivingEntity) entity.getBukkitEntity(), maxHp);
    }

    public static void createDungeonMob(Entity entity, int level, int tier) {
        LivingEntity livingEntity = (LivingEntity) entity.getBukkitEntity();
        ItemStack[] armor = new ItemGenerator().setRarity(Item.ItemRarity.UNIQUE).setTier(ItemTier.getByTier(tier)).getArmorSet();
        ItemStack weapon = livingEntity.getEquipment().getItemInMainHand();
        ItemType type = ItemType.getTypeFromMaterial(weapon.getType());
        weapon = new ItemGenerator().setType(type).setRarity(Item.ItemRarity.UNIQUE).setTier(ItemTier.getByTier(tier)).generateItem().getItem();
        livingEntity.getEquipment().setItemInMainHand(weapon);
        livingEntity.getEquipment().setBoots(armor[0]);
        livingEntity.getEquipment().setLeggings(armor[1]);
        livingEntity.getEquipment().setChestplate(armor[2]);
        int maxHp = HealthHandler.getInstance().getMonsterMaxHPOnSpawn(livingEntity);
        maxHp *= 1.25;
        livingEntity.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHp));
        HealthHandler.getInstance().setMonsterHPLive(livingEntity, maxHp);
    }

	/**
	 * @param entity
	 * @param level
	 * @param tier
	 */
	public static void setBossRandomStats(Entity entity, int level, int tier) {
		entity.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
        entity.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), HealthHandler.getInstance().getMonsterMaxHPOnSpawn((LivingEntity) entity.getBukkitEntity())));
        HealthHandler.getInstance().setMonsterHPLive((LivingEntity) entity.getBukkitEntity(), HealthHandler.getInstance().getMonsterMaxHPLive((LivingEntity) entity.getBukkitEntity()));
	}

}