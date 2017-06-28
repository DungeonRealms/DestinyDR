package net.dungeonrealms.game.listener;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.punishment.PunishAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class MessageTracker {
    @Getter
    @Setter
    private int sentRecently;

    @Getter
    private long lastSent;

    public boolean onChatMessage(Player player, String msg) {
        if (System.currentTimeMillis() - lastSent <= TimeUnit.SECONDS.toMillis(2)) {
            sentRecently++;
        } else {
            sentRecently = 0;
        }
        this.lastSent = System.currentTimeMillis();

        if (sentRecently >= 8) {
            //Mute for 5 minutes?
            PunishAPI.mute(PlayerWrapper.getPlayerWrapper(player), TimeUnit.MINUTES.toSeconds(5), "Excessive Spam", done -> {
                player.sendMessage(ChatColor.RED + "You have muted for excessively spamming.");
                GameAPI.sendStaffMessage("Muting " + player.getName() + " for spamming " + sentRecently + " messages quickly.");
            });
            return false;
        }

        return true;
    }
}
