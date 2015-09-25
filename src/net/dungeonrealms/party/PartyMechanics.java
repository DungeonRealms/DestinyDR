package net.dungeonrealms.party;

import net.dungeonrealms.DungeonRealms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nick on 8/30/2015.
 */
public class PartyMechanics {

    private static PartyMechanics instance = null;

    public static PartyMechanics getInstance() {
        if (instance == null) {
            instance = new PartyMechanics();
        }
        return instance;
    }


    private static ArrayList<Party> PARTIES = new ArrayList<>();


    public void createParty(UUID uuid) {
        if (!(isInParty(uuid))) {
            Party p = new Party(uuid, new ArrayList<>());
            p.setOwner(uuid);
            PARTIES.add(p);
            Bukkit.getPlayer(uuid).sendMessage(new String[]{
                    "You have created a party.",
                    "Type /party invite <name> - to invite players!"
            });
        }
    }

    private boolean isInParty(UUID uuid) {
        for (Party p : PARTIES) {
            if (p.getMembers().contains(uuid) || p.getOwner().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOwner(UUID uuid) {
        for (Party p : PARTIES) {
            if (p.getOwner().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public void kickPlayer(Party party, UUID uuid) {
        Bukkit.getPlayer(uuid).sendMessage(new String[]{
                "You have been kicked from the party!"
        });
        party.getMembers().remove(uuid);
    }

    public void disbandParty(Party party) {
        for (UUID uuid : party.getMembers()) {
            Bukkit.getPlayer(uuid).sendMessage(new String[]{
                    "Your party has been disbanded!"
            });
        }
        PARTIES.remove(party);
    }

    public Party getParty(UUID uuid) {
        if (isOwner(uuid)) {
            for (Party p : PARTIES) {
                if (p.getOwner().equals(uuid)) {
                    return p;
                }
            }
        }
        return null;
    }

    public void inviteToParty(Party p, UUID uuid) {
        if (isOwner(p.getOwner())) {
            Party party = getParty(p.getOwner());
            if (!party.getMembers().contains(uuid)) {
                party.addMember(uuid);
            }
        } else {
            Bukkit.getPlayer(uuid).sendMessage(new String[]{
                    "",
                    "You are not owner or haven't created a party!",
                    ""
            });
        }
    }

    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            PARTIES.forEach(this::setScoreboard);
        }, 0, 20L);
    }

    private void setScoreboard(Party party) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("test", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + Bukkit.getPlayer(party.getOwner()).getDisplayName() + "'s Party");
        for (UUID uuid : party.getMembers()) {
            Score score = objective.getScore(ChatColor.BOLD.toString() + Bukkit.getPlayer(uuid).getName());
            score.setScore((int) Bukkit.getPlayer(uuid).getHealth());
        }
        Score score = objective.getScore(ChatColor.BOLD.toString() + Bukkit.getPlayer(party.getOwner()).getName());
        score.setScore((int) Bukkit.getPlayer(party.getOwner()).getHealth());
        Bukkit.getPlayer(party.getOwner()).setScoreboard(board);

        for (UUID uuid : party.getMembers()) {
            Bukkit.getPlayer(uuid).setScoreboard(board);
        }
        Bukkit.getPlayer(party.getOwner()).setScoreboard(board);
    }
}