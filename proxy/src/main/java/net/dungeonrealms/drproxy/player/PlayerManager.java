package net.dungeonrealms.drproxy.player;

import net.dungeonrealms.drproxy.DRProxy;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Evoltr on 11/30/2016.
 */
public class PlayerManager implements Listener {

    private DRProxy plugin;

    private String FAILED_LOAD_PLAYER = ChatColor.RED + "Failed to load your player data";

    private HashMap<String, NetworkPlayer> players = new HashMap<>();

    public PlayerManager(DRProxy plugin) {
        this.plugin = plugin;
        this.plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    public NetworkPlayer getPlayer(ProxiedPlayer player) {
        return getPlayer(player.getName());
    }

    public NetworkPlayer getPlayer(String name) {
        return players.get(name.toLowerCase());
    }

    public List<NetworkPlayer> getOnlinePlayers() {
        List<NetworkPlayer> players = ProxyServer.getInstance().getPlayers().stream().map(this::getPlayer).collect(Collectors.toList());

        return players;
    }

    @EventHandler
    public void onLogin(final LoginEvent event) {
        final String name = event.getConnection().getName();
        final String uuid = event.getConnection().getUUID();
        final String ip = event.getConnection().getAddress().getAddress().toString().substring(1);

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            try {
                // Create the player data if they have never played before.
                try {
                    plugin.getDatabase().createPlayer(uuid, name, ip);
                    DRProxy.log("Successfully created player data for " + name);
                } catch (Exception e) {
                    DRProxy.log("Failed to create player data for " + name);
                }

                NetworkPlayer player = plugin.getDatabase().loadPlayer(uuid);

                if (player == null) {
                    event.setCancelled(true);
                    event.setCancelReason(FAILED_LOAD_PLAYER);
                    event.completeIntent(plugin);
                    return;
                }
                players.put(name.toLowerCase(), player);
            } catch (Exception e) {
                event.setCancelled(true);
                event.setCancelReason(FAILED_LOAD_PLAYER);
            }

            event.completeIntent(plugin);
        });

        event.registerIntent(plugin);
    }

}
