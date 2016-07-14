package net.dungeonrealms.lobby;

import lombok.Getter;
import net.dungeonrealms.Constants;
import net.dungeonrealms.game.commands.CommandManager;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.DatabaseDriver;
import net.dungeonrealms.lobby.commands.CommandShard;
import net.dungeonrealms.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.network.bungeecord.BungeeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/11/2016
 */
public class Lobby extends JavaPlugin implements Listener {

    @Getter
    private static Lobby instance;

    private List<UUID> loading_users = new ArrayList<>(Constants.PLAYER_SLOTS / 2);

    @Override
    public void onEnable() {
        instance = this;

        BungeeUtils.setPlugin(this);
        BungeeServerTracker.startTask(this, 1L);
        DatabaseDriver.getInstance().startInitialization(true);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        Bukkit.getPluginManager().registerEvents(this, this);

        CommandManager cm = new CommandManager();

        // Commands always registered regardless of server.
        cm.registerCommand(new CommandShard("shard", "/<command> [args]", "Shard command."));
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        DatabaseAPI.getInstance().requestPlayer(event.getUniqueId());

        try {
            loading_users.add(event.getUniqueId());
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> loading_users.remove(event.getUniqueId()), 60L);
        } catch (IndexOutOfBoundsException ignored) {
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(this, () -> {

            Player player = event.getPlayer();

            if (!hasItem(player.getInventory(), getShardSelector()))
                player.getInventory().setItem(0, getShardSelector());
        });
    }

    @EventHandler
    public void onItemClick(PlayerDropItemEvent e) {
        if (e.getItemDrop().getItemStack().getType() == Material.COMPASS) {
            e.setCancelled(true);
            return;
        }
    }


    @EventHandler
    public void onItemClick(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if ((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {

            if (!e.hasItem()) return;
            if (e.getItem().getType() != Material.COMPASS) return;

            if (loading_users.contains(p.getUniqueId()))
                return;

            new ShardSelector(p).open(p);
        }
    }


    private ItemStack getShardSelector() {
        ItemStack navigator = new ItemStack(Material.COMPASS);
        ItemMeta navigatorMeta = navigator.getItemMeta();
        navigatorMeta.setDisplayName(ChatColor.GREEN + "Shard Selector");
        navigator.setItemMeta(navigatorMeta);

        return navigator;
    }

    private boolean hasItem(PlayerInventory inventory, ItemStack item) {

        if (!item.hasItemMeta()) return false;
        if (!item.getItemMeta().hasDisplayName()) return false;

        for (ItemStack i : inventory.getContents()) {
            if (i != null && i.hasItemMeta() && i.getItemMeta().getDisplayName().contains(item.getItemMeta().getDisplayName())) {
                return true;
            }
        }
        return false;
    }


}
