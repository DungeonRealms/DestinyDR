package net.dungeonrealms.lobby.misc.ghost;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GhostFactory {
    /**
     * Team of ghosts and people who can see ghosts.
     */
    private static final String GHOST_TEAM_NAME = "Ghosts";
    private static final long UPDATE_DELAY = 20L;

    // No players in the ghost factory
    private static final OfflinePlayer[] EMPTY_PLAYERS = new OfflinePlayer[0];
    private Team ghostTeam;

    // Task that must be cleaned up
    private BukkitTask task;
    private boolean closed;

    // Players that are actually ghosts
    private Set<String> ghosts = new HashSet<String>();

    public GhostFactory(Plugin plugin) {
        // Initialize
        createTask(plugin);
        createGetTeam();
    }

    private void createGetTeam() {
        Scoreboard board = Bukkit.getServer().getScoreboardManager().getMainScoreboard();

        ghostTeam = board.getTeam(GHOST_TEAM_NAME);

        // Create a new ghost team if needed
        if (ghostTeam == null) {
            ghostTeam = board.registerNewTeam(GHOST_TEAM_NAME);
        }
        // Thanks to Rprrr for noticing a bug here
        ghostTeam.setCanSeeFriendlyInvisibles(true);
    }

    private void createTask(Plugin plugin) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                for (OfflinePlayer member : getMembers()) {
                    Player player = member.getPlayer();

                    if (player != null) {
                        // Update invisibility effect
                        setGhost(player, isGhost(player));
                    } else {
                        ghosts.remove(member.getName());
                        ghostTeam.removePlayer(member);
                    }
                }
            }
        }, UPDATE_DELAY, UPDATE_DELAY);
    }

    /**
     * Remove all existing player members and ghosts.
     */
    public void clearMembers() {
        if (ghostTeam != null) {
            for (OfflinePlayer player : getMembers()) {
                ghostTeam.removePlayer(player);
            }
        }
    }

    /**
     * Add the given player to this ghost manager. This ensures that it can see ghosts, and later become one.
     *
     * @param player - the player to add to the ghost manager.
     */
    public void addPlayer(Player player) {
        validateState();
        if (!ghostTeam.hasPlayer(player)) {
            ghostTeam.addPlayer(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
        }
    }

    /**
     * Determine if the given player is tracked by this ghost manager and is a ghost.
     *
     * @param player - the player to test.
     * @return TRUE if it is, FALSE otherwise.
     */
    public boolean isGhost(Player player) {
        return player != null && hasPlayer(player) && ghosts.contains(player.getName());
    }

    /**
     * Determine if the current player is tracked by this ghost manager, or is a ghost.
     *
     * @param player - the player to check.
     * @return TRUE if it is, FALSE otherwise.
     */
    public boolean hasPlayer(Player player) {
        validateState();
        return ghostTeam.hasPlayer(player);
    }

    /**
     * Set wheter or not a given player is a ghost.
     *
     * @param player  - the player to set as a ghost.
     * @param isGhost - TRUE to make the given player into a ghost, FALSE otherwise.
     */
    public void setGhost(Player player, boolean isGhost) {
        // Make sure the player is tracked by this manager
        if (!hasPlayer(player))
            addPlayer(player);

        if (isGhost) {
            ghosts.add(player.getName());
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
        } else if (!isGhost) {
            ghosts.remove(player.getName());
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    /**
     * Remove the given player from the manager, turning it back into the living and making it unable to see ghosts.
     *
     * @param player - the player to remove from the ghost manager.
     */
    public void removePlayer(Player player) {
        validateState();
        if (ghostTeam.removePlayer(player)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    /**
     * Retrieve every ghost currently tracked by this manager.
     *
     * @return Every tracked ghost.
     */
    public OfflinePlayer[] getGhosts() {
        validateState();
        Set<OfflinePlayer> players = new HashSet<OfflinePlayer>(ghostTeam.getPlayers());

        // Remove all non-ghost players
        for (Iterator<OfflinePlayer> it = players.iterator(); it.hasNext(); ) {
            if (!ghosts.contains(it.next().getName())) {
                it.remove();
            }
        }
        return toArray(players);
    }

    /**
     * Retrieve every ghost and every player that can see ghosts.
     *
     * @return Every ghost or every observer.
     */
    public OfflinePlayer[] getMembers() {
        validateState();
        return toArray(ghostTeam.getPlayers());
    }

    private OfflinePlayer[] toArray(Set<OfflinePlayer> players) {
        if (players != null) {
            return players.toArray(new OfflinePlayer[0]);
        } else {
            return EMPTY_PLAYERS;
        }
    }

    public void close() {
        if (!closed) {
            task.cancel();
            ghostTeam.unregister();
            closed = true;
        }
    }

    public boolean isClosed() {
        return closed;
    }

    private void validateState() {
        if (closed) {
            throw new IllegalStateException("Ghost factory has closed. Cannot reuse instances.");
        }
    }

}