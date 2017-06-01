package net.dungeonrealms.proxy;

import com.esotericsoftware.minlog.Log;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.util.AsyncUtils;
import net.dungeonrealms.common.network.ServerAddress;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.ping.PingResponse;
import net.dungeonrealms.common.network.ping.ServerPinger;
import net.dungeonrealms.network.GameClient;
import net.dungeonrealms.proxy.command.CommandAlert;
import net.dungeonrealms.proxy.command.CommandMaintenance;
import net.dungeonrealms.proxy.command.CommandSetMOTD;
import net.dungeonrealms.proxy.listener.NetworkClientListener;
import net.dungeonrealms.proxy.listener.ProxyChannelListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Class written by APOLLOSOFTWARE.IO on 5/31/2016
 */

public class DungeonRealmsProxy extends Plugin implements Listener {

    /**
     * Constants fixed
     */
    private static DungeonRealmsProxy instance;

    public static DungeonRealmsProxy getInstance() {
        return instance;
    }

    @Getter
    private static GameClient client;

    private boolean MAINTENANCE_MODE = false;

    private final File BUNGEE_CONFIG_FILE = new File(new File(System.getProperty("user.dir")), "config.yml");

    private List<String> WHITELIST = new ArrayList<>();

    //We need to track all vanished players at the bungee level..
    private Set<UUID> vanishedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        Constants.build();
        AsyncUtils.threadCount = Runtime.getRuntime().availableProcessors();
        AsyncUtils.pool = Executors.newFixedThreadPool(AsyncUtils.threadCount);
        instance = this;
        getLogger().info("DungeonRealmsProxy onEnable() ... STARTING UP");

        this.getProxy().getPluginManager().registerListener(this, ProxyChannelListener.getInstance());
        this.getProxy().getPluginManager().registerListener(this, this);
        this.getProxy().getPluginManager().registerListener(this, StaffSecurity.getInstance());

        this.getProxy().getPluginManager().registerCommand(this, new CommandSetMOTD());
        this.getProxy().getPluginManager().registerCommand(this, new CommandMaintenance());
        this.getProxy().getPluginManager().registerCommand(this, new CommandAlert());

        try {
            // SET DEFAULT
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(BUNGEE_CONFIG_FILE);

            if (!configuration.getKeys().contains("maintenance_mode"))
                setMaintenanceMode(MAINTENANCE_MODE);
            else MAINTENANCE_MODE = configuration.getBoolean("maintenance_mode");


            if (configuration.getKeys().contains("whitelist"))
                WHITELIST = (List<String>) configuration.getList("whitelist");

        } catch (IOException e) {
            e.printStackTrace();
        }

        getLogger().info("Connecting to DungeonRealms master server...");
        client = new GameClient();

        try {
            client.connect();
            Log.set(Log.LEVEL_INFO);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new NetworkClientListener().startInitialization(client);

        // REGISTER DUNGEON REALM SHARDS //
        Arrays.asList(ShardInfo.values()).stream().forEach(info -> {
                    ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(info.getPseudoName(), new InetSocketAddress(info.getAddress().getAddress(), info.getAddress().getPort()), "", false);
                    ProxyServer.getInstance().getServers().put(info.getPseudoName(), serverInfo);
                }
        );
    }

    public void onDisable() {
        try {
            // SAVE WHITELIST //
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(BUNGEE_CONFIG_FILE);
            configuration.set("whitelist", WHITELIST);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, BUNGEE_CONFIG_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to send player to an optimal shard.
     *
     * @param uuid        Target
     * @param populated   Find populated shard?
     * @param subscriber  Subscriber
     * @param sendToLobby Send them to lobby if they have failed to connect to a shard
     */
    public void LoadBalancer(UUID uuid, boolean populated, boolean subscriber, boolean sendToLobby) {
        Log.info("Called load balancer");

        getProxy().getScheduler().runAsync(DungeonRealmsProxy.getInstance(), () -> {
            ProxiedPlayer player = getProxy().getPlayer(uuid);
            Iterator<ServerInfo> optimalShardFinder = getOptimalShards(subscriber, populated).iterator();
            while (optimalShardFinder.hasNext()) {
                ServerInfo target = optimalShardFinder.next();

                try {
                    PingResponse ping = null;
                    boolean isOnline = true;

                    try {
                        ping = ServerPinger.fetchData(new ServerAddress(target.getAddress().getHostName(), target.getAddress().getPort()), 20);
                    } catch (Exception e) {
                        isOnline = false;
                    }

                    if (!isOnline || ping.getDescription().getText().contains("offline")) {

                        if (!optimalShardFinder.hasNext()) {
                            // CONNECT THEM TO LOBBY LOAD BALANCER //
                            if (sendToLobby)
                                player.connect(getProxy().getServerInfo("Lobby"));
                            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Unable to find a session for you.");
                            return;
                        }

                        continue;
                    }
                } catch (Exception e) {

                    if (!optimalShardFinder.hasNext()) {
                        // CONNECT THEM TO LOBBY LOAD BALANCER //
                        if (sendToLobby)
                            player.connect(getProxy().getServerInfo("Lobby"));
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Unable to find a session for you.");
                        return;
                    }

                    continue;
                }

                if (target.canAccess(player) && !(player.getServer() != null && player.getServer().getInfo().equals(target))) {
                    player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Moving your current session...");
                    player.connect(target);
                    break;
                } else if (!optimalShardFinder.hasNext()) {
                    // CONNECT THEM TO LOBBY LOAD BALANCER //
                    if (sendToLobby)
                        player.connect(getProxy().getServerInfo("Lobby"));
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Unable to find a session for you.");
                    return;
                }
            }
            if (sendToLobby) {
                player.connect(getProxy().getServerInfo("Lobby"));
            }
        });
    }

    public List<ServerInfo> getOptimalShards(boolean populated, boolean isSub) {
        List<ServerInfo> servers = new ArrayList<>();

        for (ShardInfo shardInfo : ShardInfo.values()) {
            // We want to only put them on a US as they may fail the criteria for another shard.
            // They are free to join another shard once connected.

            String name = shardInfo.getPseudoName();
            if ((name.startsWith("us") && !name.equalsIgnoreCase("us0")) || (isSub && name.startsWith("sub")))
                servers.add(getProxy().getServerInfo(name));
        }

        Collections.sort(servers, (o1, o2) -> ((o1.getPlayers().size())) - (o2.getPlayers().size()));

        if (populated)
            Collections.reverse(servers);

        return servers;
    }

    public List<String> getWhitelist() {
        return WHITELIST;
    }


    private boolean isWhitelisted(String name) {
        return WHITELIST.contains(name) || Arrays.asList(Constants.DEVELOPERS).contains(name);
    }

    public void setMaintenanceMode(boolean value) {
        MAINTENANCE_MODE = value;

        try {
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(BUNGEE_CONFIG_FILE);
            configuration.set("maintenance_mode", MAINTENANCE_MODE);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, BUNGEE_CONFIG_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onMaintenanceMode(ServerConnectEvent event) {
        if (MAINTENANCE_MODE && !isWhitelisted(event.getPlayer().getName())) {
            event.getPlayer().disconnect(ChatColor.translateAlternateColorCodes('&', "&6DungeonRealms &cis undergoing maintenance\nPlease refer to www.dungeonrealms.net for status updates"));
            event.setCancelled(true);
            return;
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLobbyConnect(ServerConnectEvent event) {
        if ((event.getPlayer().getServer() == null) ||
                // THIS IS CONSIDERED THE LOBBY LOAD BALANCE SERVER //
                event.getTarget().getName().equals("Lobby")
                ) {
            Iterator<ServerInfo> optimalLobbies = getOptimalLobbies().iterator();

            while (optimalLobbies.hasNext()) {

                ServerInfo target = optimalLobbies.next();

                if (!(event.getPlayer().getServer() != null && event.getPlayer().getServer().getInfo().equals(target))) {
                    try {
                        event.setTarget(target);
                    } catch (Exception e) {
                        if (!optimalLobbies.hasNext())
                            event.getPlayer().disconnect(ChatColor.RED + "Could not find a lobby for you.");
                        else continue;
                    }

                    break;
                } else if (!optimalLobbies.hasNext()) {
                    event.getPlayer().disconnect(ChatColor.RED + "Could not find a lobby for you.");
                    return;
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent ev) {
        if (!(ev.getCursor().startsWith("/") || ev.getCursor().startsWith("@")))
            return;

        String partialPlayerName = ev.getCursor().toLowerCase();

        int lastSpaceIndex = partialPlayerName.lastIndexOf(' ');
        if (lastSpaceIndex >= 0) partialPlayerName = partialPlayerName.substring(lastSpaceIndex + 1);

        for (ProxiedPlayer p : getProxy().getPlayers()) {
            if (p.getName().toLowerCase().startsWith(partialPlayerName) && !this.vanishedPlayers.contains(p.getUniqueId())) ev.getSuggestions().add(p.getName());
        }
    }


    @EventHandler
    public void onPlayerLeave(PlayerDisconnectEvent event){
        //Remove them from the list if they disconnect.
        this.vanishedPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onProxyConnection(PreLoginEvent event) {
        if (MAINTENANCE_MODE && !isWhitelisted(event.getConnection().getName())) {
            event.setCancelReason(ChatColor.translateAlternateColorCodes('&', "&6DungeonRealms &cis undergoing maintenance\nPlease refer to www.dungeonrealms.net for status updates"));
            event.setCancelled(true);
        }

        // DUPE GLITCH FIX //
        getProxy().getPlayers().stream().filter(p -> p.getUniqueId().equals(event.getConnection().getUniqueId())).forEach(p -> {
            if (p != null)
                p.disconnect(ChatColor.RED + "Another player with your account has logged into the server!");

            event.setCancelReason(ChatColor.RED + "Another player with your account has logged into the server!");
            event.setCancelled(true);
        });
    }


    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();

        int players = ping.getPlayers().getOnline();
        ServerPing.PlayerInfo[] sample = ping.getPlayers().getSample();

        ping.setDescription(ChatColor.translateAlternateColorCodes('&', !MAINTENANCE_MODE ? Constants.MOTD : Constants.MAINTENANCE_MOTD));
        ping.setPlayers(new ServerPing.Players(Constants.PLAYER_SLOTS, players, sample));
    }


    public List<ServerInfo> getOptimalLobbies() {
        List<ServerInfo> servers = getProxy().getServers().values().stream().filter(server -> server.getName().contains("Lobby")).collect(Collectors.toList());
        Collections.sort(servers, (o1, o2) -> o1.getPlayers().size() - o2.getPlayers().size());
        return servers;
    }

    public void hidePlayer(UUID uuid){
        if(!this.vanishedPlayers.contains(uuid))this.vanishedPlayers.add(uuid);
    }

    public void unhidePlayer(UUID uuid){
        this.vanishedPlayers.remove(uuid);
    }

    public void sendNetworkPacket(String task, String... contents) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(task);

        for (String s : contents)
            out.writeUTF(s);

        getClient().sendTCP(out.toByteArray());
    }

}