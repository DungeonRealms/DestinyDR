package net.dungeonrealms.vgame.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.vgame.item.EnumItemRarity;
import net.dungeonrealms.vgame.item.EnumItemTier;
import net.dungeonrealms.vgame.item.EnumItemType;
import net.dungeonrealms.vgame.item.weapon.WeaponItem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Giovanni on 4-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CommandItem extends BaseCommand
{
    public CommandItem(String command, String usage, String description)
    {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if (Rank.isDev(player) || player.isOp())
            {
                // Prepare construction
                EnumItemType itemType;
                EnumItemTier itemTier;
                EnumItemRarity itemRarity;
                String customName;
                boolean tradeable;
                boolean soulbound;

                // Alright..
                if (args.length < 1)
                {
                    player.getInventory().addItem(new WeaponItem(true, false).getItemStack());
                    player.sendMessage(ChatColor.RED + "Custom item generation:");
                    player.sendMessage(ChatColor.RED + "Usage: /item <type> [tier] [rarity] [name] [tradeable] [soulbound]");
                } else
                {
                    if (args.length == 1)
                    {
                        try
                        {
                            itemType = EnumItemType.valueOf(args[0]);
                        } catch (Exception e)
                        {
                            player.sendMessage(ChatColor.RED + "Unknown item type.");
                        }
                    }
                    if (args.length == 2)
                    {
                        try
                        {
                            itemType = EnumItemType.valueOf(args[0]);
                            itemTier = EnumItemTier.valueOf(args[1]);
                        } catch (Exception e)
                        {
                            player.sendMessage(ChatColor.RED + "Arguments are unknown.");
                        }
                    }
                    if (args.length == 3)
                    {
                        try
                        {
                            itemType = EnumItemType.valueOf(args[0]);
                            itemTier = EnumItemTier.valueOf(args[1]);
                            itemRarity = EnumItemRarity.valueOf(args[2]);
                        } catch (Exception e)
                        {
                            player.sendMessage(ChatColor.RED + "Arguments are unknown.");
                        }
                    }
                    if (args.length == 4)
                    {
                        try
                        {
                            itemType = EnumItemType.valueOf(args[0]);
                            itemTier = EnumItemTier.valueOf(args[1]);
                            itemRarity = EnumItemRarity.valueOf(args[2]);
                            customName = ChatColor.translateAlternateColorCodes('&', args[3]);
                        } catch (Exception e)
                        {
                            player.sendMessage(ChatColor.RED + "Arguments are unknown.");
                        }
                    }
                    if (args.length == 5)
                    {
                        try
                        {
                            itemType = EnumItemType.valueOf(args[0]);
                            itemTier = EnumItemTier.valueOf(args[1]);
                            itemRarity = EnumItemRarity.valueOf(args[2]);
                            customName = ChatColor.translateAlternateColorCodes('&', args[3]);
                            tradeable = Boolean.valueOf(args[4]);
                        } catch (Exception e)
                        {
                            player.sendMessage(ChatColor.RED + "Arguments are unknown.");
                        }
                    }
                    if (args.length == 6)
                    {
                        try
                        {
                            itemType = EnumItemType.valueOf(args[0]);
                            itemTier = EnumItemTier.valueOf(args[1]);
                            itemRarity = EnumItemRarity.valueOf(args[2]);
                            customName = ChatColor.translateAlternateColorCodes('&', args[3]);
                            tradeable = Boolean.valueOf(args[4]);
                            soulbound = Boolean.valueOf(args[5]);
                        } catch (Exception e)
                        {
                            player.sendMessage(ChatColor.RED + "Arguments are unknown.");
                        }
                    }
                }
            } else
            {
                player.sendMessage(ChatColor.RED + "Developer command.");
            }
        }
        return false;
    }
}
