package net.dungeonrealms.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Nick on 9/18/2015.
 */
public class PlayerManager {

    /**
     * Ensures that every time the player logs in
     * the last slot (8) has the correct item.
     *
     * @param uuid
     * @since 1.0
     */
    public static void checkInventory(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        player.getInventory().setItem(7, ItemManager.createCharacterJournal(Bukkit.getPlayer(uuid)));
        player.getInventory().setItem(8, ItemManager.getPlayerProfile(player, "Character Profile", new String[]{
                ChatColor.GRAY + "(Right-Click) " + ChatColor.AQUA + "Open your profile!"
        }));

    }

    public enum PlayerToggles {
        DEBUG(0, "DEBUG", "Displays statistics such as damage given/taken and health after armor swaps."),
        TRADE(1, "TRADE", "Disables or enables the ability for you to trade with other players."),
        TRADE_CHAT(2, "TRADECHAT", "Disables or enables your ability to see the trade chat."),
        GLOBAL_CHAT(3, "GLOBALCHAT", "Disables or enables your ability to see global chat."),
        RECEIVE_MESSAGES(4, "RECEIVEMESSAGES", "Disables or enables your ability to receive PMs from players."),
        PVP(5, "PVP", "Disables or enables the ability enter PvP combat."),
        DUEL(6, "DUEL", "Disables or enables the ability to participate in duels."),;

        private int id;
        private String rawName;
        private String description;

        PlayerToggles(int id, String rawName, String description) {
            this.id = id;
            this.rawName = rawName;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static PlayerToggles getByName(String rawName) {
            for (PlayerToggles playerToggles : values()) {
                if (playerToggles.rawName.equalsIgnoreCase(rawName)) {
                    return playerToggles;
                }
            }
            return null;
        }
    }
}
