package net.dungeonrealms.proxy.handle.connection;

import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.awt.handler.BungeeHandler;
import net.dungeonrealms.proxy.DungeonBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Iterator;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ConnectionHandler implements BungeeHandler.ListeningHandler {
    @Override
    public void prepare() {
        DungeonBungee.getDungeonBungee().getProxy().getPluginManager().registerListener(DungeonBungee.getDungeonBungee(), this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerConnect(ServerConnectEvent event) {
        if (DungeonBungee.getDungeonBungee().getNetworkProxy().isMaintenance()) {
            if (!DungeonBungee.getDungeonBungee().getNetworkProxy().getWhitelist().contains(event.getPlayer().getName())) {
                event.getPlayer().disconnect(ChatColor.translateAlternateColorCodes('&', "&6DungeonRealms &cis undergoing maintenance\nPlease refer to www.dungeonrealms.net for status updates"));
                event.setCancelled(true);
            }
        } else {
            // Connect them to a lobby
            if ((event.getPlayer().getServer() == null) || event.getTarget().getName().equals("Lobby")) {
                Iterator<ServerInfo> optimalLobbies = DungeonBungee.getDungeonBungee().getNetworkProxy().getProxyLobby().bufferLobbies().iterator();
                while (optimalLobbies.hasNext()) {
                    ServerInfo target = optimalLobbies.next();

                    if (!(event.getPlayer().getServer() != null && event.getPlayer().getServer().getInfo().equals(target))) {
                        try {
                            event.setTarget(target);
                        } catch (Exception e) {
                            if (!optimalLobbies.hasNext())
                                event.getPlayer().disconnect(ChatColor.RED + "No lobby session found, please retry");
                            else continue;
                        }
                        break;
                    } else if (!optimalLobbies.hasNext()) {
                        event.getPlayer().disconnect(ChatColor.RED + "No lobby session found, please retry");
                        return;
                    }

                }
            }
        }
    }

    @EventHandler
    public void onConnectionEstablish(PreLoginEvent event) {
        DungeonBungee.getDungeonBungee().getProxy().getPlayers().stream().filter(p -> {
            return p.getUniqueId().equals(event.getConnection().getUniqueId()); // Check if an already connected player has an incoming connection
        }).forEach(p -> {
            if (p != null) {
                // Kick the other account from the network to let the player login
                p.disconnect(ChatColor.RED + "Another player with your account has logged into the network!");
            }
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();

        ping.setDescription(ChatColor.translateAlternateColorCodes('&', DungeonBungee.getDungeonBungee().getNetworkProxy().isMaintenance() ? Constants.MAINTENANCE_MOTD : Constants.MOTD));
        ping.setPlayers(new ServerPing.Players(Constants.PLAYER_SLOTS, ping.getPlayers().getOnline(), ping.getPlayers().getSample()));
    }
}
