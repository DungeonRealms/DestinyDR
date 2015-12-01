package net.dungeonrealms.game.world.party;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
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

    CopyOnWriteArrayList<AffairO> _parties = new CopyOnWriteArrayList<>();
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
                updateParties();
            }


        }), 0, 15);
    }

    public void invitePlayer(Player inviting, Player invitor) {
        _invitations.put(inviting, getParty(invitor).get());
        inviting.sendMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.UNDERLINE +  invitor.getName() + ChatColor.GRAY + " has invited you to join their party! Type " + ChatColor.LIGHT_PURPLE + "/paccept" + ChatColor.GRAY + " to join, or " + ChatColor.LIGHT_PURPLE + "/pdecline" + ChatColor.GRAY + " to decline.");
    }

    public void removeParty(AffairO party) {

        List<Player> allPlayers = new ArrayList<>();
        allPlayers.add(party.getOwner());
        allPlayers.addAll(party.getMembers());

        allPlayers.stream().forEach(player -> {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            player.sendMessage(ChatColor.RED + "Your party has been disbanded!");
        });

        _parties.remove(party);
    }

    public void removeMember(Player player) {

        if (isOwner(player)) {
            removeParty(getParty(player).get());
            return;
        }

        AffairO party = getParty(player).get();

        party.getMembers().remove(player);
        player.sendMessage(ChatColor.RED + "You have left the party!");
        
        party.getOwner().sendMessage(ChatColor.AQUA + player.getName() + " " + ChatColor.RED + "has left the party!");
        party.getMembers().stream().forEach(player1 -> player1.sendMessage(ChatColor.AQUA + player.getName() + " " + ChatColor.RED + "has left the party!"));

        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

    }

    public boolean isOwner(Player player) {
        return isInParty(player) && getParty(player).get().getOwner().equals(player);
    }


    public void updateParties() {
        _parties.stream().forEach(party -> {

            if (party.getOwner() == null) {
                removeParty(party);
                return;
            }

            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getNewScoreboard();
            Objective objective = board.registerNewObjective("test", "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.setDisplayName(ChatColor.AQUA.toString() + ChatColor.BOLD + "PARTY");

            List<Player> allPlayers = new ArrayList<>();
            {
                allPlayers.add(party.getOwner());
                allPlayers.addAll(party.getMembers());
            }

            allPlayers.stream().filter(player1 -> player1 != null).forEach(player -> {
                Score score = objective.getScore(player.getName());
                score.setScore(HealthHandler.getInstance().getPlayerHPLive(player));
            });

            allPlayers.stream().filter(player1 -> player1 != null).forEach(player -> player.setScoreboard(board));


        });
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
        player.sendMessage(ChatColor.GREEN + "Your party has been created!");
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

        public AffairO(Player owner, List<Player> members) {
            this.owner = owner;
            this.members = members;
        }

        public Player getOwner() {
            return owner;
        }

        public List<Player> getMembers() {
            return members;
        }

    }
}
