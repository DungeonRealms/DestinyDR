package net.dungeonrealms.party;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.handlers.HealthHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Nick on 10/3/2015.
 */
public class Party {

    static Party instance = null;

    public static Party getInstance() {
        if (instance == null) {
            instance = new Party();
        }
        return instance;
    }

    ArrayList<RawParty> PARTIES = new ArrayList<>();

    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), this::updateParties, 0, 5L);
    }

    public void updateParties() {
        for (RawParty rp : PARTIES) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getNewScoreboard();
            Objective objective = board.registerNewObjective("party", "scoreboard");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.setDisplayName(ChatColor.WHITE + "(" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + ")");

            for (Player player : rp.members) {
                Score score = objective.getScore(player.getName());
                score.setScore(HealthHandler.getPlayerHPLive(player));
            }

            if (rp.getOwner() != null) {
                Score score = objective.getScore(rp.getOwner().getName());
                score.setScore(HealthHandler.getPlayerHPLive(rp.getOwner()));
            } else {
                rp.setOwner(rp.members.get(new Random().nextInt(rp.members.size())));
                Score score = objective.getScore(rp.getOwner().getName());
                score.setScore(HealthHandler.getPlayerHPLive(rp.getOwner()));
            }

            for (Player members : rp.getMembers()) {
                members.setScoreboard(board);
            }
            rp.owner.setScoreboard(board);

        }
    }

    /**
     * Creates a party.
     *
     * @param player
     */
    public void createParty(Player player) {
        if (!isInParty(player)) {
            RawParty p = new RawParty(player, new ArrayList<>(), new ArrayList<>());
            PARTIES.add(p);
            player.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "You've created a party!");
            player.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Type /party invite <playerName>");
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 5f, 63f);
        } else {
            player.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.RED + "You are already in a party!");
        }
    }

    /**
     * Returns the RawParty of a player.
     *
     * @param player
     * @return
     * @since 1.0
     */
    public RawParty getPlayerParty(Player player) {
        for (RawParty rp : PARTIES) {
            if (rp.members.contains(player) || rp.owner.equals(player)) return rp;
        }
        return null;
    }

    /**
     * Disbands a party.
     *
     * @param party
     * @since 1.0
     */
    public void disbandParty(RawParty party) {
        for (Player members : party.getMembers()) {
            members.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            members.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.RED + "The party has been disbanded!");
        }
        if (party.owner != null) {
            party.owner.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            party.owner.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.RED + "Your party has been disbanded!");
        }
        PARTIES.remove(party);
    }

    /**
     * Kicks a player from a party.
     *
     * @param party
     * @param player
     * @since 1.0
     */
    public void kickPlayer(RawParty party, Player player) {
        party.getMembers().remove(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        party.owner.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.RED + "You have been kicked from the party!");
    }

    /**
     * Checks if the player owns a party.
     *
     * @param player
     * @return
     * @since 1.0
     */
    public boolean isOwnerOfParty(Player player) {
        for (RawParty rp : PARTIES) {
            if (rp.owner.equals(player)) return true;
        }
        return false;
    }

    /**
     * Invites a player to a party.
     *
     * @param player
     * @param party
     * @since 1.0
     */
    public void invitePlayer(Player player, RawParty party) {
        party.invitePlayer(player);
    }

    /**
     * Checks if a player is in a party.
     *
     * @param player
     * @return
     * @since 1.0
     */
    public boolean isInParty(Player player) {
        for (RawParty rp : PARTIES) {
            if (rp.members.contains(player) || rp.owner.equals(player)) return true;
        }
        return false;
    }

    /**
     * Quits a player out the party, if player is owner.
     * The party is disbanded!
     *
     * @param player
     * @sicne 1.0
     */
    public void quitParty(Player player) {
        RawParty party = getPlayerParty(player);
        if (party.members.contains(player)) {
            party.members.remove(player);
            party.owner.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.RED + "You have left the party!");
        } else if (party.owner.equals(player)) {
            disbandParty(party);
        }
    }


    public class RawParty {
        private Player owner;
        private List<Player> members;
        private List<Player> inviting;

        public RawParty(Player owner, List<Player> members, List<Player> inviting) {
            this.owner = owner;
            this.members = members;
            this.inviting = inviting;
        }

        public Player getOwner() {
            return owner;
        }

        public List<Player> getMembers() {
            return members;
        }

        public List<Player> getInviting() {
            return inviting;
        }

        public void setOwner(Player player) {
            this.owner = player;
        }

        public void invitePlayer(Player player) {
            if (!inviting.contains(player)) {
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "You've been invited to " + owner.getName() + "'s Party!");
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Type /party accept " + owner.getName() + " to join!");
            } else {
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.RED + "This player has already been invited!");
            }
        }

    }
}
