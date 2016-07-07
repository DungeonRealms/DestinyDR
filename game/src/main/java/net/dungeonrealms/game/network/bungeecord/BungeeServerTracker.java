package net.dungeonrealms.game.network.bungeecord;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.network.BungeeServerInfo;
import net.dungeonrealms.network.PingResponse;
import net.dungeonrealms.network.ping.ServerPinger;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BungeeServerTracker {

    private static Map<String, BungeeServerInfo> trackedServers = new ConcurrentHashMap<>();
    private static int taskID = -1;

    public static void resetTrackedServers() {
        trackedServers.clear();
    }

    public static void track(String server) {
        if (!trackedServers.containsKey(server)) {
            BungeeServerInfo info = new BungeeServerInfo(server);
            trackedServers.put(server, info);

            NetworkAPI.getInstance().askPlayerCount(server);
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

    public static int getPlayersOnline(String server) {
        BungeeServerInfo info = trackedServers.get(server);
        if (info != null) {

            info.updateLastRequest();
            return info.getOnlinePlayers();
        } else {
            // It was not tracked, add it.
            track(server);
            return 0;
        }
    }

    public static int getTrackedSize() {
        return trackedServers.size();
    }

    public static Map<String, BungeeServerInfo> getTrackedServers() {

        return trackedServers;
    }

    public static void startTask(int refreshSeconds) {
        if (taskID != -1) Bukkit.getScheduler().cancelTask(taskID);

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<String, DungeonRealms.ShardInfo> entry : DungeonRealms.getInstance().DR_SHARDS.entrySet()) {

                    BungeeServerInfo serverInfo = getOrCreateServerInfo(entry.getKey());
                    boolean displayOffline = false;

                    try {
                        PingResponse data = new SpigotPingResponse(ServerPinger.fetchData(entry.getValue().getServerAddress(), 500));

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
                        Utils.log.warning("Couldn't fetch data from " + entry.getKey() + "(" + entry.getValue().toString() + "): unknown host address.");
                        displayOffline = true;
                    } catch (IOException e) {
                        displayOffline = true;
                    } catch (Exception e) {
                        displayOffline = true;
                        Utils.log.warning("Couldn't fetch data from " + entry.getKey() + "(" + entry.getValue().toString() + "), unhandled exception: " + e.toString());
                    }

                    if (displayOffline) {
                        serverInfo.setOnline(false);
                        serverInfo.setOnlinePlayers(0);
                        serverInfo.setMaxPlayers(0);
                        serverInfo.setMotd(DungeonRealms.getInstance().shardid);
                    }
                }
            }
        }.runTaskAsynchronously(DungeonRealms.getInstance()), 1, refreshSeconds * 20);
    }


}
