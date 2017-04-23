package net.dungeonrealms.game.world.entity.util;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster.CustomEntityType;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.PathfinderGoalSelector;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Random;

/**
 * EntityAPI - Basic Entity utilities.
 * 
 * Redone by Kneesnap on April 19th, 2017.
 */
public class EntityAPI {

    public static Random random = new Random();
    
    public static void registerMonster(Entity entity, int level, int tier) {
    	registerMonster(entity, level, tier, null, null);
    }
    
    public static Entity spawnElite(Location loc, EnumNamedElite elite, int level) {
    	return spawnElite(loc, elite, elite.getMonster(), elite.getTier(), level, null, false);
    }
    
    public static net.minecraft.server.v1_9_R2.Entity createElite(Location loc, EnumNamedElite elite, EnumMonster monster, int tier, int level, String name, boolean dungeon) {
    	EntityLiving entity = (EntityLiving) createEntity(elite != null ? elite.getEntity() : monster.getCustomEntity());
    	
    	ItemWeapon weapon = null;
    	ItemArmor armor = null;
    	
    	// For non-Named elites that don't have custom gear.
    	if (elite == null && monster != null) {
    		weapon = new ItemWeapon();
    		weapon.setTier(tier);
    		weapon.setRarity(dungeon ? ItemRarity.UNIQUE : ItemRarity.getRandomRarity(true));
    		ItemType type = monster.getWeaponType();
    		
    		if (type != null)
    			weapon.setType(type);
    		
    		// These have an extra special chance.
    		if ((monster == EnumMonster.Zombie || monster == EnumMonster.Undead) && random.nextBoolean())
    			weapon.setType(ItemType.AXE);
    		
    		armor = (ItemArmor) new ItemArmor().setRarity(dungeon ? ItemRarity.UNIQUE : ItemRarity.getRandomRarity(true))
    				.setTier(tier).setGlowing(true);
    	} else if (elite != null) {
    		// Load elite custom gear.
    		for (EquipmentSlot slot : EquipmentSlot.values()) {
            	if (slot == EquipmentSlot.OFF_HAND) // Skip offhand.
            		continue;
            	
            	ItemStack item = ItemGenerator.getNamedItem(elite.getTemplateStarter() + Utils.capitalize(slot.name()));
            	if (item == null || item.getType() == Material.AIR)
            		continue;
            	
            	EnchantmentAPI.addGlow(item);
            	GameAPI.setItem((LivingEntity) entity.getBukkitEntity(), slot, item);
            }
    	}
    	
    	registerMonster(entity.getBukkitEntity(), level, tier, armor, weapon);
    	
    	Metadata.ELITE.set(entity.getBukkitEntity(), true);
    	String cName = GameAPI.getTierColor(tier) + "" + ChatColor.BOLD + (name != null ? name : monster.getName());
        Metadata.CUSTOM_NAME.set(entity.getBukkitEntity(), cName);
        entity.setCustomName(cName);
        return entity;
    }
    
    public static Entity spawnElite(Location loc, EnumNamedElite elite, EnumMonster monster, int tier, int level, String name, boolean dungeon) {
    	net.minecraft.server.v1_9_R2.Entity e = createElite(loc, elite, monster, tier, level, name, dungeon);
    	e.getWorld().addEntity(e);
    	e.getBukkitEntity().teleport(loc);
    	return e.getBukkitEntity();
    }
    
    public static Entity spawnCustomMonster(Location loc, EnumMonster monster, String levelRange, int tier, ItemType weaponType) {
    	return spawnCustomMonster(loc, monster, Utils.getRandomFromTier(tier, levelRange), tier, weaponType);
    }
    
    public static Entity spawnCustomMonster(Location loc, EnumMonster monster, int level, int tier, ItemType weaponType) {
    	return spawnCustomMonster(loc, monster, level, tier, weaponType, null);
    }
    
    public static net.minecraft.server.v1_9_R2.Entity createCustomMonster(Location loc, EnumMonster monster, int level, int tier, ItemType weaponType, String customName) {
    	net.minecraft.server.v1_9_R2.Entity entity = createEntity(monster.getCustomEntity());
    	
    	// Generate Weapon.
    	ItemWeapon weapon = new ItemWeapon();
    	if (weaponType != null)
    		weapon.setType(weaponType);
    	weapon.setTier(tier);
    	
    	if (entity instanceof LivingEntity)
    		GameAPI.setItem((LivingEntity)entity, EquipmentSlot.HAND, weapon.generateItem());
    	
    	// Non friendly.
    	if (!monster.isFriendly()) {
    		EntityAPI.registerMonster(entity.getBukkitEntity(), level, tier);
    		
    		// Register Element.
    		if (new Random().nextInt(100) < monster.getElementalChance())
    			setMobElement(entity.getBukkitEntity(), monster.getRandomElement());
    		
    		setName(entity.getBukkitEntity(), monster, tier, level, customName);
    	}
    	
    	if (monster.isPassive())
    		Metadata.PASSIVE.set(entity.getBukkitEntity(), true);
    	return entity;
    }
    
    public static Entity spawnCustomMonster(Location loc, EnumMonster monster, int level, int tier, ItemType weaponType, String customName) {
    	net.minecraft.server.v1_9_R2.Entity e = createCustomMonster(loc, monster, level, tier, weaponType, customName);
    	e.getWorld().addEntity(e);
    	e.getBukkitEntity().teleport(loc);
    	return e.getBukkitEntity();
    }
    
    private static void setName(Entity entity, EnumMonster monster, int tier, int level, String customName) {
    	String mobName = Metadata.CUSTOM_NAME.has(entity) ? Metadata.CUSTOM_NAME.get(entity).asString() : monster.getName();
		String finalName = GameAPI.getTierColor(tier) + (customName != null ? ChatColor.BOLD + customName : mobName);
		entity.setCustomName(ChatColor.AQUA + "[Lvl. " + level + "] " + finalName);
		Metadata.CUSTOM_NAME.set(entity, finalName);
    }
    
    public static boolean isElemental(Entity e) {
    	return Metadata.ELEMENT.has(e);
    }
    
    public static ElementalAttribute getElement(Entity e) {
    	return Metadata.ELEMENT.getEnum(e);
    }
    
    public static void setMobElement(Entity entity, ElementalAttribute ea) {
    	Metadata.ELEMENT.set(entity, ea);
        String name = entity.getCustomName();
        String[] splitName = name.split(" ", 2);
        
        if (ea == ElementalAttribute.PURE || splitName.length == 1)
        	name = ea.getColor() + ea.getPrefix() + " " + name;
        else
        	name = ea.getColor() + splitName[0] + " " + ea.getPrefix() + " " + splitName[1];
        
        entity.setCustomName(name);
        Metadata.CUSTOM_NAME.set(entity, name);
    }
     
    /**
     * Adds metadata that identifies this as a custom monster.
     * Sets tier and level, health, etc.
     * 
     * Formerly: setMonsterRandomStats
     */
    public static void registerMonster(Entity entity, int level, int tier, ItemArmor armorSet, ItemWeapon weapon) {
    	MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, level);
    	
    	LivingEntity le = (LivingEntity) entity;
        int maxHp = HealthHandler.getMonsterMaxHPOnSpawn(le);
        HealthHandler.setMaxHP(entity, maxHp);
        HealthHandler.setMonsterHP(le, maxHp);
        
        if (armorSet != null)
        	le.getEquipment().setArmorContents(armorSet.generateArmorSet());
        
        if (weapon != null)
        	le.getEquipment().setItemInMainHand(weapon.generateItem());
    }

    /**
     * Setup the supplied entity as a dungeon mob.
     */
    public static void makeDungeonMob(Entity entity, EnumMonster monster, int level, int tier) {
        registerMonster(entity, level, tier, (ItemArmor) new ItemArmor().setTier(tier).setRarity(ItemRarity.UNIQUE),
        		(ItemWeapon) new ItemWeapon().setTier(tier).setRarity(ItemRarity.UNIQUE));
        setName(entity, monster, tier, level, null);
    }
    
    /**
     * Setup the supplied entity as a dungeon boss.
     */
    public static void registerBoss(Entity entity, int level, int tier) {
    	LivingEntity le = (LivingEntity) entity;
    	registerMonster(entity, level, tier);
    	Metadata.BOSS.set(entity, true);
    	
    	for (ItemStack item : le.getEquipment().getArmorContents())
    		if (item != null && item.getType() != Material.AIR)
    			EnchantmentAPI.addGlow(item);
    	le.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
    }
    
    /**
     * Get all nearby entities within a certain radius to untarget another entity.
     *
     * @param entToUntarget
     * @param radius
     */
    public static void untargetEntity(LivingEntity entToUntarget, int radius) {
        entToUntarget.getNearbyEntities(radius, radius, radius).stream().forEach(ent -> {
            // Has to be targettable.
        	if (!(ent instanceof Creature))
            	return;
            // Make sure the target was actually who we said to untarget.
            if (((Creature) ent).getTarget() == null || !((Creature) ent).getTarget().equals(entToUntarget))
            	return;
            //Untarget
            ((Creature) ent).setTarget(null);
        });
    }
    
    /**
     * Is this entity a boss?
     */
    public static boolean isBoss(Entity ent) {
    	return ent.hasMetadata("boss") && ent.getMetadata("boss").get(0).asBoolean();
    }
    
    /**
     * Get an entity's level.
     */
    public static int getLevel(Entity ent) {
    	return ent.hasMetadata("level") ? ent.getMetadata("level").get(0).asInt() : 1;
    }
    
    /**
     * Returns the supplied bukkit entity as a DRMonster.
     */
    public static DRMonster getMonster(Entity monster) {
    	return (DRMonster) ((CraftEntity)monster).getHandle();
    }
    
    @SuppressWarnings("rawtypes")
	public static void clearAI(PathfinderGoalSelector goal, PathfinderGoalSelector target) {
    	try {
            Field a = PathfinderGoalSelector.class.getDeclaredField("b");
            Field b = PathfinderGoalSelector.class.getDeclaredField("c");
            a.setAccessible(true);
            b.setAccessible(true);
            ((LinkedHashSet) a.get(goal)).clear();
            ((LinkedHashSet) b.get(goal)).clear();

            ((LinkedHashSet) a.get(target)).clear();
            ((LinkedHashSet) b.get(target)).clear();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    public static net.minecraft.server.v1_9_R2.Entity createEntity(CustomEntityType type) {
    	
    }
}