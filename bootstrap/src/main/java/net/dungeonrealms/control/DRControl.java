package net.dungeonrealms.control;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.dungeonrealms.common.awt.database.connection.exception.ConnectionRunningException;
import net.dungeonrealms.common.awt.database.mongo.connection.MongoConnection;
import net.dungeonrealms.control.command.CommandManager;
import net.dungeonrealms.control.config.Configuration;
import net.dungeonrealms.control.database.Database;
import net.dungeonrealms.control.database.cache.DataCache;
import net.dungeonrealms.control.friend.FriendManager;
import net.dungeonrealms.control.netty.ServerInitializer;
import net.dungeonrealms.control.party.PartyManager;
import net.dungeonrealms.control.player.PlayerManager;
import net.dungeonrealms.control.server.ServerManager;
import net.dungeonrealms.control.server.types.ProxyServer;
import net.dungeonrealms.control.utils.UtilLogger;
import net.dungeonrealms.packet.network.PacketPlayerCount;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by Evoltr on 11/15/2016.
 */
public class DRControl {

    private static DRControl instance;
    private long uptime;
    private Channel channel = null;
    private boolean running = true;

    private Database database;
    private MongoConnection mongoConnection;
    private DataCache dataCache;
    private Configuration configuration;

    private PlayerManager playerManager;
    private ServerManager serverManager;
    private PartyManager partyManager;
    private CommandManager commandManager;
    private FriendManager friendManager;

    public DRControl() throws SQLException {
        instance = this;
        uptime = System.currentTimeMillis();

        loadConfig();
        UtilLogger.info("Config is successfully loaded - Starting up DRControl.");
        UtilLogger.info("-----------------------------------------");
        UtilLogger.info("DRControl ALPHA 0.0.1-INDEV is now taking control over your entire network!");
        UtilLogger.info("");
        UtilLogger.info("Custom developed server management and load balancing system.");
        UtilLogger.info("-----------------------------------------");

        // Connect to the database.
        database = new Database(this);
        database.setup();

        // Connect the Mongo to the default Dungeon Realms database, so we can interact w/ player data
        this.mongoConnection = new MongoConnection();
        try {
            this.mongoConnection.runOn("", "dungeonrealms");
        } catch (ConnectionRunningException e) {
            e.printStackTrace();
        }

        // Start the player data cache
        this.dataCache = new DataCache();

        // Setup managers.
        this.playerManager = new PlayerManager(this);
        this.serverManager = new ServerManager(this);
        this.partyManager = new PartyManager(this);
        this.commandManager = new CommandManager(this);
        this.friendManager = new FriendManager(this);
    }

    public static void main(String[] args) throws SQLException {
        new DRControl().run();
    }

    public static DRControl getInstance() {
        return instance;
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            // Create the server bootstrap.
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // Setup the server
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ServerInitializer());

            // Bind to port, for now we use port 8192.
            channel = serverBootstrap.bind(8192).sync().channel();

            // Send the current player count to the proxies every second.
            channel.eventLoop().scheduleAtFixedRate(() -> {
                int onlinePlayers = getServerManager().getOnlinePlayers();
                int maxPlayers = getServerManager().getMaxPlayers();

                // Save the player record in the config.
                if (onlinePlayers > getPlayerRecord()) {
                    getConfiguration().setSetting("player-record", String.valueOf(onlinePlayers));
                }
                for (ProxyServer proxyServer : getServerManager().getProxyServers()) {
                    proxyServer.sendPacket(new PacketPlayerCount(onlinePlayers, maxPlayers));
                }


            }, 0L, 1L, TimeUnit.SECONDS);

            // Listen for user  input and pass to the command manager.
            Scanner scanner = new Scanner(System.in);

            while (isRunning()) {
                try {
                    commandManager.handle(scanner.nextLine());
                } catch (Exception e) {
                    UtilLogger.warn("Error executing command: " + e.getMessage());
                }
            }

            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void shutdown() {
        running = false;
        UtilLogger.critical("DRControl is shutting down.");

        // Exit DRControl
        System.exit(0);
    }

    public void loadConfig() {
        configuration = new Configuration(new File("config.properties"));

        try {
            configuration.load();
        } catch (IOException e) {
            UtilLogger.warn("Failed to load config: " + e.getMessage());
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public Database getDatabase() {
        return database;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public boolean isRunning() {
        return running;
    }

    public String getMOTD() {
        return getConfiguration().getSetting("motd");
    }

    public int getPlayerRecord() {
        return Integer.valueOf(getConfiguration().getSetting("player-record"));
    }

    public long getUptime() {
        return uptime;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public MongoConnection getMongoConnection() {
        return mongoConnection;
    }
}
