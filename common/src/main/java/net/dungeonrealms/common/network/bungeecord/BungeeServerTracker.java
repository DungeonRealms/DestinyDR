package net.dungeonrealms.common.network.bungeecord;

import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.network.PingResponse;
import net.dungeonrealms.common.network.ServerAddress;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.ping.ServerPinger;
import net.dungeonrealms.common.network.ping.type.SpigotPingResponse;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BungeeServerTracker {

    private static Map<String, BungeeServerInfo> trackedServers = new ConcurrentHashMap<>();
    private static int taskID = -1;

    public static void resetTrackedServers() {
        trackedServers.clear();
    }

    public static void track(String server, Consumer<String> requestPlayerCount) {
        if (!trackedServers.containsKey(server)) {
            BungeeServerInfo info = new BungeeServerInfo(server);
            trackedServers.put(server, info);

            requestPlayerCount.accept(server);
            //NetworkAPI.getInstance().askPlayerCount(server);
        }
    }

    public static void untrack(String server) {
        trackedServers.remove(server);
    }

    public static BungeeServerInfo getOrCreateServerInfo(String server) {
        BungeeServerInfo info = trackedServers.get(server);
        if (info == null) {
            info = new BungeeServerInfo(server);
            trackedServers.put(server, info);
        }

        return info;
    }

    public static int getPlayersOnline(String server, Consumer<String> requestPlayerCount) {
        BungeeServerInfo info = trackedServers.get(server);
        if (info != null) {

            info.updateLastRequest();
            return info.getOnlinePlayers();
        } else {
            // It was not tracked, add it.
            track(server, requestPlayerCount);
            return 0;
        }
    }

    public static int getTrackedSize() {
        return trackedServers.size();
    }

    public static Map<String, BungeeServerInfo> getTrackedServers() {

        return trackedServers;
    }

    public static void startTask(Plugin plugin, long refreshSeconds) {
        if (taskID != -1) Bukkit.getScheduler().cancelTask(taskID);

        taskID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, () -> {
            for (ShardInfo shard : ShardInfo.values()) {

                String bungeeName = shard.getPseudoName();
                ServerAddress address = shard.getAddress();

                BungeeServerInfo serverInfo = getOrCreateServerInfo(bungeeName);
                boolean displayOffline = false;

                try {
                    PingResponse data = new SpigotPingResponse(ServerPinger.fetchData(address, 500));

                    if (data.isOnline()) {
                        serverInfo.setOnline(true);
                        serverInfo.setOnlinePlayers(data.getOnlinePlayers());
                        serverInfo.setMaxPlayers(data.getMaxPlayers());
                        serverInfo.setMotd(data.getMotd());
                    } else {
                        displayOffline = true;
                    }
                } catch (SocketTimeoutException e) {
                    displayOffline = true;
                } catch (UnknownHostException e) {
                    Constants.log.warning("Couldn't fetch data from " + bungeeName + "(" + address.toString() + "): unknown host address.");
                    displayOffline = true;
                } catch (IOException e) {
                    displayOffline = true;
                } catch (Exception e) {
                    displayOffline = true;
                    Constants.log.warning("Couldn't fetch data from " + bungeeName + "(" + address.toString() + "), unhandled exception: " + e.toString());
                }

                if (displayOffline) {
                    serverInfo.setOnline(false);
                    serverInfo.setOnlinePlayers(0);
                    serverInfo.setMaxPlayers(0);
                    serverInfo.setMotd("offline");
                }
            }
        }, 0L, refreshSeconds * 20L);
    }


}
