package net.dungeonrealms.lobby.handle.network;

import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.lib.message.CenteredMessage;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.lobby.ServerLobby;
import net.dungeonrealms.lobby.misc.gui.ShardGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ShardHandler implements SuperHandler.ListeningHandler
{
    @Override
    public void prepare()
    {
        ServerLobby.getServerLobby().getServer().getPluginManager().registerEvents(this, ServerLobby.getServerLobby());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_AIR && event.getPlayer().getItemInHand() != null)
        {
            Player player = event.getPlayer();
            ItemStack itemStack = player.getItemInHand();
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            {
                if (itemStack.getItemMeta().getDisplayName().contains("Shard Selector"))
                {
                    ShardGUI shardGUI = new ShardGUI(player);
                    if (shardGUI.getInventory().getContents().length <= 0)
                    {
                        player.closeInventory();
                        player.sendMessage(ChatColor.RED + "No shards available, please retry");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 2f, 1f);
                    } else
                    {
                        shardGUI.openInventory(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_BLAST, 15f, 1f);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getInventory().getName() != null)
        {
            if (event.getInventory().getName().equalsIgnoreCase("Shard Selector"))
            {
                event.setCancelled(true); // Cancel it no matter what
                Player player = (Player) event.getWhoClicked();
                if (event.getCurrentItem().getType() != Material.AIR || event.getCurrentItem() != null)
                {
                    if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName())
                    {
                        ItemMeta itemMeta = event.getCurrentItem().getItemMeta();

                        // Check if the player has been combat logged first
                        try
                        {
                            if (((Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_COMBAT_LOGGED, player.getUniqueId())))
                            {
                                player.sendMessage("");
                                CenteredMessage.sendCenteredMessage(player, "&c&lCOMBAT LOGGED");
                                CenteredMessage.sendCenteredMessage(player, "&7You have been combat logged!");
                                CenteredMessage.sendCenteredMessage(player, "&7You'll be reconnected to your last shard in &b&l5&7s");
                                ServerLobby.getServerLobby().getServer().getScheduler().scheduleAsyncDelayedTask(ServerLobby.getServerLobby(),
                                        () -> BungeeUtils.sendToServer(player.getName(),
                                                ShardInfo.getByPseudoName((String) DatabaseAPI.getInstance().getData(EnumData.CURRENTSERVER, player.getUniqueId())).getShardID()), 20 * 5);
                                player.closeInventory();
                            }
                        } catch (NullPointerException ignored)
                        {
                        }

                        // First check if the requested shard is full
                        if (BungeeServerTracker.getPlayersOnline(ChatColor.stripColor(itemMeta.getDisplayName()))
                                >= BungeeServerTracker.getMaxPlayers(ChatColor.stripColor(itemMeta.getDisplayName())))
                        {
                            if (Rank.isSubscriber(player)) // Allow subscribers to bypass this
                            {
                                BungeeUtils.sendToServer(player.getName(), ChatColor.stripColor(itemMeta.getDisplayName()));
                            } else
                            {
                                player.sendMessage("");
                                CenteredMessage.sendCenteredMessage(player, "&c&lFULL SHARD");
                                CenteredMessage.sendCenteredMessage(player, "&7This shard is full!");
                                CenteredMessage.sendCenteredMessage(player, "&7Subscribe @ &b&nhttp://shop.dungeonrealms.net&r &7to bypass");
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1f, 1f);
                                player.closeInventory();
                            }
                        } else
                        {
                            BungeeUtils.sendToServer(player.getName(), ChatColor.stripColor(itemMeta.getDisplayName()));
                        }

                        if (itemMeta.getDisplayName().contains("YT") && Rank.isYouTuber(player) || itemMeta.getDisplayName().contains("CS") && Rank.isSupport(player))
                        {
                            BungeeUtils.sendToServer(player.getName(), ChatColor.stripColor(itemMeta.getDisplayName()));
                        } else if (itemMeta.getDisplayName().contains("YT") && !Rank.isYouTuber(player) || itemMeta.getDisplayName().contains("CS") && !Rank.isSupport(player))
                        {
                            player.sendMessage("");
                            CenteredMessage.sendCenteredMessage(player, "&c&lNOT AUTHORIZED");
                            CenteredMessage.sendCenteredMessage(player, "&7You are not allowed to join this shard!");
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1f, 1f);
                            player.closeInventory();
                        }

                        if (itemMeta.getDisplayName().contains("SUB"))
                        {
                            if (Rank.isSubscriber(player))
                            {
                                BungeeUtils.sendToServer(player.getName(), ChatColor.stripColor(itemMeta.getDisplayName()));
                            } else
                            {
                                player.sendMessage("");
                                CenteredMessage.sendCenteredMessage(player, "&c&lSUBSCRIBER SHARD");
                                CenteredMessage.sendCenteredMessage(player, "&7This is a subscriber only shard!");
                                CenteredMessage.sendCenteredMessage(player, "&7Purchase @ &b&nhttp://shop.dungeonrealms.net&r &7to access");
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1f, 1f);
                                player.closeInventory();
                            }
                        }
                    }
                }
            }
        }
    }
}
