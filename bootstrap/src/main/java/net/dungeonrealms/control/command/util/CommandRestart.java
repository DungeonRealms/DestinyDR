package net.dungeonrealms.control.command.util;

import net.dungeonrealms.control.command.NetworkCommand;
import net.dungeonrealms.control.server.Server;
import net.dungeonrealms.control.server.types.GameServer;
import net.dungeonrealms.control.server.types.ProxyServer;
import net.dungeonrealms.control.utils.UtilLogger;

/**
 * Created by Evoltr on 12/4/2016.
 */
public class CommandRestart extends NetworkCommand {

    public CommandRestart() {
        super("restart", "Restart a server/proxy");
    }

    @Override
    public void onCommand(String[] args) {

        if (args.length <= 0) {
            UtilLogger.info("Usage: restart <Server/Type>");
            return;
        }

        // Get the server with the name they entered.
        Server server = getServer(args[0]);

        if (server != null) {
            restartServer(server);
        } else if (args[0].equalsIgnoreCase("proxy")) {
            UtilLogger.info("Restarting proxies...");

            // Restart all proxy servers.
            getDRControl().getServerManager().getProxyServers().forEach(Server::restart);
        } else if (args[0].equalsIgnoreCase("all")) {
            UtilLogger.info("Restarting entire network...");

            // Restart all proxy servers.
            getDRControl().getServerManager().getProxyServers().forEach(Server::restart);

            // Restart all game servers.
            getDRControl().getServerManager().getGameServers().forEach(Server::restart);
        } else {
            // Get the server type they entered.
            GameServer.ServerType type = getType(args[0]);

            if (type == null) {
                UtilLogger.info("Unknown server type: " + args[0]);
                return;
            }

            // Restart all servers of this type.
            getDRControl().getServerManager().getGameServers(type).forEach(Server::restart);

            UtilLogger.info("Restarting " + type.toString() + " servers...");
        }
    }

    public void restartServer(Server server) {
        if (server.isOnline()) {
            UtilLogger.info("Restarting " + server.getName() + "...");
        } else {
            UtilLogger.info(server.getName() + " is not online.");
        }

        server.restart();
    }

    public Server getServer(String name) {
        ProxyServer proxy = getDRControl().getServerManager().getProxyServer(name);
        GameServer server = getDRControl().getServerManager().getGameServer(name);

        return proxy != null ? proxy : server;
    }

    public GameServer.ServerType getType(String name) {
        for (GameServer.ServerType type : GameServer.ServerType.values()) {
            if (type.toString().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

}
