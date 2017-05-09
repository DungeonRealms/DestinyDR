package net.dungeonrealms.game.handler;

import lombok.Getter;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;

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

	@Getter private static ScoreboardHandler instance = new ScoreboardHandler();

    public HashMap<UUID, Scoreboard> PLAYER_SCOREBOARDS = new HashMap<>();
    public Scoreboard mainScoreboard;

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
            //Why not just set the default scoreboard to the main one so it doesnt become desynced?
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

            registerHealth(scoreboard);

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
        for (Player player1 : Bukkit.getOnlinePlayers())
            if (!Affair.isInParty(player1))
            	 getPlayerScoreboardObject(player1).getObjective(DisplaySlot.BELOW_NAME).getScore(player.getName()).setScore(hp);

        for (Party party : Affair.getParties()) {
            Scoreboard scoreboard = party.getScoreboard();
            scoreboard.getObjective(DisplaySlot.BELOW_NAME).getScore(player.getName()).setScore(hp);
        }
        mainScoreboard.getObjective(DisplaySlot.BELOW_NAME).getScore(player.getName()).setScore(hp);
    }

    /**
     * Updates the players name and tab menus
     * (Shown to other players).
     *
     * @param player
     * @param chatColor
     * @param playerLevel
     * @since 1.0
     */
    public void setPlayerHeadScoreboard(Player player, final ChatColor chatColor, int playerLevel) {
        ChatColor color = chatColor;
        Rank.PlayerRank rank = Rank.getPlayerRank(player.getUniqueId());
        //Only need to check if GM one time..
        if (rank.isAtLeast(Rank.PlayerRank.TRIALGM))
            color = ChatColor.AQUA;

        String guild = "";

        GuildWrapper guildWrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
        if (guildWrapper != null) {
            String clanTag = guildWrapper.getTag();
            guild = ChatColor.translateAlternateColorCodes('&', ChatColor.RESET + "[" + clanTag + ChatColor.RESET + "] ");
        }

        //Async please thanks.
        //Do this once.
        player.setPlayerListName(rank.getChatColor() + player.getName());
        for (Player player1 : Bukkit.getOnlinePlayers()) {

            //Party support.
            if (Affair.isInParty(player1)) {
                //Dont update them each indiviually.
                continue;
            }

            //The player1 team on their scoreboard.
            Team team = getPlayerTeam(getPlayerScoreboardObject(player1), player);
            team.setPrefix(guild + color);
            team.setSuffix(ChatColor.AQUA + " [Lvl. " + playerLevel + "]");
            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
        }

        for (Party party : Affair.getParties()) {
        	// Update the scoreboards to show levels.
            Scoreboard scoreboard = party.getScoreboard();
            Team team = getPlayerTeam(scoreboard, player);
            team.setPrefix(guild + color);
            team.setSuffix(ChatColor.AQUA + " [Lvl. " + playerLevel + "]");
            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
        }

        Team team = getPlayerTeam(mainScoreboard, player);
        team.setPrefix(guild + color);
        team.setSuffix(ChatColor.AQUA + " [Lvl. " + playerLevel + "]");
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

    }

    public void registerHealth(Scoreboard scoreboard) {
        Objective objective = scoreboard.getObjective("playerScoreboard") != null ? scoreboard.getObjective("playerScoreboard") : scoreboard.registerNewObjective("playerScoreboard", "playerScoreboard");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        objective.setDisplayName(ChatColor.RED.toString() + "❤");
    }

    public void setCurrentPlayerLevels(Scoreboard scoreboard) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player1);
            if (wrapper == null) {
                continue;
            }
            int level = wrapper.getLevel();

            Team team = getPlayerTeam(scoreboard, player1);
            ChatColor chatColor = wrapper.getAlignment().getColor();
            if (Rank.isTrialGM(player1))
                chatColor = ChatColor.AQUA;
            
            String guild = "";
            GuildWrapper guildWrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player1.getUniqueId());
            if (guildWrapper != null) {
                String clanTag = guildWrapper.getTag();
//                GuildDatabase.getAPI().getTagOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, player1.getUniqueId()).toString());
                guild = ChatColor.translateAlternateColorCodes('&', ChatColor.RESET + "[" + clanTag + ChatColor.RESET + "] ");
            }
            team.setPrefix(guild + chatColor);
            team.setSuffix(ChatColor.AQUA + " [Lvl. " + level + "]");

            Rank.PlayerRank rank = Rank.getRank(player1);
            player1.setPlayerListName(rank.getChatColor() + player1.getName());
            if (!team.hasEntry(player1.getName())) {
                team.addEntry(player1.getName());
            }
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
            Team team = scoreboard.registerNewTeam(player.getName());
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            return team;
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
            Team mainTeam = mainScoreboard.registerNewTeam(name);
            mainTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        if (scoreboard.getTeam(name) == null) {
            Team scTeam = scoreboard.registerNewTeam(name);
            scTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            return scTeam;
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