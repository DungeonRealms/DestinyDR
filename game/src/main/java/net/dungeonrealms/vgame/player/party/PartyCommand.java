package net.dungeonrealms.vgame.player.party;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.packet.party.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Giovanni on 20-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PartyCommand extends BaseCommand
{
    // TODO send packets to master server

    public PartyCommand(String command, String usage, String description, List<String> aliasList)
    {
        super(command, usage, description, aliasList);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if (args.length == 0)
            {
                player.sendMessage(ChatColor.RED + "Usage: /party <invite/join/leave/info/disband>");
                player.sendMessage(ChatColor.RED + "Example usage: /party join Evoltr");
            }
            if (args.length == 1)
            {
                if (args[0].equalsIgnoreCase("disband"))
                {
                    new PacketPartyDisband(player.getName());
                } else if (args[0].equalsIgnoreCase("leave"))
                {
                    new PacketPartyLeave(player.getName());
                }
            }
            if (args.length == 2)
            {
                if (args[0].equalsIgnoreCase("invite"))
                {
                    if (!args[1].isEmpty())
                    {
                        new PacketPartyInvite(player.getName(), args[1]);
                    } else player.sendMessage(ChatColor.RED + "You must specify a player to invite!");
                } else if (args[0].equalsIgnoreCase("info"))
                {
                    if (!args[1].isEmpty())
                    {
                        new PacketPartyInfo(player.getName(), args[1]);
                    } else player.sendMessage(ChatColor.RED + "You must specify a player to view the party of!");
                } else if (args[0].equalsIgnoreCase("join"))
                {
                    if (!args[1].isEmpty())
                    {
                        new PacketPartyJoin(player.getName(), args[1]);
                    } else player.sendMessage(ChatColor.RED + "You must specify a player to join the party of!");
                }
            }
        } else
            sender.sendMessage(ChatColor.RED + "Player command.");
        return false;
    }
}
