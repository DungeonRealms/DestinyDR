package net.dungeonrealms.game.player.combat;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.CombatItem;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.NBTUtils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeZombie;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.minecraft.server.v1_9_R2.DataWatcherObject;
import net.minecraft.server.v1_9_R2.DataWatcherRegistry;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Nick on 8/29/2015.
 */
public class CombatLog implements GenericMechanic {

	@Getter
    private static CombatLog instance = new CombatLog();

    public static ConcurrentHashMap<Player, Integer> COMBAT = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Player, Integer> PVP_COMBAT = new ConcurrentHashMap<>();

    public ConcurrentMap<UUID, CombatLogger> getCOMBAT_LOGGERS() {
        return COMBAT_LOGGERS;
    }

    private ConcurrentMap<UUID, CombatLogger> COMBAT_LOGGERS = new ConcurrentHashMap<>();

    public static boolean isInCombat(Player player) {
        return COMBAT.containsKey(player) && !DungeonRealms.getInstance().isAlmostRestarting();
    }

    // PVP COMBAT

    /**
     * Handle a player leaving whilst in combat
     *
     * @param player The player
     */
    public void handleCombatLog(Player player) {
        if (inPVP(player)) {
            Bukkit.getLogger().info("Handling combat log for " + player.getName());
            KarmaHandler.EnumPlayerAlignments alignments = PlayerWrapper.getPlayerWrapper(player).getAlignment();
            switch (alignments) {
                case LAWFUL:
                    ItemStack storedItem = null;
                    // Keep the item a player has in his offhand & damage it

                    ItemStack firstItem = player.getInventory().getItem(0);
                    if (firstItem != null && firstItem.getType() != Material.AIR) {
                        storedItem = firstItem;
                        this.damageAndReturn(player, storedItem, null);
                    }
                    // Drop all items except for storedItem
                    for (ItemStack itemStack : player.getInventory().getContents()) {
                        if (itemStack != null) {
                            // Don't drop the journal/realm star
                            if (itemStack.getType() != Material.WRITTEN_BOOK && itemStack.getType() != Material.NETHER_STAR) {
                                // We don't want to drop a pickaxe/fishing rod
                                if (!ItemManager.isItemSoulbound(itemStack) && !ProfessionItem.isProfessionItem(itemStack)) {
                                    // We don't want to drop the storedItem
                                    if (!itemStack.equals(storedItem)) {
                                        player.getInventory().remove(itemStack);
                                        Bukkit.getLogger().info("Dropping item " + itemStack);
                                        player.getWorld().dropItem(player.getLocation(), itemStack);
                                    }
                                }
                            }
                        }
                    }
                    break;
                case NEUTRAL:
                case CHAOTIC:
                    // Just drop all that shit
                    for (ItemStack itemStack : player.getInventory().getContents()) {
                        if (itemStack != null) {
                            if (itemStack.getType() != Material.WRITTEN_BOOK && itemStack.getType() != Material.NETHER_STAR) {
                                player.getInventory().remove(itemStack);
                                if (!ItemManager.isItemSoulbound(itemStack)) {
                                    player.getWorld().dropItem(player.getLocation(), itemStack);
                                    Bukkit.getLogger().info("1 Dropping item " + itemStack);
                                }
                            }
                        }
                    }
                    player.getInventory().clear();
                    player.getEquipment().clear();
                    break;
                default:
                    break;
            }
        }
        GameAPI.teleport(player, TeleportLocation.CYRENNICA.getLocation());
    }

    public void damageAndReturn(Player player, ItemStack itemStack, List<ItemStack> list) {
        if (CombatItem.isCombatItem(itemStack)) {
            // Damage by 30% of current durability
        	CombatItem ci = (CombatItem)PersistentItem.constructItem(itemStack);
        	ci.damageItem(player, (int) (ci.getDurability() / 3.33333D));
            if (list != null)
                list.add(ci.generateItem());
        }
    }

    /**
     * Update a player's PVP timer
     *
     * @param player The player
     */
    public static void updatePVP(Player player) {
        if (inPVP(player)) {
            PVP_COMBAT.put(player, 10);
        } else {
        	addToPVP(player);
        }
    }

    /**
     * Add a player to PVP
     *
     * @param player The player
     */
    public static void addToPVP(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null || !wrapper.isVulnerable() || inPVP(player) || DuelingMechanics.isDueling(player.getUniqueId()))
        	return;

        PVP_COMBAT.put(player, 10);
        TitleAPI.sendActionBar(player, ChatColor.RED.toString() + ChatColor.BOLD + "ENTERING PVP COMBAT", 4 * 20);

        /*
         Knock player off of horse, if they're tagged in combat.
         */
        if (player.getVehicle() != null) {
        	MountUtils.removeMount(player);
        	if (player.getVehicle() != null)
        		player.getVehicle().remove();
        	player.sendMessage(ChatColor.RED + "You have been dismounted as you have taken damage!");
        }
    }

    /**
     * Remove a player from PVP
     *
     * @param player The player
     */
    public static void removeFromPVP(Player player) {
        if (!inPVP(player))
        	return;
        PVP_COMBAT.remove(player);
        //Removes all arrows from player.
        ((CraftPlayer) player).getHandle().getDataWatcher().set(new DataWatcherObject<>(9, DataWatcherRegistry.b), 0);
        TitleAPI.sendActionBar(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "LEAVING PVP COMBAT", 4 * 20);
    }

    /**
     * Check if a player is in PVP
     *
     * @param player The player
     * @return Boolean
     */
    public static boolean inPVP(Player player) {
        return PVP_COMBAT.containsKey(player) && !DungeonRealms.getInstance().isAlmostRestarting();
    }

    // END PVP COMBAT

    public static void updateCombat(Player player) {
        if (isInCombat(player))
            COMBAT.put(player, 10);
        else
        	addToCombat(player);
    }

    public static void addToCombat(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null || isInCombat(player) || !wrapper.isVulnerable())
        	return;
       
        COMBAT.put(player, 10);
        TitleAPI.sendActionBar(player, ChatColor.RED.toString() + ChatColor.BOLD + "Entering Combat", 4 * 20);
        
        /*
         Knock player off of horse, if they're tagged in combat.
         */
        if (player.getVehicle() != null) {
        	MountUtils.removeMount(player);
        	if (player.getVehicle() != null)
        		player.getVehicle().remove();
        	player.sendMessage(ChatColor.RED + "You have been dismounted as you have taken damage!");
        }
    }

    public static void removeFromCombat(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;
        if (isInCombat(player)) {
            COMBAT.remove(player);
            //Removes all arrows from player.
            ((CraftPlayer) player).getHandle().getDataWatcher().set(new DataWatcherObject<>(9, DataWatcherRegistry.b), 0);
            TitleAPI.sendActionBar(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "Leaving Combat", 4 * 20);
        }
    }

    public static void handleCombatLogger(Player player) {
    	// If we ever re-enable this, redo it.
        final World world = player.getWorld();
        final Location loc = player.getLocation();
        CombatLogger combatLogger = new CombatLogger(player.getUniqueId());
        List<ItemStack> itemsToDrop = new ArrayList<>();
        List<ItemStack> armorToDrop = new ArrayList<>();
        List<ItemStack> itemsToSave = new ArrayList<>();
        List<ItemStack> armorToSave = new ArrayList<>();
        PlayerWrapper pw = PlayerWrapper.getWrapper(player);
        KarmaHandler.EnumPlayerAlignments alignments = pw.getAlignment();
        int lvl = pw.getLevel();
        if (alignments == null)
            return;
        
        Random random = ThreadLocalRandom.current();
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

//        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.IS_COMBAT_LOGGED, true, true);

        MeleeZombie combatNMSNPC = new MeleeZombie(((CraftWorld) world).getHandle());
        combatNMSNPC.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ((CraftWorld) world).getHandle().addEntity(combatNMSNPC, CreatureSpawnEvent.SpawnReason.CUSTOM);
        Zombie combatNPC = (Zombie) combatNMSNPC.getBukkitEntity();
        NBTUtils.nullifyAI(combatNPC);
        combatNPC.getEquipment().setArmorContents(player.getEquipment().getArmorContents());
        combatNPC.getEquipment().setItemInMainHand(player.getEquipment().getItemInMainHand());
        combatNPC.getEquipment().setItemInOffHand(player.getEquipment().getItemInOffHand());
        combatNPC.setCustomName(ChatColor.AQUA + "[Lvl. " + lvl + "]" + ChatColor.RED + " " + player.getName());
        combatNPC.setCustomNameVisible(true);
        MetadataUtils.registerEntityMetadata(combatNPC, EnumEntityType.HOSTILE_MOB, 4, lvl);
        HealthHandler.setHP(combatNPC, HealthHandler.getHP(player));
        combatNPC.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), HealthHandler.getMaxHP(player)));
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
            //Just hold the player in memory? I like it.
            if (CombatLog.getInstance().getCOMBAT_LOGGERS().containsKey(player.getUniqueId())) {
                CombatLog.getInstance().getCOMBAT_LOGGERS().get(player.getUniqueId()).handleTimeOut();
            }

        }, 200L);
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
            for (Map.Entry<Player, Integer> e : PVP_COMBAT.entrySet()) {
                if (e.getValue() <= 0) {
                    removeFromPVP(e.getKey());
                } else {
                    PVP_COMBAT.put(e.getKey(), (e.getValue() - 1));
                }
            }
        }, 0, 20L);
    }

    @Override
    public void stopInvocation() {

    }

    public static void checkCombatLog(UUID uuid) {
        if (CombatLog.getInstance().getCOMBAT_LOGGERS().containsKey(uuid)) {
            CombatLogger combatLogger = CombatLog.getInstance().getCOMBAT_LOGGERS().get(uuid);
            if (combatLogger.getLoggerNPC().isDead()) {
                combatLogger.handleNPCDeath();
            } else {
                HealthHandler.setHP(Bukkit.getPlayer(uuid), HealthHandler.getHP(combatLogger.getLoggerNPC()));
                combatLogger.handleTimeOut();
            }

            //Get characterID.
            SQLDatabaseAPI.getInstance().getSqlQueries().add("UPDATE characters SET combatLogged = 0 WHERE character_id = '" + "';");
            GameAPI.submitAsyncCallback(() -> {

                //NEEDS RECODE.
//                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_COMBAT_LOGGED, false, true);
                return true;
            }, null);
        }
    }
}