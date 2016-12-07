package net.dungeonrealms.bukkit.player;

import net.dungeonrealms.bukkit.BukkitCore;
import net.dungeonrealms.bukkit.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Evoltr on 12/7/2016.
 */
public class PlayerManager implements Listener {

    private BukkitCore plugin;

    private String FAILED_LOAD_PLAYER = "§c[WARN] Failed to load your player. Please report this on the forums if this keeps happening!";

    private HashMap<String, NetworkPlayer> players = new HashMap<>();

    public PlayerManager(BukkitCore plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public NetworkPlayer getPlayer(Player player) {
        return getPlayer(player.getName());
    }

    public NetworkPlayer getPlayer(String name) {
        return players.get(name.toLowerCase());
    }

    public NetworkPlayer getPlayerByUUID(String uuid) {
        for (NetworkPlayer player : getOnlinePlayers()) {
            if (player.getUUID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public List<NetworkPlayer> getOnlinePlayers() {
        List<NetworkPlayer> players = Bukkit.getOnlinePlayers().stream().map((Function<Player, NetworkPlayer>) this::getPlayer).collect(Collectors.toList());

        return players;
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        try {
            NetworkPlayer player = plugin.getDB().loadPlayerByName(event.getName());

            if (player == null) {
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                event.setKickMessage(FAILED_LOAD_PLAYER);
                return;
            }

            players.put(event.getName().toLowerCase(), player);
        } catch (Exception e) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(FAILED_LOAD_PLAYER);
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        NetworkPlayer player = getPlayer(event.getPlayer());

        if (player == null) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(FAILED_LOAD_PLAYER);
        }

        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();

        // Premium players may join full servers.
        if (onlinePlayers >= maxPlayers) {

            if (player.getRank().getID() >= Rank.SUB.getID()) {
                event.setResult(PlayerLoginEvent.Result.ALLOWED);
            } else {
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.setKickMessage("§cThis server is full. Purchase premium to join full servers.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        NetworkPlayer player = getPlayer(event.getPlayer());
        players.remove(player.getName().toLowerCase());
    }
}
