package net.dungeonrealms.game.handler;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.mastery.GamePlayer;
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

    private static ScoreboardHandler instance = null;

    public static ScoreboardHandler getInstance() {
        if (instance == null) {
            instance = new ScoreboardHandler();
        }
        return instance;
    }

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

        Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {

            ChatColor color = chatColor;
            String rank = Rank.getInstance().getRank(player.getUniqueId());
            //Only need to check if GM one time..
            if (Rank.isTrialGMRank(rank)) {
                color = ChatColor.AQUA;
            }

            String guild = "";
            if (!GuildDatabase.getAPI().isGuildNull(player.getUniqueId())) {
                String clanTag = GuildDatabase.getAPI().getTagOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString());
                guild = ChatColor.translateAlternateColorCodes('&', ChatColor.RESET + "[" + clanTag + ChatColor.RESET + "] ");
            }

            //Async please thanks.
            ChatColor rankColor = Rank.colorFromRank(rank);

            //Final cause threading is fun.
            final String guildName = guild;
            final ChatColor newColor = color;
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                //Do this once.
                player.setPlayerListName(rankColor + player.getName());
                for (Player player1 : Bukkit.getOnlinePlayers()) {

                    //Party support.
                    if (Affair.isInParty(player1)) {
                        //Dont update them each indiviually.
                        continue;
                    }

                    //The player1 team on their scoreboard.
                    Team team = getPlayerTeam(getPlayerScoreboardObject(player1), player);
                    team.setPrefix(guildName + newColor);
                    team.setSuffix(ChatColor.AQUA + " [Lvl. " + playerLevel + "]");
                    if (!team.hasEntry(player.getName())) {
                        team.addEntry(player.getName());
                    }
                }

                for (Party party : Affair.getParties()) {
                    //Update the party scoreboards with this persons new level.
//                updateCurrentPlayerLevel(player, party.getPartyScoreboard());
                    Scoreboard scoreboard = party.getScoreboard();
                    Team team = getPlayerTeam(scoreboard, player);
                    team.setPrefix(guildName + newColor);
                    team.setSuffix(ChatColor.AQUA + " [Lvl. " + playerLevel + "]");
                    if (!team.hasEntry(player.getName())) {
                        team.addEntry(player.getName());
                    }
                }

                Team team = getPlayerTeam(mainScoreboard, player);
                team.setPrefix(guildName + newColor);
                team.setSuffix(ChatColor.AQUA + " [Lvl. " + playerLevel + "]");
                if (!team.hasEntry(player.getName())) {
                    team.addEntry(player.getName());
                }
            });

        });
    }

   /* public void updateCurrentPlayerLevel(Player toSetFor, Scoreboard scoreboard) {
        GamePlayer gamePlayer = GameAPI.getGamePlayer(toSetFor);
        if (gamePlayer == null) return;

        int level = gamePlayer.getStats().getLevel();

        Team team = getPlayerTeam(scoreboard, toSetFor);
        ChatColor chatColor = GameAPI.getGamePlayer(toSetFor).getPlayerAlignment().getAlignmentColor();
        if (Rank.isGM(toSetFor)) {
            chatColor = ChatColor.AQUA;
        }
        String guild = "";
        if (!GuildDatabase.getAPI().isGuildNull(toSetFor.getUniqueId())) {
            String clanTag = GuildDatabase.getAPI().getTagOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, toSetFor.getUniqueId()).toString());
            guild = ChatColor.translateAlternateColorCodes('&', ChatColor.RESET + "[" + clanTag + ChatColor.RESET + "] ");
        }
        team.setPrefix(guild + chatColor);
        team.setSuffix(ChatColor.AQUA + " [Lvl. " + level + "]");
        toSetFor.setPlayerListName(Rank.colorFromRank(Rank.getInstance().getRank(toSetFor.getUniqueId())) + toSetFor.getName());
        if (!team.hasEntry(toSetFor.getName())) {
            team.addEntry(toSetFor.getName());
        }
    }*/

    public void registerHealth(Scoreboard scoreboard) {
        Objective objective = scoreboard.getObjective("playerScoreboard") != null ? scoreboard.getObjective("playerScoreboard") : scoreboard.registerNewObjective("playerScoreboard", "playerScoreboard");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        objective.setDisplayName(ChatColor.RED.toString() + "❤");
    }

    public void setCurrentPlayerLevels(Scoreboard scoreboard) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            GamePlayer gamePlayer = GameAPI.getGamePlayer(player1);
            if (gamePlayer == null) {
                continue;
            }
            int level = gamePlayer.getStats().getLevel();

            Team team = getPlayerTeam(scoreboard, player1);
            ChatColor chatColor = GameAPI.getGamePlayer(player1).getPlayerAlignment().getAlignmentColor();
            if (Rank.isTrialGM(player1)) {
                chatColor = ChatColor.AQUA;
            }
            String guild = "";
            if (!GuildDatabase.getAPI().isGuildNull(player1.getUniqueId())) {
                String clanTag = GuildDatabase.getAPI().getTagOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, player1.getUniqueId()).toString());
                guild = ChatColor.translateAlternateColorCodes('&', ChatColor.RESET + "[" + clanTag + ChatColor.RESET + "] ");
            }
            team.setPrefix(guild + chatColor);
            team.setSuffix(ChatColor.AQUA + " [Lvl. " + level + "]");
            player1.setPlayerListName(Rank.colorFromRank(Rank.getInstance().getRank(player1.getUniqueId())) + player1.getName());
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
