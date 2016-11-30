package net.dungeonrealms.control.command.proxy;


import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.command.NetworkCommand;
import net.dungeonrealms.control.server.types.ProxyServer;
import net.dungeonrealms.control.utils.UtilLogger;

import java.util.List;

/**
 * Created by Evoltr on 11/20/2016.
 */
public class CommandProxyList extends NetworkCommand {

    public CommandProxyList() {
        super("proxylist", "Displays a list of loaded proxies");
    }

    @Override
    public void onCommand(String[] args) {
        int page = 1;

        if (args.length >= 1) {
            page = Integer.parseInt(args[0]);
        }

        // Get a list of proxy servers to display
        List<ProxyServer> proxyServerList = DRControl.getInstance().getServerManager().getProxyServers();

        int maxPages = (proxyServerList.size() / 10) + 1;

        // Check the page number is valid.
        if (page < 1 || page > maxPages) {
            UtilLogger.info("Invalid page number: " + page);
            return;
        }

        // Display a header for the page.
        UtilLogger.info("------------- Proxies (Page " + page + "/" + maxPages + ") -------------");

        // Loop through commands.
        for (int x = 0; x < 10; x++) {
            int entry = x + (page - 1) * 9;

            if (entry >= proxyServerList.size()) {
                break;
            }

            // Get the server from the value.
            ProxyServer server = proxyServerList.get(entry);
            String message = (entry + 1) + ". " + server.getName() + " - " + server.getHost() + ": " + server.getPort();

            if (server.isOnline()) {
                message += " | ONLINE | Players: ";
            } else {
                message += " | OFFLINE | Players: ";
            }

            // Add the player count to the message.
            message += server.getPlayers().size() + "/500";

            // Print info to console.
            UtilLogger.info(message);
        }
    }
}
