package net.dungeonrealms.game.player.combat;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.NBTUtils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nick on 8/29/2015.
 */
public class CombatLog implements GenericMechanic {

    private static CombatLog instance = null;

    public static CombatLog getInstance() {
        if (instance == null) {
            instance = new CombatLog();
        }
        return instance;

    }

    public static ConcurrentHashMap<Player, Integer> COMBAT = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<UUID, Zombie> LOGGER = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<UUID, Inventory> LOGGER_INVENTORY = new ConcurrentHashMap<>();

    public static boolean isInCombat(Player player) {
        return COMBAT.containsKey(player);
    }

    public static void updateCombat(Player player) {
        if (isInCombat(player)) {
            COMBAT.put(player, 10);
        }
    }

    public static void addToCombat(Player player) {
        if (!isInCombat(player)) {
            COMBAT.put(player, 10);
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                player.sendMessage(ChatColor.RED + "Entering Combat");
            }
        }
    }

    public static void removeFromCombat(Player player) {
        if (isInCombat(player)) {
            COMBAT.remove(player);
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                player.sendMessage(ChatColor.RED + "Leaving Combat");
            }
        }
    }

    public static void handleCombatLogger(Player p) {
        World world = p.getWorld();
        Location loc = p.getLocation();
        Zombie z = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
        NBTUtils.nullifyAI(z);
        z.getEquipment().setArmorContents(p.getEquipment().getArmorContents());
        z.getEquipment().setItemInHand(p.getItemInHand());
        if (p.getEquipment().getHelmet() == null || p.getEquipment().getHelmet().getType() == Material.AIR) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(p.getName());
            meta.setDisplayName(p.getName());
            skull.setItemMeta(meta);
            z.getEquipment().setHelmet(skull);
        }
        int lvl = API.getGamePlayer(p).getLevel();
        z.setCustomName(ChatColor.LIGHT_PURPLE + "[" + lvl + "]" + ChatColor.RED + " " + p.getName());
        z.setCustomNameVisible(true);
        MetadataUtils.registerEntityMetadata(((CraftEntity) z).getHandle(), EnumEntityType.HOSTILE_MOB, 4, lvl);


        HealthHandler.getInstance().setMonsterHPLive(z, HealthHandler.getInstance().getPlayerHPLive(p));
        z.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), HealthHandler.getInstance().getPlayerMaxHPLive(p)));
        z.setMetadata("combatlog", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
        z.setMetadata("uuid", new FixedMetadataValue(DungeonRealms.getInstance(), p.getUniqueId().toString()));
        LOGGER.put(p.getUniqueId(), z);
        LOGGER_INVENTORY.put(p.getUniqueId(), p.getInventory());
        //for(ItemStack stack : p.getInventory()){

        //}
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            LOGGER.get(p.getUniqueId()).remove();
            LOGGER.remove(p.getUniqueId());
            LOGGER_INVENTORY.remove(p.getUniqueId());
        }, 20 * 10);

    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (Map.Entry<Player, Integer> e : COMBAT.entrySet()) {
                if (e.getValue() <= 0) {
                    removeFromCombat(e.getKey());
                    HealthHandler.getInstance().recalculateHPAfterCombat(e.getKey());
                } else {
                    COMBAT.put(e.getKey(), (e.getValue() - 1));
                }
            }
        }, 0, 20l);
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * @param uuid
     */
    public static void checkCombatLog(UUID uuid) {
        if (CombatLog.LOGGER.containsKey(uuid)) {
            Zombie z = CombatLog.LOGGER.get(uuid);
            if (!z.isDead()) {
                Player p = Bukkit.getPlayer(uuid);
                HealthHandler.getInstance().setPlayerHPLive(p, HealthHandler.getInstance().getMonsterHPLive(z));
                z.remove();
            } else {
                //TP TO CYRENN
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, "", true);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.CURRENT_LOCATION, "-367,90,390,0,0", true);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, "", true);
            }
        }
    }
}