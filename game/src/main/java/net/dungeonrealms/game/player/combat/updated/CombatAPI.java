package net.dungeonrealms.game.player.combat.updated;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.title.TitleAPI;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Giovanni on 24-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CombatAPI implements Listener {

    @Getter
    private Map<CombatEntity, BukkitTask> entityTasks;

    @Getter
    private Map<UUID, Long> combatTagged;

    @Getter
    private List<UUID> combatLogged;

    private static CombatAPI instance;

    public void start() {
        DungeonRealms.getInstance().getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        new CombatListener().start();
        this.entityTasks = Maps.newHashMap();
        this.combatTagged = Maps.newHashMap();
        this.combatLogged = Lists.newArrayList();
    }

    public static CombatAPI getInstance() {
        if (instance == null) {
            instance = new CombatAPI();
        }
        return instance;
    }

    /**
     * Set a player in combat
     *
     * @param player The player
     */
    public void tag(Player player) {
        if(!this.combatTagged.containsKey(player)) {
            TitleAPI.sendActionBar(player, ChatColor.RED.toString() + ChatColor.BOLD + "Entering Combat", 4 * 20);
        }
        this.combatTagged.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Check if a player is live combat logged on the server session
     *
     * @param player The unique id
     * @return Boolean
     */
    public boolean isLiveLogged(Player player) {
        return this.combatLogged.contains(player.getUniqueId());
    }

    /**
     * Check if a player is in combat
     *
     * @param player The player
     * @return Boolean
     */
    public boolean isTagged(Player player) {
        return combatTagged.containsKey(player.getUniqueId()) && (System.currentTimeMillis() - combatTagged.get(player.getUniqueId()) < 10 * 1000);
    }

    /**
     * Save all combat logged players
     */
    public void saveCombatLoggers() {
        for (UUID uuid : this.combatLogged) {
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_COMBAT_LOGGED, true, true, null);
        }
        Constants.log.info("Saved all combat loggers");
    }

    /**
     * Set a player to to combat logged
     *
     * @param uuid The unique id of the player
     */
    public void setLogged(UUID uuid) {
        combatLogged.add(uuid);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_COMBAT_LOGGED, true, true, null);
    }

    /**
     * Unset a player to to combat logged
     *
     * @param uuid The unique id of the player
     */
    public void unsetLogged(UUID uuid) {
        combatLogged.remove(uuid);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_COMBAT_LOGGED, false, true, null);
    }

    /**
     * Check if a player has a combat logger entity
     *
     * @param player The player
     * @return Boolean
     */
    public boolean hasLogger(Player player) {
        return this.getOf(player) != null;
    }

    /**
     * Get the corresponding combat entity of a logged player
     *
     * @param player The player
     * @return CombatEntity
     */
    public CombatEntity getOf(Player player) {
        for (CombatEntity combatEntity : this.entityTasks.keySet()) {
            if (combatEntity.getOwner().equals(player.getUniqueId())) {
                return combatEntity;
            }
        }
        return null;
    }

    public CombatEntity getOf(Zombie zombie) {
        for (CombatEntity combatEntity : this.entityTasks.keySet()) {
            if (combatEntity.getLoggerEntity().getUniqueId().equals(zombie.getUniqueId())) {
                return combatEntity;
            }
        }
        return null;
    }

    /**
     * Despawn a combat entity
     *
     * @param combatEntity The combat entity
     */
    public void despawnEntity(CombatEntity combatEntity) {
        if (combatEntity.getLoggerEntity() != null && !combatEntity.getLoggerEntity().isDead()) {
            combatEntity.getLoggerEntity().remove();
        }
        // Remove & stop the task
        if (this.entityTasks.containsKey(combatEntity)) {
            this.entityTasks.get(combatEntity).cancel();
            this.entityTasks.remove(combatEntity);
        }
    }

    /**
     * Spawn a combat entity and handle the task
     *
     * @param combatEntity The combat entity
     */
    public void spawnEntity(CombatEntity combatEntity) {
        if (!this.entityTasks.containsKey(combatEntity)) {
            this.entityTasks.put(combatEntity, DungeonRealms.getInstance().getServer().getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                this.despawnEntity(combatEntity);
            }, 15 * 20));
        } else this.despawnEntity(combatEntity);
    }

    /**
     * Create a logger NPC
     *
     * @param player The player
     * @return Zombie
     */
    public Zombie createOn(Player player) {
        Zombie npc = player.getWorld().spawn(player.getLocation(), Zombie.class);
        npc.setBaby(false);
        npc.setVillager(false);
        npc.setCustomName(player.getName());
        npc.setCustomNameVisible(true);
        npc.setRemoveWhenFarAway(true);
        npc.getEquipment().setArmorContents(player.getInventory().getArmorContents());
        npc.getEquipment().setHelmet(this.playerSkull(player));
        // No AI
        this.noAI(npc);
        if (npc.isInsideVehicle()) npc.getVehicle().eject();
        return npc;
    }

    /**
     * Create a player skull
     *
     * @param player The player
     * @return ItemStack
     */
    private ItemStack playerSkull(Player player) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        skull.setItemMeta(meta);
        return skull;
    }

    /**
     * Remove the AI of an entity
     *
     * @param bukkitEntity The entity
     */
    private void noAI(Entity bukkitEntity) {
        net.minecraft.server.v1_9_R2.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        nmsEntity.c(tag);
        tag.setInt("NoAI", 1);
        nmsEntity.f(tag);
    }
}
