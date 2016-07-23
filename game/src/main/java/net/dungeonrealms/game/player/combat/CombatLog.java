package net.dungeonrealms.game.player.combat;


import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.NBTUtils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.MeleeMobs.MeleeZombie;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.minecraft.server.v1_9_R2.DataWatcherObject;
import net.minecraft.server.v1_9_R2.DataWatcherRegistry;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
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
        if (!isInCombat(player) && !GameAPI.getGamePlayer(player).isInvulnerable()) {
            COMBAT.put(player, 10);
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                TitleAPI.sendActionBar(player, ChatColor.RED.toString() + ChatColor.BOLD + "Entering Combat", 4 * 20);
            }

            /*
            Knock player off of horse, if they're tagged in combat.
             */
            if (player.getVehicle() != null) {
                if (EntityAPI.hasMountOut(player.getUniqueId())) {
                    net.minecraft.server.v1_9_R2.Entity mount = Entities.PLAYER_MOUNTS.get(player.getUniqueId());
                    player.eject();
                    mount.getBukkitEntity().remove();
                    EntityAPI.removePlayerMountList(player.getUniqueId());
                } else {
                    player.eject();
                    player.getVehicle().remove();
                }
                player.sendMessage(ChatColor.RED + "You have been dismounted as you have taken damage!");
            }

        }
    }

    public static void removeFromCombat(Player player) {
        if (isInCombat(player)) {
            COMBAT.remove(player);
            //Removes all arrows from player.
            ((CraftPlayer) player).getHandle().getDataWatcher().set(new DataWatcherObject<>(9, DataWatcherRegistry.b), 0);
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                TitleAPI.sendActionBar(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "Leaving Combat", 4 * 20);
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
        KarmaHandler.EnumPlayerAlignments alignments = GameAPI.getGamePlayer(player).getPlayerAlignment();
        int lvl = GameAPI.getGamePlayer(player).getLevel();
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

        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.IS_COMBAT_LOGGED, true, true);

        MeleeZombie combatNMSNPC = new MeleeZombie(((CraftWorld) world).getHandle());
        combatNMSNPC.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ((CraftWorld) world).getHandle().addEntity(combatNMSNPC, CreatureSpawnEvent.SpawnReason.CUSTOM);
        Zombie combatNPC = (Zombie) combatNMSNPC.getBukkitEntity();
        NBTUtils.nullifyAI(combatNPC);
        combatNPC.getEquipment().setArmorContents(player.getEquipment().getArmorContents());
        combatNPC.getEquipment().setItemInMainHand(player.getEquipment().getItemInMainHand());
        combatNPC.getEquipment().setItemInOffHand(player.getEquipment().getItemInOffHand());
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
                CombatLog.getInstance().getCOMBAT_LOGGERS().get(player.getUniqueId()).handleTimeOut();
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

            GameAPI.submitAsyncCallback(() -> {
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_COMBAT_LOGGED, false, true);
                return true;
            }, null);
        }
    }
}