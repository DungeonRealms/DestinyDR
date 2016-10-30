package net.dungeonrealms.lobby;

import lombok.Getter;
import net.dungeonrealms.common.game.command.CommandManager;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.common.lib.scoreboard.ScoreboardBuilder;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
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
import org.bukkit.scoreboard.DisplaySlot;

import java.util.*;

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
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        ghostFactory = new GhostFactory(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        CommandManager cm = new CommandManager();

        // Commands always registered regardless of server.
        cm.registerCommand(new CommandShard("shard", "/<command> [args]", "Shard command.", Collections.singletonList("connect")));

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Lobby.getInstance(), this::scoreboardTask, 5L, 5L);
    }


    /**
     * This event is used for the DatabaseDriver.
     *
     * @param event the event.
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) throws InterruptedException {
        if (PunishAPI.getInstance().isBanned(event.getUniqueId())) {
            String bannedMessage = PunishAPI.getInstance().getBannedMessage(event.getUniqueId());
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

    public void scoreboardTask() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            ScoreboardBuilder builder = new ScoreboardBuilder(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Shards  ");
            builder.setDisplaySlot(DisplaySlot.SIDEBAR);
            getShardInfo(player).keySet().forEach(i -> builder.setLine(i, getShardInfo(player).get(i)));
            builder.send(player);
        });
    }


    public HashMap<Integer, String> getShardInfo(Player player) {
        List<BungeeServerInfo> servers = new ArrayList<>(getFilteredServers().values());

        Collections.sort(servers, (o1, o2) -> {

            int o1num = Integer.parseInt(o1.getServerName().substring(o1.getServerName().length() - 1));
            int o2num = Integer.parseInt(o2.getServerName().substring(o2.getServerName().length() - 1));

            if (!o1.getServerName().contains("us"))
                return -1;

            return o1num - o2num;
        });

        // DISPLAY AVAILABLE SHARDS //
        for (BungeeServerInfo info : servers) {
            String bungeeName = info.getServerName();
            String shardID = ShardInfo.getByPseudoName(bungeeName).getShardID();

            // Do not show YT / CS shards unless they've got the appropriate permission to see them.
            if ((shardID.contains("YT") && !Rank.isYouTuber(player)) || (shardID.contains("CS") && !Rank.isSupport(player)) || (shardID.equalsIgnoreCase("US-0") && !Rank.isGM(player))) {
                continue;
            }
        }
        HashMap<Integer, String> map = new HashMap<>();
        int i = 0;
        for (BungeeServerInfo server : servers) {
            String shardID = ShardInfo.getByPseudoName(server.getServerName()).getShardID();
            String load = server.getMotd1().replace("}", "").replace("\"", "").split(",")[1];
            int minPlayers = server.getOnlinePlayers();
            int maxPlayers = server.getMaxPlayers();
            String color = "";
            if (shardID.contains("SUB")) {
                color = "&a";
            }
            if (shardID.contains("BR")) {
                color = "&3";
            }
            if (shardID.contains("US")) {
                color = "&e";
            }
            if (shardID.contains("YT")) {
                color = "&6";
            }
            if (shardID.contains("EU")) {
                color = "&6";
            }
            if (shardID.contains("CS")) {
                color = "&c";
            }
            String shardString = color + shardID + " " + load + " &7(" + minPlayers + "/" + maxPlayers + ") ";
            map.put(i, ChatColor.translateAlternateColorCodes('&', shardString));
            i++;
        }
        return map;
    }

    private int getNormalServers() {
        int count = 0;

        for (String bungeeName : getFilteredServers().keySet()) {
            String shardID = ShardInfo.getByPseudoName(bungeeName).getShardID();
            if (getServerType(shardID).equals(""))
                count++;
        }

        return count;
    }

    /**
     * Returns the material associated with a shard.
     *
     * @param shardID
     * @return Material
     */
    private ItemStack getShardItem(String shardID) {
        shardID = shardID.toUpperCase();

        if (shardID.equals("US-0")) return new ItemStack(Material.DIAMOND);
        else if (shardID.startsWith("CS-")) return new ItemStack(Material.PRISMARINE_SHARD);
        else if (shardID.startsWith("YT-")) return new ItemStack(Material.GOLD_NUGGET);
        else if (shardID.startsWith("BR-")) return new ItemStack(Material.SAPLING, 1, (byte) 3);
        else if (shardID.startsWith("SUB-")) return new ItemStack(Material.EMERALD);

        return new ItemStack(Material.END_CRYSTAL);
    }

    /**
     * Returns the chat colour associated with a shard.
     *
     * @param shardID
     * @return ChatColor
     */
    private ChatColor getShardColour(String shardID) {
        shardID = shardID.toUpperCase();

        if (shardID.equals("US-0")) return ChatColor.AQUA;
        else if (shardID.startsWith("CS-")) return ChatColor.BLUE;
        else if (shardID.startsWith("YT-")) return ChatColor.RED;
        else if (shardID.startsWith("SUB-")) return ChatColor.GREEN;

        return ChatColor.YELLOW;
    }

    private static Map<String, BungeeServerInfo> getFilteredServers() {
        Map<String, BungeeServerInfo> filteredServers = new HashMap<>();

        for (Map.Entry<String, BungeeServerInfo> e : BungeeServerTracker.getTrackedServers().entrySet()) {
            String bungeeName = e.getKey();
            if (ShardInfo.getByPseudoName(bungeeName) == null) continue;
            BungeeServerInfo info = e.getValue();

            if (!info.isOnline() || info.getMotd1().contains("offline"))
                continue;

            filteredServers.put(bungeeName, info);
        }

        return filteredServers;
    }

    public String getServerType(String shardID) {
        if (shardID.contains("SUB")) return "Subscribers Only";
        if (shardID.contains("YT")) return "YouTubers Only";
        if (shardID.contains("BR")) return "Brazilian Shard";
        if (shardID.contains("RP")) return "Role-playing Shard";
        if (shardID.contains("CS")) return "Support Agents Only";
        return "";
    }
}
