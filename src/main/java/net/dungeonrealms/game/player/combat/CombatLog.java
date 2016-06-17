package net.dungeonrealms.game.player.combat;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.NBTUtils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    public ConcurrentMap<UUID, CombatLogger> getCOMBAT_LOGGERS() {
        return COMBAT_LOGGERS;
    }

    private ConcurrentMap<UUID, CombatLogger> COMBAT_LOGGERS = new ConcurrentHashMap<>();

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
                BountifulAPI.sendActionBar(player, ChatColor.RED + "Entering Combat...", 2);
            }

            /*
            Knock player off of horse, if they're tagged in combat.
             */
            if (player.getVehicle() != null && player.getVehicle() instanceof Horse) {
                player.getVehicle().remove();
            }

        }
    }

    public static void removeFromCombat(Player player) {
        if (isInCombat(player)) {
            COMBAT.remove(player);
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                BountifulAPI.sendActionBar(player, ChatColor.GREEN + "Leaving Combat...", 2);
            }
        }
    }

    public static void handleCombatLogger(Player player) {
        final World world = player.getWorld();
        final Location loc = player.getLocation();
        CombatLogger combatLogger = new CombatLogger(player.getUniqueId());
        List<ItemStack> itemsToDrop = new ArrayList<>();
        List<ItemStack> armorToDrop = new ArrayList<>();
        List<ItemStack> itemsToSave = new ArrayList<>();
        List<ItemStack> armorToSave = new ArrayList<>();
        KarmaHandler.EnumPlayerAlignments alignments = API.getGamePlayer(player).getPlayerAlignment();
        int lvl = API.getGamePlayer(player).getLevel();
        if (alignments == null) {
            return;
        }
        Random random = new Random();
        //TODO: Check if this includes Armor.
        for (int i = 0; i <= player.getInventory().getContents().length; i++) {
            if (i > 35) {
                break;
            }
            ItemStack stack = player.getInventory().getItem(i);
            if (i == 0) {
                if (alignments == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                    itemsToDrop.add(stack);
                } else if (alignments == KarmaHandler.EnumPlayerAlignments.NEUTRAL) {
                    if (random.nextInt(99) < 50) {
                        itemsToDrop.add(stack);
                    } else {
                        itemsToSave.add(stack);
                    }
                } else {
                    itemsToSave.add(stack);
                }
            } else {
                itemsToDrop.add(stack);
            }
        }
        ItemStack melonStack = new ItemStack(Material.MELON);
        for (ItemStack stack : player.getEquipment().getArmorContents()) {
            if (alignments == KarmaHandler.EnumPlayerAlignments.NEUTRAL) {
                if (random.nextInt(99) < 25) {
                    armorToDrop.add(stack);
                    armorToSave.add(melonStack);
                } else {
                    armorToSave.add(stack);
                }
            } else if (alignments == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                armorToSave.add(melonStack);
                armorToDrop.add(stack);
            } else {
                armorToSave.add(stack);
            }
        }
        Zombie combatNPC = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
        NBTUtils.nullifyAI(combatNPC);
        combatNPC.getEquipment().setArmorContents(player.getEquipment().getArmorContents());
        combatNPC.getEquipment().setItemInHand(player.getEquipment().getItemInMainHand());
        combatNPC.setCustomName(ChatColor.LIGHT_PURPLE + "[" + lvl + "]" + ChatColor.RED + " " + player.getName());
        combatNPC.setCustomNameVisible(true);
        MetadataUtils.registerEntityMetadata(((CraftEntity) combatNPC).getHandle(), EnumEntityType.HOSTILE_MOB, 4, lvl);
        HealthHandler.getInstance().setMonsterHPLive(combatNPC, HealthHandler.getInstance().getPlayerHPLive(player));
        combatNPC.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), HealthHandler.getInstance().getPlayerMaxHPLive(player)));
        combatNPC.setMetadata("combatlog", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
        combatNPC.setMetadata("uuid", new FixedMetadataValue(DungeonRealms.getInstance(), player.getUniqueId().toString()));
        combatLogger.setArmorToDrop(armorToDrop);
        combatLogger.setItemsToDrop(itemsToDrop);
        combatLogger.setLoggerNPC(combatNPC);
        combatLogger.setPlayerAlignment(alignments);
        combatLogger.setItemsToSave(itemsToSave);
        combatLogger.setArmorToSave(armorToSave);
        CombatLog.getInstance().getCOMBAT_LOGGERS().put(player.getUniqueId(), combatLogger);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            if (CombatLog.getInstance().getCOMBAT_LOGGERS().containsKey(player.getUniqueId())) {
                if (player != null && player.isOnline()) {
                    CombatLog.getInstance().getCOMBAT_LOGGERS().get(player.getUniqueId()).handleTimeOut();
                }
            }
        }, 250L);
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
        }, 0, 20L);
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * @param uuid
     */
    public static void checkCombatLog(UUID uuid) {
        if (CombatLog.getInstance().getCOMBAT_LOGGERS().containsKey(uuid)) {
            CombatLogger combatLogger = CombatLog.getInstance().getCOMBAT_LOGGERS().get(uuid);
            if (combatLogger.getLoggerNPC().isDead()) {
                combatLogger.handleNPCDeath();
            } else {
                HealthHandler.getInstance().setPlayerHPLive(Bukkit.getPlayer(uuid), HealthHandler.getInstance().getMonsterHPLive(combatLogger.getLoggerNPC()));
                combatLogger.handleTimeOut();
            }
        }
        /*if (CombatLog.LOGGER.containsKey(uuid)) {
            Zombie z = CombatLog.LOGGER.get(uuid);
            if (!z.isDead()) {
                Player p = Bukkit.getPlayer(uuid);
                HealthHandler.getInstance().setPlayerHPLive(p, HealthHandler.getInstance().getMonsterHPLive(z));
                z.remove();
            } else {
                //TP TO CYRENN
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, new ArrayList<String>(), false);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.CURRENT_LOCATION, "-367,90,390,0,0", false);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, "", true);
            }
        }*/
    }
}