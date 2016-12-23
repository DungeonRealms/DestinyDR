package net.dungeonrealms.lobby;

import lombok.Getter;
import net.dungeonrealms.common.game.command.CommandManager;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.lobby.commands.CommandShard;
import net.dungeonrealms.lobby.effect.GhostFactory;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/11/2016
 */
public class Lobby extends JavaPlugin implements Listener {

    @Getter
    private static Lobby instance;

    @Getter
    private GhostFactory ghostFactory;

    @Override
    public void onEnable() {
        instance = this;

        BungeeUtils.setPlugin(this);
        BungeeServerTracker.startTask(3L);
        DatabaseInstance.getInstance().startInitialization(true);
        DatabaseAPI.getInstance().startOn(this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        ghostFactory = new GhostFactory(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        CommandManager cm = new CommandManager();

        // Commands always registered regardless of server.
        cm.registerCommand(new CommandShard("shard", "/<command> [args]", "Shard command.", Collections.singletonList("connect")));
    }


    /**
     * This event is used for the DatabaseDriver.
     *
     * @param event the event.
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) throws InterruptedException {
        DatabaseAPI.getInstance().requestPlayer(event.getUniqueId(), false);
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Prevent a player from joining the lobby if he already is connected on a server
        if (DatabaseAPI.getInstance().PLAYERS.containsKey(event.getPlayer().getUniqueId())) {
            if ((boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, event.getPlayer().getUniqueId())) {
                event.getPlayer().kickPlayer(ChatColor.RED + "Invalid game_session ID");
                DatabaseAPI.getInstance().PLAYERS.remove(event.getPlayer().getUniqueId());
                return;
            }

            if (PunishAPI.getInstance().isBanned(event.getPlayer().getUniqueId())) {
                String bannedMessage = PunishAPI.getInstance().getBannedMessage(event.getPlayer().getUniqueId());
                event.getPlayer().kickPlayer(bannedMessage);

                DatabaseAPI.getInstance().PLAYERS.remove(event.getPlayer().getUniqueId());
                return;
            }
        }
            Bukkit.getScheduler().runTask(this, () -> {
                Player player = event.getPlayer();

                player.setPlayerListName(Rank.colorFromRank(Rank.getInstance().getRank(player.getUniqueId())) + player.getName());
                player.setDisplayName(Rank.colorFromRank(Rank.getInstance().getRank(player.getUniqueId())) + player.getName());
                player.setCustomName(Rank.colorFromRank(Rank.getInstance().getRank(player.getUniqueId())) + player.getName());

                player.teleport(new Location(player.getWorld(), -972 + 0.5, 13.5, -275 + 0.5));

                if (!player.isOp())
                    player.getInventory().clear();

                player.getInventory().setItem(0, getShardSelector());

                ghostFactory.addPlayer(player);
                ghostFactory.setGhost(player, !Rank.isGM(player) && !Rank.isSubscriber(player));
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(Lobby.getInstance(), () -> {
            if (DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) {
                DatabaseAPI.getInstance().PLAYERS.remove(player.getUniqueId());
            }
        }, 1L);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().isOp())
            event.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().isOp())
            e.setCancelled(true);

    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if ((event.getPlayer().getGameMode() != GameMode.CREATIVE) && (event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR))
            event.getPlayer().setAllowFlight(true);
    }

    @EventHandler
    public void onFly(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setVelocity(player.getLocation().getDirection().multiply(2.7D).setY(0.4D));
            player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0F, 1.0F);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemClick(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if ((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {

            if (!e.hasItem()) return;
            if (e.getItem().getType() != Material.COMPASS) return;

            new ShardSelector(p).open(p);
            e.setCancelled(true);
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
