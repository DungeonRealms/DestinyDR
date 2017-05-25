package net.dungeonrealms.lobby;

import com.esotericsoftware.minlog.Log;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.CommandManager;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.common.game.util.AsyncUtils;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.lobby.bungee.NetworkClientListener;
import net.dungeonrealms.lobby.commands.CommandBuild;
import net.dungeonrealms.lobby.commands.CommandLogin;
import net.dungeonrealms.lobby.commands.CommandSetPin;
import net.dungeonrealms.lobby.commands.CommandShard;
import net.dungeonrealms.lobby.commands.CommandTest;
import net.dungeonrealms.lobby.effect.GhostFactory;
import net.dungeonrealms.network.GameClient;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/11/2016
 */
public class Lobby extends JavaPlugin implements Listener {

    @Getter
    private GameClient client;

    @Getter
    private static Lobby instance;

    @Getter
    private GhostFactory ghostFactory;

    private ArrayList<UUID> allowedStaff = new ArrayList<UUID>();

    @Getter
    private Cache<UUID, AtomicInteger> recentLogouts = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.SECONDS).build();

    @Override
    public void onEnable() {
        instance = this;
        AsyncUtils.threadCount = Runtime.getRuntime().availableProcessors();
        AsyncUtils.pool = Executors.newFixedThreadPool(AsyncUtils.threadCount);
        Constants.build();
        BungeeUtils.setPlugin(this);
        BungeeServerTracker.startTask(3L);
        DatabaseInstance.getInstance().startInitialization(true);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        ghostFactory = new GhostFactory(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        CommandManager cm = new CommandManager();

        client = new GameClient();

        try {
            client.connect();
            Log.set(Log.LEVEL_INFO);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.client != null)
            new NetworkClientListener().startInitialization(this.client);

        cm.registerCommand(new CommandShard("shard", "/<command> [args]", "Shard command.", Collections.singletonList("connect")));
        cm.registerCommand(new CommandLogin("pin", "/<command> <pin>", "Staff auth command.", Arrays.asList("pin", "login")));
        cm.registerCommand(new CommandSetPin("setpin", "/<command> <oldpin> <pin>", "Set your pin.", Collections.singletonList("setpin")));
        cm.registerCommand(new CommandBuild());
        cm.registerCommand(new CommandTest());

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
//            recentLogouts.getAllPresent((iter))
            recentLogouts.asMap().forEach((id, timer) -> timer.decrementAndGet());
        }, 20, 20);
    }

    public void sendClientMessage(String task, String message, String[] contents) {
        if (this.client != null) {
            this.client.sendNetworkMessage(task, message, contents);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().isOp() && isLoggedIn(event.getPlayer()))
            event.setFormat(ChatColor.AQUA + event.getPlayer().getName() + ": " + ChatColor.WHITE + event.getMessage());
        else
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (Rank.isPMOD(event.getPlayer()) && !isLoggedIn(event.getPlayer()))
            event.setCancelled(true);
    }

    /**
     * This event is used for the DatabaseDriver.
     *
     * @param event the event.
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) throws InterruptedException {
        if (PunishAPI.isBanned(event.getUniqueId())) {
            String bannedMessage = PunishAPI.getBannedMessage(event.getUniqueId());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            event.setKickMessage(bannedMessage);

            DatabaseAPI.getInstance().PLAYERS.remove(event.getUniqueId());
            return;
        }

        // REQUEST PLAYER'S DATA ASYNC //
        DatabaseAPI.getInstance().requestPlayer(event.getUniqueId(), false);

    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(this, () -> {
            Player player = event.getPlayer();

            String rankColor = Rank.colorFromRank(Rank.getInstance().getRank(player.getUniqueId())) + player.getName();
            player.setPlayerListName(rankColor);
            player.setDisplayName(rankColor);
            player.setCustomName(rankColor);

            player.teleport(new Location(player.getWorld(), -420.512, 8.5, -149.540));

            if (!player.isOp())
                player.getInventory().clear();

            player.getInventory().setItem(0, getShardSelector());

            ghostFactory.addPlayer(player);
            ghostFactory.setGhost(player, !Rank.isPMOD(player) && !Rank.isSubscriber(player));

            if (Rank.isPMOD(player)) {
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {

                    String lastIp = (String) DatabaseAPI.getInstance().getData(EnumData.IP_ADDRESS, player.getUniqueId());

                    if (lastIp != null && lastIp.equals(player.getAddress().getAddress().getHostAddress())) {
                        player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + " >> " + ChatColor.GREEN + "You have been automatically logged in.");
                        this.allowLogin(player, true);
                        return;
                    }

                    String messagePrefix = ChatColor.RED + ChatColor.BOLD.toString() + " >> " + ChatColor.RED;
                    if (DatabaseAPI.getInstance().getData(EnumData.LOGIN_PIN, player.getUniqueId()) == null) {
                        player.sendMessage(messagePrefix + "Please set a login code with /setpin <pin>");
                    } else {
                        player.sendMessage(messagePrefix + "Please login with /pin <pin>");
                    }
                });

            } else {
                this.allowLogin(player, false);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(Lobby.getInstance(), () -> {
            if (DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) {
                DatabaseAPI.getInstance().PLAYERS.remove(player.getUniqueId());
            }
            this.allowedStaff.remove(event.getPlayer().getUniqueId());
        }, 1L);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreProcess(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().toLowerCase().startsWith("/me") || event.getMessage().toLowerCase().startsWith("/minecraft:me")) {
            event.setCancelled(true);
            event.setMessage("");
        }
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

            e.setCancelled(true);

            if (!Lobby.getInstance().isLoggedIn(p)) {
                p.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + " >> " + ChatColor.RED + "You must login before you can use this.");
                return;
            }

            new ShardSelector(p).open(p);

        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
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

    public void allowLogin(Player player, boolean addToList) {
        if (addToList && !this.allowedStaff.contains(player.getUniqueId()))
            this.allowedStaff.add(player.getUniqueId());

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("AllowLogin");
        out.writeUTF(player.getUniqueId().toString());
        getClient().sendTCP(out.toByteArray());
    }

    public boolean isLoggedIn(Player player) {
        return this.allowedStaff.contains(player.getUniqueId()) || !Rank.isPMOD(player);
    }
}
