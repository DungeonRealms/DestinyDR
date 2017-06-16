package net.dungeonrealms.lobby;

import com.esotericsoftware.minlog.Log;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.CommandManager;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.common.util.ChatUtil;
import net.dungeonrealms.common.util.TimeUtil;
import net.dungeonrealms.lobby.bungee.NetworkClientListener;
import net.dungeonrealms.lobby.commands.CommandBuild;
import net.dungeonrealms.lobby.commands.CommandLogin;
import net.dungeonrealms.lobby.commands.CommandSetPin;
import net.dungeonrealms.lobby.commands.CommandShard;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


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

    public static Map<UUID, Consumer<AsyncPlayerChatEvent>> chatCallbacks = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        //Dont need these in the lobby?
//        AsyncUtils.threadCount = 2;
//        AsyncUtils.pool = Executors.newFixedThreadPool(AsyncUtils.threadCount);
        Constants.build();
        BungeeUtils.setPlugin(this);
        BungeeServerTracker.startTask(3L);

        SQLDatabaseAPI.getInstance().init();
//        this.sqlDatabase = new SQLDatabase(getConfig().getString("sql.hostname"), getConfig().getString("sql.username"), getConfig().getString("sql.password"), getConfig().getString("sql.database"));
        if (!SQLDatabaseAPI.getInstance().getDatabase().isConnected()) {
            Bukkit.getLogger().info("Unable to connect to MySQL database....");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getLogger().info("Connected to MySQL Database!");
//        DatabaseInstance.getInstance().startInitialization(true);
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

        new Rank();
        cm.registerCommand(new CommandShard("shard", "/<command> [args]", "Shard command.", Collections.singletonList("connect")));
        cm.registerCommand(new CommandLogin("pin", "/<command> <pin>", "Staff auth command.", Arrays.asList("pin", "login")));
        cm.registerCommand(new CommandSetPin("setpin", "/<command> <oldpin> <pin>", "Set your pin.", Collections.singletonList("setpin")));
        cm.registerCommand(new CommandBuild());

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            recentLogouts.asMap().forEach((id, timer) -> timer.decrementAndGet());
        }, 20, 20);
    }

    public void sendClientMessage(String task, String message, String[] contents) {
        if (this.client != null)
            this.client.sendNetworkMessage(task, message, contents);

    }

    @Override
    public void onDisable() {
        SQLDatabaseAPI.getInstance().shutdown();
    }

    private DecimalFormat decimalFormat = new DecimalFormat("#.#");


    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
//        if (event.getPlayer().isOp() && isLoggedIn(event.getPlayer()))
//            event.setFormat(ChatColor.AQUA + event.getPlayer().getName() + ": " + ChatColor.WHITE + event.getMessage());
//        else {

        Consumer<AsyncPlayerChatEvent> callback = chatCallbacks.get(event.getPlayer().getUniqueId());
        if (callback != null) {
            event.setCancelled(true);
            callback.accept(event);
            return;
        }
        Player player = event.getPlayer();
        PlayerRank rank = Rank.getPlayerRank(event.getPlayer().getUniqueId());
        if (player.hasMetadata("chatCD") && player.getMetadata("chatCD").size() > 0) {
            long time = player.getMetadata("chatCD").get(0).asLong();

            int MAX_WAIT = rank.isAtLeast(PlayerRank.PMOD) ? 3 : rank.isSUB() ? 10 : 20;

            double timeLeft = MAX_WAIT - (System.currentTimeMillis() - time) / 1_000D;

            if (timeLeft > 0) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Please wait " + decimalFormat.format(timeLeft) + "s before sending another chat message.");
                return;
            }
        }


        String prefix;
        if (rank != null) {
            prefix = rank.getChatPrefix();
        } else {
            prefix = "";
        }

        String msg = event.getMessage();

        if (rank == null || !rank.isAtLeast(PlayerRank.GM))
            msg = ChatUtil.checkForBannedWords(msg);

        if (msg.contains(".com") || msg.contains(".net") || msg.contains(".org") || msg.contains("http://") || msg.contains("www.")) {
            if (!Rank.isGM(player)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "No " + ChatColor.UNDERLINE + "URL's" + ChatColor.RED + " in chat!");
                return;
            }
        }

        if (rank != null && !rank.isAtLeast(PlayerRank.TRIALGM))
            player.setMetadata("chatCD", new FixedMetadataValue(Lobby.getInstance(), System.currentTimeMillis()));

        event.setFormat(prefix + event.getPlayer().getName() + ": " + ChatColor.WHITE + msg);
//        }
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
        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_BANS.getQuery(event.getUniqueId().toString()), false, rs -> {
            try {
                if (rs.first()) {
                    long expiration = rs.getLong("expiration");
                    if (expiration == 0 && !rs.getBoolean("quashed") || System.currentTimeMillis() < expiration) {
                        //Banned...
                        String bannedMessage = rs.getString("reason");
//                        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
//                        event.setKickMessage(bannedMessage);
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                                ChatColor.RED.toString() + "The account " + ChatColor.BOLD.toString() + event.getName() + ChatColor.RED.toString()
                                        + " is banned. Your ban expires in " + ChatColor.UNDERLINE.toString() +
                                        (expiration <= 0 ? "NEVER" : TimeUtil.formatDifference((expiration - System.currentTimeMillis()) / 1000))
                                        + "." + "\n\n" + ChatColor.RED.toString() + "You were banned for:\n" + ChatColor.UNDERLINE.toString() + bannedMessage);
                        rs.close();
                        return;
                    }
                }

                // REQUEST PLAYER'S DATA ASYNC //
                SQLDatabaseAPI.getInstance().createDataForPlayer(event.getUniqueId(), event.getName(), event.getAddress().getHostAddress(), account_id -> {
                    //No new account_id..
                    if (account_id == null) return;
                    Bukkit.getLogger().info("Sending network packet to register user " + event.getName() + " with new account_id = " + account_id);
                    client.sendNetworkMessage("CreateAccount", event.getUniqueId().toString(), event.getName(), account_id + "");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public Location getLobbyLocation(World world) {
        return new Location(world, 36.873, 142, 21, 179.4F, -2.5F);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(this, () -> {
            Player player = event.getPlayer();

            PlayerRank rank = Rank.getRank(player);
            String rankColor = rank.getChatColor() + player.getName();
            player.setPlayerListName(rankColor);
            player.setDisplayName(rankColor);
            player.setCustomName(rankColor);

//            player.teleport(new Location(player.getWorld(), -420.512, 8.5, -149.540));
            player.teleport(getLobbyLocation(player.getWorld()));

            if (!player.isOp())
                player.getInventory().clear();

            player.getInventory().setItem(0, getShardSelector());

            ghostFactory.addPlayer(player);
            ghostFactory.setGhost(player, !rank.isSUB());

            //if (Rank.isPMOD(player))
            this.allowLogin(player, false);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        chatCallbacks.remove(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> this.allowedStaff.remove(event.getPlayer().getUniqueId()), 1L);
    }

    @EventHandler
    public void onEntityDamage(HangingBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteractAt(PlayerInteractAtEntityEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(true);
        }

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
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR)
            event.getPlayer().setAllowFlight(true);


        if (event.getTo().getY() < 0) {
            event.getPlayer().teleport(getLobbyLocation(event.getPlayer().getWorld()));
//            event.setCancelled(true);
        }
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
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (!e.hasItem()) return;
            if (e.getItem().getType() != Material.COMPASS) return;

            e.setCancelled(true);

            if (!Lobby.getInstance().isLoggedIn(p)) {
                p.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + " >> " + ChatColor.RED + "You must login before you can use this.");
                return;
            }

            if (SQLDatabaseAPI.getInstance().getPendingPlayerCreations().contains(p.getUniqueId())) {
                p.sendMessage(ChatColor.RED + "Please wait while we create your player data for the first time...");
                return;
            }

            if (chatCallbacks.containsKey(p.getUniqueId())) {
                p.sendMessage(ChatColor.RED + "Please finish entering your Pack Bypass code.");
                p.sendMessage(ChatColor.GRAY + "Since you decided to not use our Custom Resource Pack, you must enter a code before logging in.");
                return;
            }
            new ShardSelector(p).open(p);
        } else {
            e.setCancelled(true);
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

    public void allowLogin(Player player, boolean addToList) {
        if (addToList && !this.allowedStaff.contains(player.getUniqueId()))
            this.allowedStaff.add(player.getUniqueId());

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("AllowLogin");
        out.writeUTF(player.getUniqueId().toString());
        getClient().sendTCP(out.toByteArray());
    }

    public boolean isLoggedIn(Player player) {
        return true; //this.allowedStaff.contains(player.getUniqueId()) || !Rank.isPMOD(player);
    }
}
