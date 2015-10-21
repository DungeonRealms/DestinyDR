package net.dungeonrealms.party;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.handlers.ScoreboardHandler;
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
            for (Player player : rp.members) {
                Objective objective = ScoreboardHandler.getInstance().getPlayerScoreboardObject(player).getObjective(DisplaySlot.SIDEBAR);
                if (objective == null) {
                    objective = ScoreboardHandler.getInstance().getPlayerScoreboardObject(player).registerNewObjective("party", "scoreboard");
                    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                    objective.setDisplayName(ChatColor.WHITE + "(" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + ")");
                }
                ScoreboardHandler.getInstance().getPlayerScoreboardObject(player).resetScores(player.getName());
                Score score = objective.getScore(player.getName());
                score.setScore(HealthHandler.getInstance().getPlayerHPLive(player));
            }

            if (rp.getOwner() != null) {
                Objective objective = ScoreboardHandler.getInstance().getPlayerScoreboardObject(rp.getOwner()).getObjective(DisplaySlot.SIDEBAR);
                if (objective == null) {
                    objective = ScoreboardHandler.getInstance().getPlayerScoreboardObject(rp.getOwner()).registerNewObjective("party", "scoreboard");
                    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                    objective.setDisplayName(ChatColor.WHITE + "(" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + ")");
                }
                ScoreboardHandler.getInstance().getPlayerScoreboardObject(rp.getOwner()).resetScores(rp.getOwner().getName());
                Score score = objective.getScore(rp.getOwner().getName());
                score.setScore(HealthHandler.getInstance().getPlayerHPLive(rp.getOwner()));
            } else {
                rp.setOwner(rp.members.get(new Random().nextInt(rp.members.size())));
                Objective objective = ScoreboardHandler.getInstance().getPlayerScoreboardObject(rp.getOwner()).getObjective(DisplaySlot.SIDEBAR);
                if (objective == null) {
                    objective = ScoreboardHandler.getInstance().getPlayerScoreboardObject(rp.getOwner()).registerNewObjective("party", "scoreboard");
                    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                    objective.setDisplayName(ChatColor.WHITE + "(" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + ")");
                }
                ScoreboardHandler.getInstance().getPlayerScoreboardObject(rp.getOwner()).resetScores(rp.getOwner().getName());
                Score score = objective.getScore(rp.getOwner().getName());
                score.setScore(HealthHandler.getInstance().getPlayerHPLive(rp.getOwner()));
            }
        }
    }

    /**
     * Creates a party.
     *
     * @param player
     * @since 1.0
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
            ScoreboardHandler.getInstance().getPlayerScoreboardObject(members).resetScores(members.getName());
            ScoreboardHandler.getInstance().getPlayerScoreboardObject(members).resetScores(party.owner.getName());
            members.setScoreboard(members.getScoreboard());
            members.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.RED + "The party has been disbanded!");
        }
        if (party.owner != null) {
            ScoreboardHandler.getInstance().getPlayerScoreboardObject(party.owner).resetScores(party.owner.getName());
            party.owner.setScoreboard(party.owner.getScoreboard());
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
        ScoreboardHandler.getInstance().getPlayerScoreboardObject(player).resetScores(player.getName());
        player.setScoreboard(player.getScoreboard());
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
     * Handles players logging out
     * removes them from a party
     * if applicable
     *
     * @param player
     * @since 1.0
     */
    public void handleLogout(Player player) {
        if (isInParty(player)) {
            quitParty(player);
        }
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
            ScoreboardHandler.getInstance().getPlayerScoreboardObject(player).resetScores(player.getName());
            player.setScoreboard(player.getScoreboard());
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
                inviting.add(player);
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "You've been invited to " + owner.getName() + "'s Party!");
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Type /accept party " + owner.getName() + " to join!");
            } else {
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY" + ChatColor.WHITE + "] " + ChatColor.RED + "This player has already been invited!");
            }
        }

    }
}
