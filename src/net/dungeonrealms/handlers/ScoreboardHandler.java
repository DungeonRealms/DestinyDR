package net.dungeonrealms.handlers;

import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumGuildData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Kieran on 10/21/2015.
 */
public class ScoreboardHandler implements GenericMechanic {

    private static ScoreboardHandler instance = null;

    public static ScoreboardHandler getInstance() {
        if (instance == null) {
            instance = new ScoreboardHandler();
        }
        return instance;
    }

    public HashMap<UUID, Scoreboard> PLAYER_SCOREBOARDS = new HashMap<>();
    private Scoreboard mainScoreboard;

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {
        mainScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = mainScoreboard.registerNewObjective("mainScoreboard", "mainScoreboard");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        objective.setDisplayName(ChatColor.RED.toString() + "❤");
    }

    @Override
    public void stopInvocation() {
    }

    /**
     * Gets the players scoreboard from hashmap
     * or creates one if it is not there.
     *
     * @param player
     * @return Scoreboard
     * @since 1.0
     */
    public Scoreboard getPlayerScoreboardObject(Player player) {
        if (!(PLAYER_SCOREBOARDS.containsKey(player.getUniqueId()))) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

            Objective objective = scoreboard.registerNewObjective("playerScoreboard", "playerScoreboard");
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            objective.setDisplayName(ChatColor.RED.toString() + "❤");

            PLAYER_SCOREBOARDS.put(player.getUniqueId(), scoreboard);
            player.setScoreboard(scoreboard);
        }
        return PLAYER_SCOREBOARDS.get(player.getUniqueId());
    }

    /**
     * Updates the players HP
     * (Shown below the players name to
     * other players).
     *
     * @param player
     * @param hp
     * @since 1.0
     */
    public void updatePlayerHP(Player player, int hp) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            getPlayerScoreboardObject(player1).getObjective(DisplaySlot.BELOW_NAME).getScore(player.getName()).setScore(hp);
        }
        mainScoreboard.getObjective(DisplaySlot.BELOW_NAME).getScore(player.getName()).setScore(hp);
    }

    /**
     * Updates the players name and tab menu
     * (Shown to other players).
     *
     * @param player
     * @param chatColor
     * @param playerLevel
     * @since 1.0
     */
    public void setPlayerHeadScoreboard(Player player, ChatColor chatColor, int playerLevel) {
        String suffix = "";
        if (!Guild.getInstance().isGuildNull(player.getUniqueId())) {
            String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()));
            suffix = ChatColor.translateAlternateColorCodes('&', ChatColor.RESET + " [" + clanTag + ChatColor.RESET + "]");
        }
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            Team team = getPlayerTeam(getPlayerScoreboardObject(player1), player);
            team.setPrefix(ChatColor.LIGHT_PURPLE + "[" + playerLevel + "] " + chatColor);
            team.setSuffix(suffix);
            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
        }
        Team team = getPlayerTeam(mainScoreboard, player);
        team.setPrefix(ChatColor.LIGHT_PURPLE + "[" + playerLevel + "] " + chatColor);
        team.setSuffix(suffix);
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    /**
     * Gets the team named after the player
     * or creates one if there is not one.
     *
     * @param scoreboard
     * @param player
     * @return Scoreboard
     * @since 1.0
     */
    public Team getPlayerTeam(Scoreboard scoreboard, Player player) {
        if (scoreboard.getTeam(player.getName()) == null) {
            return scoreboard.registerNewTeam(player.getName());
        }
        return scoreboard.getTeam(player.getName());
    }

    /**
     * Gets the team named after the player
     * or creates one if there is not one.
     *
     * @param scoreboard
     * @param name
     * @return Scoreboard
     * @since 1.0
     */
    private Team getMainScoreboardTeam(Scoreboard scoreboard, String name) {
        if (mainScoreboard.getTeam(name) == null) {
            mainScoreboard.registerNewTeam(name);
        }
        if (scoreboard.getTeam(name) == null) {
            return scoreboard.registerNewTeam(name);
        }
        return scoreboard.getTeam(name);
    }

    /**
     * Updates the players scoreboard
     * to match the main scoreboard so
     * that the player is up to date with
     * previous server events.
     *
     * @param player
     * @since 1.0
     */
    public void matchMainScoreboard(Player player) {
        Scoreboard scoreboard = getPlayerScoreboardObject(player);
        for (Team team : mainScoreboard.getTeams()) {
            Team currentTeam = getMainScoreboardTeam(scoreboard, team.getName());
            currentTeam.setAllowFriendlyFire(team.allowFriendlyFire());
            currentTeam.setCanSeeFriendlyInvisibles(team.canSeeFriendlyInvisibles());
            currentTeam.setDisplayName(team.getDisplayName());
            currentTeam.setPrefix(team.getPrefix());
            currentTeam.setSuffix(team.getSuffix());
            team.getEntries().forEach(currentTeam::addEntry);
        }
    }

    /**
     * Removes the player from
     * the scoreboard hashmap.
     *
     * @param player
     * @since 1.0
     */
    public void removePlayerScoreboard(Player player) {
        if (PLAYER_SCOREBOARDS.containsKey(player.getUniqueId())) {
            PLAYER_SCOREBOARDS.remove(player.getUniqueId());
        }
    }
}
