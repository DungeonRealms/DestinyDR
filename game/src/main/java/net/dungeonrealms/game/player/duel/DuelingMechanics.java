package net.dungeonrealms.game.player.duel;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.network.ShardInfo;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Chase on Nov 13, 2015
 */

public class DuelingMechanics {

    public static ArrayList<DuelOffer> duels = new ArrayList<>();
    public static HashMap<UUID, UUID> pending = new HashMap<>();
    public static ArrayList<UUID> cooldown = new ArrayList<>();

    public static void startDuel(Player p1, Player p2) {
        duels.add(new DuelOffer(p1, p2));
    }

    /**
     * @param sender
     * @param requested
     */
    public static void sendDuelRequest(Player sender, Player requested) {
    	
    	if(DungeonRealms.getShard() == ShardInfo.US1){
    		sender.sendMessage(ChatColor.RED + "Dueling is temporarily disabled on this server.");
    		sender.sendMessage(ChatColor.RED + ChatColor.ITALIC.toString() + "You can duel on another shard in the meantime.");
    		return;
    	}
    	
        if (isOnCooldown(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You're currently on cooldown for sending duel requests!");
            return;
        }
        if (isDueling(requested.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "That player is already dueling!");
            return;
        }

        UUID pendingPartner = getPendingPartner(requested.getUniqueId());
        if (pendingPartner != null && pendingPartner.equals(sender.getUniqueId())) {
//		if (isPending(requested.getUniqueId()) && getPendingPartner(requested.getUniqueId()).toString().equalsIgnoreCase(sender.getUniqueId().toString())) {
            DuelOffer offer = getDuelOffer(sender.getUniqueId(), requested.getUniqueId());
            if (offer != null) return;
            startDuel(sender, requested);
            return;
        }

        if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DUEL, requested.getUniqueId())) {
            pending.put(sender.getUniqueId(), requested.getUniqueId());
            cooldown.add(sender.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "Duel request sent!");
            requested.sendMessage(ChatColor.YELLOW + "Duel request received from " + sender.getName() + "");
            requested.sendMessage(ChatColor.YELLOW + "Shift punch them back to accept");
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                if (pending.containsKey(sender.getUniqueId()))
                    pending.remove(sender.getUniqueId());
                cooldown.remove(sender.getUniqueId());
            }, 100L);// Remove Pending Request after 10 seconds.
        } else {
            sender.sendMessage(ChatColor.RED + "That player has duels toggled off!");
        }
    }

    public static DuelOffer getDuelOffer(UUID uuid, UUID other) {
        for (DuelOffer offer : duels) {
            if ((offer.player1.equals(uuid) || offer.player2.equals(uuid)) && (offer.player1.equals(other) && offer.player2.equals(other)))
                return offer;
        }
        return null;
    }

    /**
     * @param uuid
     * @return UUID
     */
    public static UUID getPendingPartner(UUID uuid) {

        UUID pendingKey = pending.get(uuid);
        if (pendingKey != null) return pendingKey;

        if (pending.containsValue(uuid)) {
            for (UUID id : pending.values()) {
                if (id.toString().equals(uuid.toString())) {
                    for (UUID uniqueId : pending.keySet()) {
                        if (uniqueId.toString().equalsIgnoreCase(id.toString()))
                            return uniqueId;
                    }
                }
            }
        }

        return null;
    }

    /**
     * @param uuid
     * @return boolean
     */
    public static boolean isOnCooldown(UUID uuid) {
        return cooldown.contains(uuid);
    }

    /**
     * @param uuid
     * @return boolean
     */
    public static boolean isPending(UUID uuid) {
        return pending.containsKey(uuid) || pending.containsValue(uuid);
    }

    /**
     * @param uuid
     * @return boolean
     */
    public static boolean isDueling(UUID uuid) {
        DuelOffer offer = getOffer(uuid);
        return offer != null;
//		return !duels.isEmpty() && (uuid.equals(duels.get(0).player1) || uuid.equals(duels.get(0).player2));
    }

    /**
     * @return Duel offer
     */
    public static DuelOffer getOffer(UUID id) {
        for (DuelOffer offer : duels) {
            if (offer.player1.toString().equalsIgnoreCase(id.toString())
                    || offer.player2.toString().equalsIgnoreCase(id.toString()))
                return offer;
        }
        return null;
    }

    /**
     * @param offer
     */
    public static void removeOffer(DuelOffer offer) {
        if (offer.timerID != -1) {
            Bukkit.getScheduler().cancelTask(offer.timerID);
        }
        duels.remove(offer);

    }

    /**
     * @param uniqueId
     * @param uniqueId2
     * @return
     */
    public static boolean isDuelPartner(UUID uniqueId, UUID uniqueId2) {
        DuelOffer offer = getOffer(uniqueId);

        if (offer != null && (offer.player2.equals(uniqueId2) || offer.player1.equals(uniqueId2))) return true;

        return false;
//		return !duels.isEmpty() && (duels.get(0).player1.equals(uniqueId) || duels.get(0).player2.equals(uniqueId))
//				&& (duels.get(0).player1.equals(uniqueId2) || duels.get(0).player2.equals(uniqueId2));
    }
}
