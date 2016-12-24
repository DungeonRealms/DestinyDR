package net.dungeonrealms.game.player.combat.updated;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.minecraft.server.v1_9_R2.EntityZombie;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Giovanni on 24-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CombatEntity {

    @Getter
    private UUID owner;

    @Getter
    private Zombie loggerEntity;

    @Getter
    private ItemStack[] armorContents;

    @Getter
    private ItemStack[] inventoryContents;

    public CombatEntity(Player player) {
        this.owner = player.getUniqueId();
        this.loggerEntity = CombatAPI.getInstance().createOn(player);
        this.armorContents = player.getInventory().getArmorContents();
        this.inventoryContents = player.getInventory().getContents();

        this.loggerEntity.setCustomName(ChatColor.RED.toString() + ChatColor.BOLD + player.getName() + "'s COMBAT INVENTORY");
        this.loggerEntity.setCustomNameVisible(true);

        // Remove entity knockback
        EntityZombie entityZombie = ((CraftZombie) this.loggerEntity).getHandle();
        entityZombie.getAttributeInstance(GenericAttributes.c).setValue(0);
    }

    /**
     * Handle the death of the entity
     *
     * @param location The location
     */
    public void handleDeath(Location location) {
        List<ItemStack> invContents = Lists.newArrayList();
        for(ItemStack itemStack : inventoryContents) {
            if(itemStack.getType() != Material.WRITTEN_BOOK || itemStack.getType() != Material.NETHER_STAR) {
                invContents.add(itemStack);
            }
        }
        DatabaseAPI.getInstance().update(this.owner, EnumOperators.$SET, EnumData.LOGGERDIED, true, true, null);
        // Calculate items to remove based on alignment
        String alignment = (String) DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, this.owner);
        KarmaHandler.EnumPlayerAlignments playerAlignments = KarmaHandler.EnumPlayerAlignments.valueOf(alignment.toUpperCase());
        // The combat logger is CHAOTIC, drop all contents
        if (playerAlignments == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
            // Clear a player's database inventory
            DatabaseAPI.getInstance().update(this.owner, EnumOperators.$SET, EnumData.INVENTORY, "", true, true, null);
            // Clear a player's database armor
            DatabaseAPI.getInstance().update(this.owner, EnumOperators.$SET, EnumData.ARMOR, new ArrayList<>(), true, true, null);
            for (ItemStack a : this.armorContents)
                if (a != null && a.getType() != Material.AIR) location.getWorld().dropItem(location, a);
            invContents.stream().filter(i -> i != null && i.getType() != Material.AIR).forEach(i -> location.getWorld().dropItem(location, i));
        }
        // The combat logger is LAWFUL, only drop inventory contents
        if (playerAlignments == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
            // Clear a player's database inventory
            DatabaseAPI.getInstance().update(this.owner, EnumOperators.$SET, EnumData.INVENTORY, "", true, true, null);
            for (ItemStack i : this.inventoryContents)
                if (i != null && i.getType() != Material.AIR) location.getWorld().dropItem(location, i);
        }
        // The combat logger is NEUTRAL, calculate drop chance
        if (playerAlignments == KarmaHandler.EnumPlayerAlignments.NEUTRAL) {
            Random random = new Random();
            int armorDropChance = random.nextInt(100);
            // Clear database inventory
            DatabaseAPI.getInstance().update(this.owner, EnumOperators.$SET, EnumData.INVENTORY, "", true, true, null);
            invContents.stream().filter(i -> i != null && i.getType() != Material.AIR).forEach(i -> location.getWorld().dropItem(location, i));
            if (armorDropChance > 75) {
                ItemStack toDrop = this.armorContents[random.nextInt(this.armorContents.length)];
                location.getWorld().dropItem(location, toDrop);
                String removeBase64 = ItemSerialization.itemStackToBase64(toDrop);
                // Remove armor piece from game inventory
                DatabaseAPI.getInstance().update(this.owner, EnumOperators.$PULL, EnumData.ARMOR, removeBase64, true, true, null);
            }
        }
    }
}
