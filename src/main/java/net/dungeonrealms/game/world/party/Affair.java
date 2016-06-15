package net.dungeonrealms.game.world.party;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.ScoreboardHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Nick on 11/9/2015.
 */
public class Affair implements GenericMechanic {

    static Affair instance = null;

    public static Affair getInstance() {
        if (instance == null) {
            instance = new Affair();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    public CopyOnWriteArrayList<AffairO> _parties = new CopyOnWriteArrayList<>();
    public static ConcurrentHashMap<Player, AffairO> _invitations = new ConcurrentHashMap<>();

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> _parties.stream().forEach(party -> {
            if (party.getOwner() == null) {
                removeParty(party);
            } else {
            /*
            Scoreboards
             */
                Scoreboard board;
                if (party.getPartyScoreboard() == null) {
                    board = party.createScoreboard();
                    party.setPartyScoreboard(board);
                } else {
                    board = party.getPartyScoreboard();
                }

                Objective objective = board.getObjective("party");
                if (objective == null) {
                    objective = board.registerNewObjective("party", "dummy");
                    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                    objective.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Party");
                }

                List<Player> allPlayers = new ArrayList<>();
                allPlayers.add(party.getOwner());
                allPlayers.addAll(party.getMembers());

                for (Player player : allPlayers) {
                    if (player != null) {
                        Score score = objective.getScore(player.getName());
                        score.setScore(HealthHandler.getInstance().getPlayerHPLive(player));

                        //Only set the scoreboard if we need to as setScoreboard will send packets and also cause the sb to flicker
                        if (player.getScoreboard() != board) {
                            player.setScoreboard(board);
                        }
                    }
                }


//				allPlayers.stream().filter(player1 -> player1 != null).forEach(player -> player.setScoreboard(board));
            }

        }), 0, 15);
    }

    public void invitePlayer(Player inviting, Player invitor) {
        _invitations.put(inviting, getParty(invitor).get());
        inviting.sendMessage(
                ChatColor.LIGHT_PURPLE.toString() + ChatColor.UNDERLINE + invitor.getName() + ChatColor.GRAY + " has invited you to join their party! To accept, type "
                        + ChatColor.LIGHT_PURPLE + "/paccept" + ChatColor.GRAY + " or to decline, type " + ChatColor.LIGHT_PURPLE + "/pdecline");

        invitor.sendMessage(ChatColor.GRAY + "You have invited " + ChatColor.LIGHT_PURPLE + inviting.getDisplayName() + ChatColor.GRAY + " to join your party.");
    }

    public void removeParty(AffairO party) {

        List<Player> allPlayers = new ArrayList<>();
        allPlayers.add(party.getOwner());
        allPlayers.addAll(party.getMembers());

        allPlayers.stream().forEach(player -> {
            player.setScoreboard(ScoreboardHandler.getInstance().mainScoreboard);
            player.sendMessage(ChatColor.RED + "Your party has been disbanded.");
        });


        Utils.log.info("Deleted Old Party: " + party.toString());

        _parties.remove(party);
    }

    public void removeMember(Player player, boolean kicked) {
        if (!getParty(player).isPresent()) {
            return;
        }

        if (isOwner(player)) {
            removeParty(getParty(player).get());
            return;
        }

        AffairO party = getParty(player).get();

        party.getMembers().remove(player);
        Scoreboard board = party.getPartyScoreboard();
        board.resetScores(player.getName());

        if (kicked)
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You have been kicked out of the party.");
        else
            player.sendMessage(ChatColor.RED + "You have left the party.");

        party.getOwner().sendMessage(ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + "> " + ChatColor.GRAY + player.getDisplayName() + " has " + ChatColor.LIGHT_PURPLE + ChatColor.UNDERLINE + "left" + ChatColor.GRAY + " the party.");
        party.getMembers().stream().forEach(player1 -> player1.sendMessage(ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + "> " + ChatColor.GRAY + player.getDisplayName() + " has " + ChatColor.LIGHT_PURPLE + ChatColor.UNDERLINE + "left" + ChatColor.GRAY + " the party."));

        player.setScoreboard(ScoreboardHandler.getInstance().mainScoreboard);

    }

    public boolean isOwner(Player player) {
        return isInParty(player) && getParty(player).get().getOwner().equals(player);
    }

    public boolean areInSameParty(Player player1, Player player2) {
        return isInParty(player1) && isInParty(player2) && (getParty(player1).get().getOwner().getName()
                .equalsIgnoreCase(getParty(player2).get().getOwner().getName().toLowerCase()));
    }

    public int amountInParty(AffairO party) {
        return party.getMembers().size() + 1;
    }

    public boolean isInParty(Player player) {
        for (AffairO party : _parties) {
            if (party.getOwner().equals(player) || party.getMembers().contains(player)) {
                return true;
            }
        }
        return false;
    }

    public void createParty(Player player) {
        _parties.add(new AffairO(player, new ArrayList<>()));
        player.sendMessage(new String[]{
                ChatColor.GREEN + ChatColor.BOLD.toString() + "Your party has been created!",
                ChatColor.GRAY + "To invite more people to join your party, " + ChatColor.UNDERLINE + "Left Click" + ChatColor.GRAY + " them with your character journal or use " + ChatColor.BOLD + "/pinvite" + ChatColor.GRAY + ". To kick, use " + ChatColor.BOLD + "/pkick" + ChatColor.GRAY + ". To chat with party, use " + ChatColor.BOLD + "/p" + ChatColor.GRAY + ". To change the loot profile, use " + ChatColor.BOLD + "/ploot"
        });

        Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.PARTY_MAKER);
    }

    public Optional<AffairO> getParty(Player player) {
        return _parties.stream().filter(affairO -> affairO.getOwner().equals(player) || affairO.getMembers().contains(player)).findFirst();
    }

    @Override
    public void stopInvocation() {

    }

    public class AffairO {
        private Player owner;
        private List<Player> members;
        private boolean updateScoreboard;

        @Getter
        @Setter
        private Scoreboard partyScoreboard;

        public AffairO(Player owner, List<Player> members) {
            this.owner = owner;
            this.members = members;
            this.partyScoreboard = createScoreboard();
            this.updateScoreboard = false;
        }

        public Scoreboard createScoreboard() {
            Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
            ScoreboardHandler handler = ScoreboardHandler.getInstance();
            handler.setCurrentPlayerLevels(sb);
            handler.registerHealth(sb);
            return sb;
        }

        public Player getOwner() {
            return owner;
        }

        public List<Player> getMembers() {
            return members;
        }

    }
}
