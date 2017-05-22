package net.dungeonrealms.game.world.entity.util;

import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.dungeons.Dungeon;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster.CustomEntityType;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.PathfinderGoalSelector;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * EntityAPI - Basic Entity utilities.
 * <p>
 * Redone by Kneesnap on April 19th, 2017.
 */
public class EntityAPI {

    private static Random random = ThreadLocalRandom.current();

    @Getter
    //TODO: Prevent memory leaks, on death, on despawn. Every few minutes go through this list and clean up the trash.
    private static Map<DRMonster, AttributeList> entityAttributes = new ConcurrentHashMap<>();

    public static Entity spawnElite(Location loc, EnumNamedElite elite) {
        return spawnElite(loc, elite, elite.getMonster(), elite.getTier(), elite.randomLevel(), null);
    }

    public static Entity spawnElite(Location loc, EnumNamedElite elite, String displayName) {
        return spawnElite(loc, elite, elite.getMonster(), elite.getTier(), elite.randomLevel(), displayName);
    }

    /**
     * Creates an elite without spawning it into the world.
     */
    public static Entity spawnElite(Location loc, EnumNamedElite elite, EnumMonster monster, int tier, int level, String name) {
        name = name != null ? name : monster.getName();
        LivingEntity entity = spawnEntity(loc, monster, elite != null ? elite.getEntity() : monster.getCustomEntity(), tier, level, name);
        ;
        ;

        // For non-Named elites that don't have custom gear.
        if (elite == null && monster != null && !DungeonManager.isDungeon(loc.getWorld())) {
            ItemWeapon weapon = new ItemWeapon();
            weapon.setTier(tier).setRarity(ItemRarity.getRandomRarity(true)).setGlowing(true);
            ItemType type = monster.getWeaponType();

            if (type != null)
                weapon.setType(type);

            // These have an extra special chance.
            if ((monster == EnumMonster.Zombie || monster == EnumMonster.Undead) && random.nextBoolean())
                weapon.setType(ItemType.AXE);

            ItemArmor armor = (ItemArmor) new ItemArmor().setRarity(ItemRarity.getRandomRarity(true)).setTier(tier).setGlowing(true);

            EntityEquipment e = entity.getEquipment();
            e.setItemInMainHand(weapon.generateItem());
            e.setArmorContents(armor.generateArmorSet());
        } else if (elite != null) {
            // Load elite custom gear.
            Map<EquipmentSlot, ItemStack> gear = ItemGenerator.getEliteGear(elite);
            for (EquipmentSlot e : gear.keySet()) {
                ItemStack i = gear.get(e);
                EnchantmentAPI.addGlow(i);
                GameAPI.setItem(entity, e, i);
            }
        }

        Metadata.ELITE.set(entity, true);
        if (elite != null)
            Metadata.NAMED_ELITE.set(entity, elite);

        return entity;
    }

    public static Entity spawnCustomMonster(Location loc, EnumMonster monster, String levelRange, int tier, ItemType weaponType) {
        return spawnCustomMonster(loc, monster, Utils.getRandomFromTier(tier, levelRange), tier, weaponType);
    }

    public static Entity spawnCustomMonster(Location loc, EnumMonster monster, int level, int tier, ItemType weaponType) {
        return spawnCustomMonster(loc, monster, level, tier, weaponType, null);
    }

    /**
     * Spawns a custom monster.
     */
    public static Entity spawnCustomMonster(Location loc, EnumMonster monster, int level, int tier, ItemType weaponType, String customName) {
        LivingEntity e = spawnEntity(loc, monster, monster.getCustomEntity(), tier, level, customName);

        // Register mob element.
        if (!monster.isFriendly() && ThreadLocalRandom.current().nextInt(100) < monster.getElementalChance())
            setMobElement(e, monster.getRandomElement());

        if (monster.isPassive())
            Metadata.PASSIVE.set(e, true);
        return e;
    }

    /**
     * Updates the entity's display name to its friendly display name.
     */
    public static void updateName(Entity entity) {
        if (!Metadata.CUSTOM_NAME.has(entity)) {
            // They don't have a custom name set.
            Bukkit.getLogger().warning(entity.getName() + " has no custom name!");
            return;
        }

        String prefix = "";
        String name = Metadata.CUSTOM_NAME.get(entity).asString();

        // Apply elemental name.
        if (isElemental(entity)) {
            ElementalAttribute ea = getElement(entity);
            String[] splitName = name.split(" ", 2);

            boolean shortName = splitName.length == 1;
            String ePrefix = shortName ? splitName[0] : "";
            String eSuffix = shortName ? "" : " " + splitName[1];

            if (shortName) {
                //Fire Acolyte, Fire Daemon etc.
                name = ea.getColor() + ea.getPrefix() + " " + ePrefix;
            } else {
                name = ea.getColor() + ePrefix + ea.getPrefix() + eSuffix;
            }
        }

        // Apply boss.
        if (isBoss(entity))
            prefix = ChatColor.RED + "" + ChatColor.BOLD;

        // Apply elite.
        if (Metadata.ELITE.get(entity).asBoolean())
            prefix = ChatColor.BOLD + "";

        int tier = Metadata.TIER.get(entity).asInt();
        if (!name.contains(ChatColor.COLOR_CHAR + "") && tier != -1) {
            //Add the tier color to the front?
            prefix = Item.ItemTier.getByTier(tier).getColor() + "";
        }
        entity.setCustomName(prefix + name);
        entity.setCustomNameVisible(true);
    }

    public static boolean isElemental(Entity e) {
        return Metadata.ELEMENT.has(e);
    }

    public static ElementalAttribute getElement(Entity e) {
        return Metadata.ELEMENT.getEnum(e);
    }

    public static void setMobElement(Entity entity, ElementalAttribute ea) {
        Metadata.ELEMENT.set(entity, ea);
        updateName(entity);
    }

    public static void registerMonster(Entity entity, int level, int tier) {
        registerMonster(entity, level, tier, null, null, null);
    }

    /**
     * Adds metadata that identifies this as a custom monster.
     * Sets tier and level, health, etc.
     * Weapon / Armor should only be supplied if you wish to forcefully set the gear, since gear is normally generated in DRMonster's setup.
     * <p>
     * Formerly: setMonsterRandomStats
     */
    public static void registerMonster(Entity entity, int level, int tier, ItemArmor armorSet, ItemWeapon weapon, String name) {
        MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, level);

        LivingEntity le = (LivingEntity) entity;
        HealthHandler.calculateHP(le);
        HealthHandler.setHP(le, HealthHandler.getMaxHP(le));

        if (armorSet != null)
            le.getEquipment().setArmorContents(armorSet.generateArmorSet());

        if (weapon != null)
            le.getEquipment().setItemInMainHand(weapon.generateItem());

        if (name != null && name.length() > 0) {
            Metadata.CUSTOM_NAME.set(entity, name);
        }
        updateName(entity);
    }

    /**
     * Setup the supplied entity as a dungeon boss.
     */
    public static void registerBoss(DungeonBoss boss, int level, int tier) {
        LivingEntity le = boss.getBukkit();
        Metadata.BOSS.set(le, boss.getBossType().name());
        Metadata.CUSTOM_NAME.set(le, boss.getBossType().getName());
        registerMonster(le, level, tier);

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
     * Gets this mob tier.
     */
    public static int getTier(Entity entity) {
        return Metadata.TIER.get(entity).asInt();
    }

    /**
     * Is this an elite?
     */
    public static boolean isElite(Entity ent) {
        return Metadata.ELITE.has(ent);
    }

    /**
     * Is this entity a boss?
     */
    public static boolean isBoss(Entity ent) {
        return Metadata.BOSS.has(ent);
    }

    public static String getCustomName(Entity entity) {
        return Metadata.CUSTOM_NAME.get(entity).asString();
    }

    /**
     * Get an entity's level.
     */
    public static int getLevel(Entity ent) {
        return Metadata.LEVEL.get(ent).asInt();
    }

    /**
     * Is this a DR Monster?
     */
    public static boolean isMonster(Entity monster) {
        return ((CraftEntity) monster).getHandle() instanceof DRMonster;
    }

    /**
     * Gets attributes of the specified entity.
     */
    public static AttributeList getAttributes(Entity e) {
        if (GameAPI.isPlayer(e)) {
            return PlayerWrapper.getWrapper((Player) e).getAttributes();
        } else if (isMonster(e)) {
            return getMonster(e).getAttributes();
        }

        Utils.printTrace();
        Bukkit.getLogger().warning("Could not get attributes from " + e.getName() + "!");
        return new AttributeList();
    }

    /**
     * Returns the supplied bukkit entity as a DRMonster.
     */
    public static DRMonster getMonster(Entity monster) {
        return (DRMonster) ((CraftEntity) monster).getHandle();
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

            if (target != null) {
                ((LinkedHashSet) a.get(target)).clear();
                ((LinkedHashSet) b.get(target)).clear();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static LivingEntity spawnEntity(Location loc, EnumMonster mType, CustomEntityType type, int tier, int level, String displayName) {
        DRMonster monster = null;

        try {
            // Setup monster.
            World nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();
            EntityInsentient entity = type.getClazz().getDeclaredConstructor(World.class).newInstance(nmsWorld);
            if (entity instanceof DRMonster) {
                monster = (DRMonster) entity;
                getEntityAttributes().put(monster, new AttributeList());
                monster.setMonster(mType);
                monster.setupMonster(tier);
                // Add to world.
                monster.getNMS().setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
            }
            nmsWorld.addEntity(monster == null ? entity : monster.getNMS(), SpawnReason.CUSTOM);

            // Setup bukkit data and return.
            LivingEntity le = (LivingEntity) entity.getBukkitEntity();
            le.teleport(loc);
            le.setCollidable(true);

            boolean dungeon = DungeonManager.isDungeon(loc.getWorld());
            ItemWeapon weapon = dungeon ? (ItemWeapon) new ItemWeapon().setTier(tier).setRarity(ItemRarity.UNIQUE) : null;
            ItemArmor armor = dungeon ? (ItemArmor) new ItemArmor().setTier(tier).setRarity(ItemRarity.UNIQUE) : null;

            // Register monster data.
            if (mType != null && !mType.isFriendly()) {
                registerMonster(le, level, tier, armor, weapon, displayName);

                // Mark as dungeon mob.
                if (dungeon) {
                    Metadata.DUNGEON.set(le, true);
                    Dungeon dung = DungeonManager.getDungeon(loc.getWorld());
                    if (dung != null)
                        dung.getTrackedMonsters().put(le, loc);
                }
            }

            if (monster != null)
                calculateAttributes(monster);
            return le;
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("Failed to create " + type.getClazz().getSimpleName());
        }
        return null;
    }

    /**
     * Recalculates a monster's attributes.
     *
     * @param ent
     */
    public static void calculateAttributes(DRMonster m) {
        AttributeList attributes = m.getAttributes();
        attributes.clear();

        ItemStack[] armorSet = m.getBukkit().getEquipment().getArmorContents().clone();
        int tier = EntityAPI.getTier(m.getBukkit());

        // if we have a skull we need to generate a helmet so mob stats are calculated correctly
        // TODO: Verify 3 is the correct slot.
        if (armorSet[3].getType() == Material.SKULL_ITEM && (tier >= 3 || ThreadLocalRandom.current().nextInt(10) <= (6 + tier)))
            armorSet[3] = new ItemArmor().setTier(tier).setRarity(ItemRarity.getRandomRarity(EntityAPI.isElemental(m.getBukkit()))).generateItem();

        attributes.addStats(m.getBukkit().getEquipment().getItemInMainHand());
        for (ItemStack armor : armorSet)
            attributes.addStats(armor);
        attributes.applyStatBonuses();
    }

    public static void showHPBar(DRMonster monster) {
        Entity ent = monster.getBukkit();
        boolean boss = isBoss(ent);
        boolean bold = boss || isElite(ent);
        int barSize = (boss ? 15 : 10) * 2;

        double hpPercentDecimal = HealthHandler.getHPPercent(ent);
        double hpPercent = hpPercentDecimal * 100D;
        hpPercent = Math.max(1, hpPercent);

        String fullBar = new String(new char[barSize]).replace("\0", "|");

        String full = ChatColor.GREEN + (bold ? ChatColor.BOLD + "" : "");
        String empty = ChatColor.DARK_RED + (bold ? ChatColor.BOLD + "" : "");

        // Apply color.
        int greenBars = (int) Math.ceil(hpPercentDecimal * barSize);
        fullBar = full + fullBar.substring(0, greenBars) + empty + fullBar.substring(greenBars);

        // Apply name to entity.
        String[] bar = splitHalfColor(fullBar, bold, String.valueOf(monster.getHP()).length());
        ent.setCustomName("[" + bar[0] + " " + ChatColor.WHITE + monster.getHP() + " " + bar[1] + ChatColor.WHITE + "]");
        ent.setCustomNameVisible(true);
    }

    private static String[] splitHalfColor(String spl, boolean bold, int healthLength) {
        //Need to offset for the health.
        int splitIndex = spl.length() / 2 - healthLength / 2 - 1;

        // Add any color.
        ChatColor lastColor = null;
        String colorChar = ChatColor.WHITE.toString().substring(0, 1);
        for (int i = 0; i < splitIndex; i++) {
            if (spl.substring(i).startsWith(colorChar)) {
                splitIndex += 2;
                ChatColor c = ChatColor.getByChar(spl.charAt(i + 1));
                if (c != null && c != ChatColor.BOLD)
                    lastColor = c;
            }
        }

        return new String[]{spl.substring(0, splitIndex), (lastColor != null ? lastColor + (bold ? ChatColor.BOLD + "" : "") : "") + spl.substring(splitIndex)};
    }
}