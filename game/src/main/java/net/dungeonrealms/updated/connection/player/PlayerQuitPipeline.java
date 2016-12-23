package net.dungeonrealms.updated.connection.player;

import com.mongodb.client.model.UpdateOneModel;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import net.dungeonrealms.game.mastery.DamageTracker;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.updated.connection.pipeline.DataPipeline;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerQuitPipeline extends DataPipeline {

    @Override
    public void handle(UUID uniqueId) {
        if (Bukkit.getPlayer(uniqueId) != null && Bukkit.getPlayer(uniqueId).isOnline()) {
            Player player = Bukkit.getPlayer(uniqueId);
            if (DatabaseAPI.getInstance().PLAYERS.containsKey(uniqueId)) {
                // Stop listening
                this.stopHandlersOn(uniqueId);
                // Save
                this.saveData(uniqueId);
            } else {
                Constants.log.warning("Limbo'd player detected: " + player.getName());
            }
        }
    }

    /**
     * Stop all handlers from listening to a player
     *
     * @param uniqueId The unique id of the player
     */
    private void stopHandlersOn(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        // Prevent dungeon entry
        DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 15);
        Chat.listenForMessage(player, null, null);

        // Damage tacking
        for (DamageTracker tracker : HealthHandler.getInstance().getMonsterTrackers().values()) {
            tracker.removeDamager(player);
        }
        // Dungeon item stuff
        if (player.getWorld().getName().contains("DUNGEON")) {
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack != null && stack.getType() != Material.AIR) {
                    if (DungeonManager.getInstance().isDungeonItem(stack)) {
                        player.getInventory().remove(stack);
                    }
                }
            }
        }
        // Bank item stuff
        if (BankMechanics.shopPricing.containsKey(player.getName())) {
            player.getInventory().addItem(BankMechanics.shopPricing.get(player.getName()));
            BankMechanics.shopPricing.remove(player.getName());
        }
        if (GameAPI._hiddenPlayers.contains(player)) {
            GameAPI._hiddenPlayers.remove(player);
        }
        if (!DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) {
            return;
        }
        MountUtils.inventories.remove(uniqueId);
        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.LAST_LOGOUT, System.currentTimeMillis(), true, true, null);
        EnergyHandler.getInstance().handleLogoutEvents(player);
        HealthHandler.getInstance().handleLogoutEvents(player);
        KarmaHandler.getInstance().handleLogoutEvents(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            ScoreboardHandler.getInstance().removePlayerScoreboard(player);
        });
        if (EntityAPI.hasPetOut(uniqueId)) {
            net.minecraft.server.v1_9_R2.Entity pet = EntityMechanics.PLAYER_PETS.get(uniqueId);
            pet.dead = true;
            if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(pet)) {
                DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(pet);
            }
            EntityAPI.removePlayerPetList(uniqueId);
        }
        if (EntityAPI.hasMountOut(uniqueId)) {
            net.minecraft.server.v1_9_R2.Entity mount = EntityMechanics.PLAYER_MOUNTS.get(uniqueId);
            if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(mount)) {
                DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(mount);
            }
            if (mount.isAlive()) { // Safety check
                if (mount.passengers != null) {
                    mount.passengers.forEach(passenger -> passenger = null);
                }
                mount.dead = true;
            }
            EntityAPI.removePlayerMountList(uniqueId);
        }

        if (Affair.getInstance().isInParty(player)) {
            Affair.getInstance().removeMember(player, false);
        }

        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.IS_PLAYING, false, true, true, null);
    }

    /**
     * Save a player's data
     *
     * @param uniqueId The unique id of the player
     */
    public void saveData(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);

        // BANK AND COLLECTION BIN
        if (BankMechanics.storage.containsKey(uniqueId)) {
            Inventory inv = BankMechanics.getInstance().getStorage(uniqueId).inv;
            if (inv != null)
                DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.INVENTORY_STORAGE, ItemSerialization.toString(inv), true, true, null);

            inv = BankMechanics.getInstance().getStorage(uniqueId).collection_bin;
            if (inv != null)
                DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, ItemSerialization.toString(inv), true, true, null);
        }

        // PLAYER ARMOR AND INVENTORY
        Inventory inv = player.getInventory();
        ArrayList<String> armor = new ArrayList<>();
        for (ItemStack stack : player.getEquipment().getArmorContents())
            if (stack == null || stack.getType() == Material.AIR) armor.add("");
            else armor.add(ItemSerialization.itemStackToBase64(stack));
        ItemStack offHand = player.getEquipment().getItemInOffHand();
        if (offHand == null || offHand.getType() == Material.AIR) armor.add("");
        else armor.add(ItemSerialization.itemStackToBase64(offHand));

        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.INVENTORY, ItemSerialization.toString(inv), true, true, null);
        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.ARMOR, armor, true, true, null);

        String locationAsString;

        // LOCATION
        if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
            locationAsString = player.getLocation().getX() + "," + (player.getLocation().getY()) + ","
                    + player.getLocation().getZ() + "," + player.getLocation().getYaw() + ","
                    + player.getLocation().getPitch();
            DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, true, true, null);
        }

        // MULE INVENTORY
        if (MountUtils.inventories.containsKey(uniqueId))
            DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.INVENTORY_MULE, ItemSerialization.toString(MountUtils.inventories.get(uniqueId)), true, true, null);

        // LEVEL AND STATISTICS
        if (GameAPI.GAMEPLAYERS.containsKey(player.getName())) {
            GamePlayer gp = GameAPI.getGamePlayer(player);
            if (gp != null) {
                DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.EXPERIENCE, gp.getPlayerEXP(), true, true, null);
                gp.getPlayerStatistics().updatePlayerStatistics();
                gp.getStats().updateDatabase(false);
            }
        }

        // MISC
        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.CURRENT_FOOD, player.getFoodLevel(), true, true, null);
        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.HEALTH, HealthHandler.getInstance().getPlayerHPLive(player), true, true, null);
        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.ALIGNMENT, KarmaHandler.getInstance().getPlayerRawAlignment(player).name(), true, true, null);
        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.ALIGNMENT_TIME, KarmaHandler.getInstance().getAlignmentTime(player), true, true, null);

        // Done
        DatabaseAPI.getInstance().PLAYERS.remove(uniqueId);
        GameAPI.GAMEPLAYERS.remove(player.getName());
    }
}
