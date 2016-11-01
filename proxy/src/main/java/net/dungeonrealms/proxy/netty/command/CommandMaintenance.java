package net.dungeonrealms.proxy.netty.command;

import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.proxy.DungeonBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/25/2016
 *
 * Some changes have been made by Giovanni.
 */
public class CommandMaintenance extends Command
{

    public CommandMaintenance()
    {
        super("maintenancemode", null, "mm", "whitelist");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (sender instanceof ProxiedPlayer && !Arrays.asList(Constants.DEVELOPERS).contains(sender.getName())) return;

        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.RED + "/mm on/off/add/remove");
            return;
        }

        if (args[0].equalsIgnoreCase("on"))
        {
            ProxyServer.getInstance().broadcast(ChatColor.translateAlternateColorCodes('&', "&6DungeonRealms &cis about to go under maintenance..."));
            ProxyServer.getInstance().broadcast(ChatColor.translateAlternateColorCodes('&', "&cPlease visit &nwww.dungeonrealms.net&c for status updates"));

            ProxyServer.getInstance().getScheduler().schedule(DungeonBungee.getDungeonBungee(), () -> {
                DungeonBungee.getDungeonBungee().getNetworkProxy().setMaintenance(true);

                DungeonBungee.getDungeonBungee().getProxy().getPlayers().stream().filter(player -> {
                    return ShardInfo.getByPseudoName(player.getServer().getInfo().getName()) == null;
                }).forEach(player -> {
                    if (!Arrays.asList(Constants.DEVELOPERS).contains(player.getName()))
                    {
                        player.disconnect(ChatColor.translateAlternateColorCodes('&', "&6DungeonRealms &cis undergoing maintenance\nPlease refer to www.dungeonrealms.net for status updates"));
                    }
                });
                DungeonBungee.getDungeonBungee().getNetworkProxy().sendGlobalPacket("Stop");
            }, 5, TimeUnit.SECONDS);

        } else
        {
            if (args[0].equalsIgnoreCase("off"))
            {
                DungeonBungee.getDungeonBungee().getNetworkProxy().setMaintenance(false);
                sender.sendMessage(ChatColor.GRAY + "Maintenance mode disabled.");
            } else
            {
                if (args[0].equalsIgnoreCase("add"))
                {
                    DungeonBungee.getDungeonBungee().getNetworkProxy().getWhitelist().add(args[1]);
                    sender.sendMessage(ChatColor.GRAY + args[1] + " has been added to whitelist.");
                } else
                {
                    if (args[0].equalsIgnoreCase("remove"))
                    {
                        DungeonBungee.getDungeonBungee().getNetworkProxy().getWhitelist().remove(args[1]);
                        sender.sendMessage(ChatColor.GRAY + args[1] + " has been removed from whitelist.");
                    } else sender.sendMessage(ChatColor.RED + "/mm on/off/add/remove");
                }
            }
        }
    }
}
