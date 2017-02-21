package net.dungeonrealms.game.player.notice;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.handler.MailHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.tool.PatchTools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Nick on 10/11/2015.
 */
@SuppressWarnings("unchecked")
public class Notice {

    static Notice instance = null;

    public static Notice getInstance() {
        if (instance == null) {
            instance = new Notice();
        }
        return instance;
    }

    public Notice() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            Bukkit.getOnlinePlayers().forEach(this::executeVoteReminder);
        }, 0L, 6000L);

    }

    //TODO: Friends, Guilds and clickable acceptance.

    /**
     * This type handles notices per player.
     * E.g. A player logs in and has a guild invite,
     * or a player logs in and has pending friend request.
     *
     * @param player
     * @since 1.0
     */
    public void doLogin(Player player) {
        ArrayList<String> mailbox = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MAILBOX, player.getUniqueId());

        if (mailbox.size() > 0)
            MailHandler.getInstance().sendMailMessage(player, ChatColor.GREEN + " ✉ You have " + ChatColor.AQUA + mailbox.size() + ChatColor.GREEN + " new mail! ✉ ");

        String lastViewedBuild = (String) DatabaseAPI.getInstance().getData(EnumData.LAST_BUILD, player.getUniqueId());
        
        int lastSeenBuild = -1;
        int serverBuild = Integer.parseInt(Constants.BUILD_NUMBER.substring(1));
        if(lastViewedBuild != null)
        	lastSeenBuild = Integer.parseInt(lastViewedBuild.substring(1));
        
        Object noteSize = DatabaseAPI.getInstance().getData(EnumData.LAST_NOTES_SIZE, player.getUniqueId());
        
        if (lastViewedBuild == null || (serverBuild > lastSeenBuild && (noteSize == null || PatchTools.getInstance().getSize() != (Integer)noteSize)))
            Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> executeBuildNotice(player), 150);

        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> executeVoteReminder(player), 300);
    }

    private void executeBuildNotice(Player p) {
        final JSONMessage normal = new JSONMessage(ChatColor.GOLD + " ❢ " + ChatColor.YELLOW + "Patch notes available for Build " + Constants.BUILD_NUMBER + " " + ChatColor.GRAY + "View notes ", ChatColor.WHITE);
        normal.addRunCommand(ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "/patch");
        normal.addText(ChatColor.GOLD + " ❢ ");

        p.sendMessage(" ");
        normal.sendToPlayer(p);
        p.sendMessage(" ");

        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 0.5F);

        // UPDATE LAST VIEWED BUILD NUMBER //
        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.LAST_BUILD, Constants.BUILD_NUMBER, true);
        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.LAST_NOTES_SIZE, PatchTools.getInstance().getSize(), true);
    }

    private void executeVoteReminder(Player p) {
        Long vote = (Long) DatabaseAPI.getInstance().getData(EnumData.LAST_VOTE, p.getUniqueId());

        if (vote == null || (System.currentTimeMillis() - vote) >= 86400000) {
            int ecashAmount = 15;
            if (Rank.isSubscriberPlus(p)) ecashAmount = 25;
            else if (Rank.isSubscriber(p)) ecashAmount = 20;

            GamePlayer gp = GameAPI.getGamePlayer(p);
            if (gp == null || gp.isStreamMode()) {
                return;
            }

            p.sendMessage(" ");
            final JSONMessage message = new JSONMessage("Hey there! You have not voted for a day. Vote for " + ecashAmount + " ECASH & 5% EXP, click ", ChatColor.GRAY);
            message.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://dungeonrealms.net/vote");
            message.sendToPlayer(p);
            p.sendMessage(" ");
        }
    }


}
