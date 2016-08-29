package net.dungeonrealms.game.affair.party;

import lombok.Data;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/1/2016
 */

@Data
public class Party {

    private Player owner;

    private List<Player> members;

    private boolean updateScoreboard;

    private Scoreboard partyScoreboard;

    public Party(Player owner, List<Player> members) {
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
