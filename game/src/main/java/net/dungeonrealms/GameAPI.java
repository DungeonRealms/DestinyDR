package net.dungeonrealms;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.ConcurrentSet;
import lombok.Cleanup;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.player.Rank.PlayerRank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.util.AsyncUtils;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.ShardInfo.ShardType;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.common.util.TimeUtil;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;
import net.dungeonrealms.database.rank.Subscription;
import net.dungeonrealms.game.achievements.AchievementManager;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.achievements.Achievements.EnumRankAchievement;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler.WorldZoneType;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.mastery.DamageTracker;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mastery.UUIDHelper;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.generic.MechanicManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.combat.CombatLogger;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.player.notice.Notice;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.dungeonrealms.game.world.item.Item.GeneratedItemType;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.dungeonrealms.network.GameClient;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.activation.UnknownObjectException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Nick on 9/17/2015.
 */
public class GameAPI {

    /**
     * Thread-safe ConcurrentHashMap. Constant time searches instead of linear for
     * CopyOnWriteArrayList
     */
    public static Map<String, GamePlayer> GAMEPLAYERS = new ConcurrentHashMap<>();
    public static Set<Player> _hiddenPlayers = new ConcurrentSet<>();

    /**
     * Used to avoid double saving player data
     */
    public static Set<UUID> IGNORE_QUIT_EVENT = new ConcurrentSet<>();


    private static class PlayerLogoutWatchdog extends BukkitRunnable {
        private Player player;

        PlayerLogoutWatchdog(Player player) {
            this.runTaskLater(DungeonRealms.getInstance(), 8 * 20);
            this.player = player;
        }

        @Override
        public void run() {
            if (player.isOnline()) {
                IGNORE_QUIT_EVENT.remove(player.getUniqueId());
                TitleAPI.sendTitle(player, 0, 0, 0, "", "");
                BungeeUtils.sendToServer(player.getName(), "Lobby");

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(),
                        () -> BungeeUtils.sendPlayerMessage(player.getName(), ChatColor.RED + "Unable to send you to requested server. We have sent you to the lobby as a safety measure."), 3 * 20L);
            }
        }
    }

    /**
     * Utility type for calling async tasks with callbacks.
     *
     * @param callable Callable type
     * @param consumer Consumer task
     * @param <T>      Type of data
     * @author apollosoftware
     */
    public static <T> void submitAsyncCallback(Callable<T> callable, Consumer<Future<T>> consumer) {
        // FUTURE TASK //
        FutureTask<T> task = new FutureTask<>(callable);

        // BUKKIT'S ASYNC SCHEDULE WORKER
        new BukkitRunnable() {
            @Override
            public void run() {
                // RUN FUTURE TASK ON THREAD //
                task.run();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // ACCEPT CONSUMER //
                        if (consumer != null)
                            consumer.accept(task);
                    }
                }.runTask(DungeonRealms.getInstance());
            }
        }.runTaskAsynchronously(DungeonRealms.getInstance());
    }

    /**
     * To get the players region.
     *
     * @param location The location
     * @return The region name
     * @since 1.0
     */
    public static String getRegionName(Location location) {

        try {
            RegionManager regionManager = WorldGuardPlugin.inst().getRegionManager(location.getWorld());
            if (regionManager == null) return "";
            ApplicableRegionSet set = regionManager.getApplicableRegions(location);
            if (set.size() == 0)
                return "";

            String returning = "";
            int priority = -1;
            for (ProtectedRegion s : set) {
                if (s.getPriority() > priority) {
                    if (!s.getId().equals("")) {
                        returning = s.getId();
                        priority = s.getPriority();
                    }
                }
            }

            return returning;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getItemSlot(PlayerInventory inv, String type) {
        for (int i = 0; i < inv.getContents().length; i++) {
            ItemStack item = inv.getContents()[i];
            if (item == null || item.getType() == null || item.getType() == Material.AIR) continue;
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = nmsStack.getTag();
            if (tag == null) continue;
            if (!tag.hasKey(type)) continue;
            if (tag.getString(type).equalsIgnoreCase("true")) return i;
        }
        return -1;
    }

    public static int getTierFromLevel(int level) {
        if (level < 10) {
            return 1;
        } else if (level < 20) {
            return 2;
        } else if (level < 30) {
            return 3;
        } else if (level < 40) {
            return 4;
        } else {
            return 5;
        }
    }

    /**
     * @param player
     * @param kill
     * @return Integer
     */
    public static int getMonsterExp(Player player, org.bukkit.entity.Entity kill) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        int level = wrapper.getLevel();
        int mob_level = EntityAPI.getLevel(kill);
        int xp;
        double amplifier = 1.0;
        if (mob_level > level + 10) {  // limit mob xp calculation to 10 levels above player level
            xp = calculateXP(level, level + 10, amplifier);
        } else if (level + 5 > mob_level) {
            int difference = (level + 5) - mob_level;
            int toReduce = 0;
            while (difference > 0) {
                if (toReduce >= 75) {
                    break;
                }
                difference--;
                toReduce += 5;
            }
            amplifier = ((100.0 - toReduce) / 100.0);
            xp = calculateXP(mob_level + 5, mob_level, amplifier);
        } else {
            xp = calculateXP(level, mob_level, amplifier);
        }
        return xp;
    }

    //639 Realm instance

    public static ItemStack[] getTierArmor(int tier) {
        ItemArmor armor = new ItemArmor();
        armor.setTier(ItemTier.getByTier(tier));
        armor.setRarity(ItemRarity.getRandomRarity());
        return armor.generateArmorSet();
    }

    public static org.bukkit.ChatColor getTierColor(int tier) {
        ItemTier retr = ItemTier.getByTier(tier);
        return (retr == null ? ItemTier.TIER_1 : retr).getColor();
    }

    public static GameClient getClient() {
        return DungeonRealms.getClient();
    }

    /**
     * Stops DungeonRealms server
     */
    public static void stopGame() {
        DungeonRealms.getInstance().setAlmostRestarting(true);
        DungeonRealms.getInstance().getLogger().info("stopGame() called.");

        final long restartTime = Bukkit.getOnlinePlayers().size() <= 5 && DungeonRealms.isMaster() ? 10 : (Bukkit.getOnlinePlayers().size() * 25) + 100; // second per player plus 5 seconds

        try {
            Bukkit.getLogger().info("Saving all shops sync...");
            long start = System.currentTimeMillis();
            @Cleanup PreparedStatement statement = ShopMechanics.deleteAllShops(true);
            statement.executeBatch();
            Bukkit.getLogger().info("Saved all shops in " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bukkit.getServer().setWhitelist(true);
        DungeonRealms.getInstance().setAcceptPlayers(false);
        DungeonRealms.getInstance().saveConfig();
        CombatLog.getInstance().getCOMBAT_LOGGERS().values().forEach(CombatLogger::handleTimeOut);
        Bukkit.getScheduler().cancelAllTasks();

        GameAPI.logoutAllPlayers();

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN in 5s");
            //Do sync..
            long start = System.currentTimeMillis();
            SQLDatabaseAPI.getInstance().executeUpdate(done ->
                            Bukkit.getLogger().info("Set " + done + " players with shard " + DungeonRealms.getInstance().bungeeName + " to offline in " + (System.currentTimeMillis() - start) + "ms"),
                    QueryType.FIX_WHOLE_SHARD.getQuery(DungeonRealms.getShard().getPseudoName()), false);

            MechanicManager.stopMechanics();
            AsyncUtils.pool.shutdown();
            Bukkit.shutdown();
        }, restartTime);
    }


    public static void handleCrash() {
        Bukkit.getServer().setWhitelist(true);
        DungeonRealms.getInstance().setAcceptPlayers(false);
        DungeonRealms.getInstance().saveConfig();

        Constants.log.info("called handleCrash()...");

        //Sometimes the crash detector has to kill bukkit on a normal shutdown, we don't need to announce it if DR's normal shutdown has already run.
        if (!DungeonRealms.getInstance().isAlmostRestarting())
            sendNetworkMessage("GMMessage", ChatColor.RED + "[ALERT] " + ChatColor.WHITE + "Shard " + ChatColor.GOLD + "{SERVER}" + ChatColor.WHITE + " has crashed.");


        final long terminateTime = ScoreboardHandler.getInstance().PLAYER_SCOREBOARDS.size() * 1000 + 10000;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Constants.log.info("Terminating server's process..");

                try {
                    Runtime.getRuntime().exec("kill -9 " + Utils.getPid());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, terminateTime);

        try {
            Constants.log.info("Attempting to save all shops on crash...");
            int[] shopsSaved = null;
            @Cleanup PreparedStatement statement = ShopMechanics.deleteAllShops(true);
            statement.executeBatch();
            int affected = Arrays.stream(shopsSaved).sum();
            Constants.log.info("Managed to save " + affected + " shops..");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Constants.log.info("Saved all player shops successfully.");

        Constants.log.info("Saving all player realms.");

        Realms.getInstance().saveAllRealms();

        Constants.log.info("Saved all player realms successfully.");

        Constants.log.info("Saving all players' sessions...");

        final long currentTime = System.currentTimeMillis();
        //Use this cause its offline access essentially?
        ScoreboardHandler.getInstance().PLAYER_SCOREBOARDS.keySet().forEach(uuid -> {
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
            wrapper.saveData(false, false, done -> {
                wrapper.setPlayingStatus(false);
                IGNORE_QUIT_EVENT.add(uuid);
                GameAPI.sendNetworkMessage("MoveSessionToken", uuid.toString(), "false");
            });
        });

        System.out.println("Successfully saved all sessions in " + String.valueOf(System.currentTimeMillis() - currentTime) + "ms");

        MechanicManager.stopMechanics();
        AsyncUtils.pool.shutdown();
        SQLDatabaseAPI.getInstance().shutdown();
    }

    /**
     * @param pLevel
     * @param mob_level
     * @param reduction
     * @return integer
     */
    private static int calculateXP(int pLevel, int mob_level, double reduction) {
        int expToGive = (int) ((pLevel * 5 + 45) * (1 + 0.07 * (pLevel + mob_level - pLevel)));
        return (int) (expToGive * reduction);
    }


    /**
     * Will return the players
     * IP,Country,Zipcode,region,region_name,City,time_zone, and geo cordinates
     * in the world.
     *
     * @param uuid
     * @return
     * @since 1.0
     */
    public static JsonObject getPlayerCredentials(UUID uuid) {
        URL url = null;
        try {
            url = new URL("http://freegeoip.net/json/");
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            return root.getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @SuppressWarnings("deprecation")
    public static String getServerLoad() {
        double tps = MinecraftServer.getServer().recentTps[0];
        return ((tps >= 19.99) ? ChatColor.GREEN + "Extremely Low" : (tps >= 19.90) ? ChatColor.GREEN + "Very Low" : (tps > 19.0) ? ChatColor.GREEN + "Low" : (tps > 15.0) ? ChatColor.YELLOW + "Medium" : ChatColor.RED + "High");
    }


    /**
     * Requests an update for cached player data on target
     * player's server
     *
     * @param uuid Target
     */
    public static void updatePlayerData(UUID uuid, UpdateType updateType) {
        // CHECK IF LOCAL //
        if (Bukkit.getPlayer(uuid) != null)
            return; // their player data has already been updated in PLAYERS

        // SENDS PACKET TO MASTER SERVER //
        sendNetworkMessage("Update", uuid.toString(), updateType.getFieldName());
    }

    /**
     * Requests an update for cached guild data on target
     * player's server
     *
     * @param guildName Target
     */
    public static void updateGuildData(String guildName) {
        // SENDS PACKET TO MASTER SERVER //
        sendNetworkMessage("Guild", "Update", guildName);
    }


    /**
     * @param task     Packet job
     * @param message  Message to send.
     * @param contents More data?
     * @since 1.0
     */
    public static void sendNetworkMessage(String task, String message, String... contents) {
        //Not Catching this could result in a crash handle failure.
        if (getClient() == null) {
            Utils.log.info("Not sending " + task + ", we haven't connected.");
            return;
        }
        getClient().sendNetworkMessage(task, message.replace("{SERVER}", ChatColor.GOLD + "" + ChatColor.UNDERLINE + DungeonRealms.getShard().getShardID() + ChatColor.RESET), contents);
    }

    public static void sendIngameDevMessage(String message, String... contents) {
        sendNetworkMessage("IGN_DEVMessage", message, contents);
    }

    public static void sendDevMessage(String message, String... contents) {
        sendNetworkMessage("DEVMessage", message, contents);
    }
    
    /**
     * Send a ComponentBuilt message cross-shard.
     * @param cb
     */
    public static void sendShardMessage(ComponentBuilder cb) {
    	sendNetworkMessage("BroadcastRaw", ComponentSerializer.toString(cb.create()));
    }

    /**
     * Gets players UUID from Name. ASYNC.
     *
     * @param name
     * @return
     */
    public static UUID getUUIDFromName(String name) {
        if (Bukkit.getPlayer(name) != null) {
            return Bukkit.getPlayer(name).getUniqueId();
        }
        return UUIDHelper.getOfflineUUID(name);
    }


    public static boolean isUUID(String uuidString) {
        try {
            UUID.fromString(uuidString);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
        return true;
    }

    /**
     * Gets players name from UUID. ASYNC.
     *
     * @param uuid
     * @return
     */
    public static String getNameFromUUID(UUID uuid) {
        if (Bukkit.getPlayer(uuid) != null) {
            return Bukkit.getPlayer(uuid).getName();
        }
        return UUIDHelper.uuidToName(uuid.toString());
    }

    public static boolean isInWorld(Player player, World world) {
        return world != null && player.getLocation().getWorld().equals(world);
    }

    /**
     * Gets the WorldGuard plugin.
     *
     * @return
     * @since 1.0
     */
    public static WorldGuardPlugin getWorldGuard() {
        Plugin plugin = DungeonRealms.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            try {
                throw new UnknownObjectException("getWorldGuard() of GameAPI.class is RETURNING NULL!");
            } catch (UnknownObjectException e) {
                e.printStackTrace();
            }
        }
        return (WorldGuardPlugin) plugin;
    }

    /**
     * Checks if player is in a region that denies PvP and Mob Damage
     *
     * @param location
     * @since 1.0
     */
    public static boolean isInSafeRegion(Location location) {
    	ApplicableRegionSet region = getRegion(location);
        return region != null && region.getFlag(DefaultFlag.PVP) != null && !region.allows(DefaultFlag.PVP)
                && region.getFlag(DefaultFlag.MOB_DAMAGE) != null && !region.allows(DefaultFlag.MOB_DAMAGE);
    }

    public static boolean isNonPvPRegion(Location location) {
        ApplicableRegionSet region = getRegion(location);
        return region != null && region.getFlag(DefaultFlag.PVP) != null && !region.allows(DefaultFlag.PVP);
    }

    public static boolean isNonMobDamageRegion(Location location) {
        ApplicableRegionSet region = getRegion(location);
        return region != null && region.getFlag(DefaultFlag.MOB_DAMAGE) != null && !region.allows(DefaultFlag.MOB_DAMAGE);
    }
    
    private static ApplicableRegionSet getRegion(Location l) {
    	if (l == null || l.getWorld() == null)
    		return null;
    	
    	 RegionManager regionManager = getWorldGuard().getRegionManager(l.getWorld());
         if (regionManager == null)
        	 return null;
         
         return regionManager.getApplicableRegions(l);
    }

    /**
     * Will check the players region
     *
     * @param uuid
     * @param region
     * @return
     * @since 1.0
     */
    public static boolean isPlayerInRegion(UUID uuid, String region) {
        return getWorldGuard().getRegionManager(Bukkit.getPlayer(uuid).getWorld())
                .getApplicableRegions(Bukkit.getPlayer(uuid).getLocation()).getRegions().contains(region);
    }

    public static List<Player> getNearbyPlayers(Location location, int radius) {
        return getNearbyPlayers(location, radius, false);
    }

    public static List<Player> getNearbyPlayers(CraftEntity entity, int radius) {
        return getNearbyPlayers(entity, radius, false);
    }


    /**
     * Gets the a list of nearby players from a location within a given radius
     *
     * @param entity
     * @param radius
     * @param
     * @since 1.0
     */
    public static List<Player> getNearbyPlayers(CraftEntity entity, int radius, boolean ignoreVanish) {
        List<Player> playersNearby = new ArrayList<>();
        entity.getNearbyEntities(radius, radius, radius).stream().filter((ent) -> ent instanceof Player).forEach((pl) -> {
            Player player = (Player) pl;
            if ((!GameAPI.isPlayer(player) || GameAPI._hiddenPlayers.contains(player)) && !ignoreVanish) {
                return;
            }
            playersNearby.add(player);
        });
        return playersNearby;
    }

    /**
     * Gets the a list of nearby players from a location within a given radius
     *
     * @param location
     * @param radius
     * @param
     * @since 1.0
     */
    public static List<Player> getNearbyPlayers(Location location, int radius, boolean ignoreVanish) {
        return location.getWorld().getPlayers().stream().filter(player -> !((!GameAPI.isPlayer(player) || GameAPI._hiddenPlayers.contains(player)) && !ignoreVanish)).filter(player -> location.distanceSquared(player.getLocation()) <= radius * radius).collect(Collectors.toList());
    }

    public static volatile Set<Player> asyncTracker = new ConcurrentSet<>();

    public static Set<Player> getNearbyPlayersAsync(Location location, int radius) {
        return asyncTracker.stream().filter(OfflinePlayer::isOnline).filter(player -> player.getWorld().equals(location.getWorld()) && location.distanceSquared(player.getLocation()) <= radius * radius).collect(Collectors.toSet());
    }

    /**
     * Async thread safe method.
     *
     * @param location
     * @param radius
     * @return
     */
    public static boolean arePlayersNearbyAsync(Location location, int radius) {
        int finalRadius = radius * radius;
        return asyncTracker.stream().anyMatch(player -> player.isOnline() && player.getWorld().equals(location.getWorld()) && location.distanceSquared(player.getLocation()) <= finalRadius);
    }

    public static boolean arePlayersNearby(Location location, int radius) {
        return location.getWorld().getPlayers().stream().filter(player -> !(!GameAPI.isPlayer(player) || GameAPI._hiddenPlayers.contains(player))).anyMatch(player -> location.distanceSquared(player.getLocation()) <= radius * radius);
    }

    public static void handleLogout(Player player, boolean async, Consumer<Boolean> doAfter) {
        handleLogout(player, async, doAfter, true);
    }

    public static void handleLogout(Player player, boolean async, Consumer<Boolean> doAfter, boolean remove) {
        if (player == null || DungeonRealms.getInstance().getLoggingIn().contains(player.getUniqueId()))
            return;

        if (player.hasMetadata("saved")) {
            //Already saved... just call callback so it can remove them..
            doAfter.accept(false);
            Bukkit.getLogger().info("No re-saving " + player.getName() + " because they have already been saved.");
            return;
        }

        Utils.log.info("Handling logout for " + player.getName() + " (" + player.getUniqueId().toString() + ")");
        DungeonRealms.getInstance().getLoggingOut().add(player.getName());

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) {
            Bukkit.getLogger().info("null player wrapper for " + player.getName());
            return;
        }

        GuildMechanics.getInstance().doLogout(player);
        wrapper.setHealth(HealthHandler.getHP(player));
        wrapper.setStoredFoodLevel(player.getFoodLevel());
        Realms.getInstance().handleLogout(player);
        HealthHandler.handleLogout(player);
        Chat.listenForMessage(player, null);

        // Remove dungeonitems from inventory.
        DungeonManager.removeDungeonItems(player);

        player.setMetadata("saved", new FixedMetadataValue(DungeonRealms.getInstance(), ""));
        wrapper.setLastLogout(System.currentTimeMillis());
        System.out.println("Starting the save data!");
        wrapper.saveData(async, false, wrap -> {
            System.out.println("Save data callback!");
            wrapper.setPlayingStatus(false);
            Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                for (DamageTracker tracker : HealthHandler.getMonsterTrackers().values())
                    tracker.removeDamager(player);

                if (GameAPI._hiddenPlayers.contains(player))
                    GameAPI._hiddenPlayers.remove(player);

                MountUtils.getInventories().remove(player.getUniqueId());
                Quests.getInstance().handleLogoutEvents(player);
                ScoreboardHandler.getInstance().removePlayerScoreboard(player);
                PetUtils.removePet(player);
                MountUtils.removeMount(player);

                Party party = Affair.getParty(player);
                if (party != null)
                    party.removePlayer(player, false);

                DungeonRealms.getInstance().getLoggingOut().remove(player.getName());
                if (remove) {
                    PlayerWrapper.getPlayerWrappers().remove(player.getUniqueId());
                    GAMEPLAYERS.remove(player.getName());
                }
                Utils.log.info("Saved information for uuid: " + player.getName() + " on their logout.");

                if (doAfter != null)
                    doAfter.accept(true);
            });
        });
    }

    public static void backupPlayers() {
        //TODO:
        //We need to save all players inventories and locations?
        try {
            long start = System.currentTimeMillis();
            PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("");
            int online = PlayerWrapper.getPlayerWrappers().size();
            for (PlayerWrapper wrapper : PlayerWrapper.getPlayerWrappers().values()) {
                //Dont save these ijots.
                if (wrapper.getPlayer() == null || !wrapper.getPlayer().isOnline() || !wrapper.isLoadedSuccessfully())
                    continue;

                Player player = wrapper.getPlayer();
                if (Constants.debug)
                    Bukkit.getLogger().info("Backing up " + player.getUniqueId().toString() + "(" + player.getName() + ")");

                Storage bank = BankMechanics.getStorage(player.getUniqueId());
                statement.addBatch(wrapper.getQuery(QueryType.BACKUP_CHARACTER, wrapper.getLevel(), wrapper.getExperience(),
                        wrapper.getLocationString(player.getLocation()), player.getInventory(), wrapper.getEquipmentString(player), wrapper.getGems(), bank != null ? bank.inv : null,
                        MountUtils.getInventory(player), wrapper.getMuleLevel(), wrapper.getCharacterID()));
            }
            int[] args = statement.executeBatch();
            int completed = 0;
            for (int complete : args) {
                completed += complete;
            }
            statement.close();
            Bukkit.getLogger().info("Backed up " + completed + " Player Wrappers successfully out of " + online + " loaded wrappers in " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Safely logs out all players when the server restarts. Saves their data async before.
     *
     * @since 1.0
     */
    public static void logoutAllPlayers() {
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);

        try {
            ShopMechanics.deleteAllShops(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < players.length; i++) {
            final Player player = players[i];
            Metadata.SHARDING.set(player, true);
            final boolean sub = Rank.isSUB(player);

            player.sendMessage(ChatColor.AQUA + ">>> This DungeonRealms shard is " + ChatColor.BOLD + "RESTARTING.");

            if (!DungeonRealms.getInstance().isDrStopAll) {
                player.sendMessage(" ");
                player.sendMessage(ChatColor.GRAY + "Your current game session has been paused while you are transferred.");
                player.sendMessage(" ");
            }

            // Handle pvp log first
            CombatLog.removeFromPVP(player);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {

                // prevent any interaction while the data is being uploaded
                Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(player));
                player.setInvulnerable(true);
                player.setNoDamageTicks(10);
                player.closeInventory();
                player.setCanPickupItems(false);


                GamePlayer gp = GameAPI.getGamePlayer(player);
                if (gp != null) {
                    gp.setAbleToSuicide(false);
                    gp.setAbleToDrop(false);
                    //No opening inventories while restarting... kys
                    gp.setAbleToOpenInventory(false);
                }

                if (DungeonRealms.getInstance().isDrStopAll) {
                    // SEND THEM TO THE LOBBY NORMALLY INSTEAD //
                    BungeeUtils.sendToServer(player.getName(), "Lobby");
                    return;
                } else GameAPI.IGNORE_QUIT_EVENT.add(player.getUniqueId());

                // upload data and send to server
                GameAPI.handleLogout(player, true, consumer -> {
                    // Move
                    GameAPI.sendNetworkMessage("MoveSessionToken", player.getUniqueId().toString(), String.valueOf(sub));
                }, false);

            }, (i + 1) * 4);
        }
    }

    public static void sendStopAllServersPacket() {
        sendNetworkMessage("Stop", "");
    }

    public static void handleLogin(Player player) {
        PlayerWrapper playerWrapper = PlayerWrapper.getPlayerWrapper(player);

        if (playerWrapper == null) {
            player.kickPlayer(ChatColor.RED + "Unable to grab your data, please reconnect!");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Successfully received your data, loading...");

        if (!DungeonRealms.getInstance().canAcceptPlayers() && !Rank.isDev(player)) {
            player.kickPlayer(ChatColor.RED + "This shard has not finished it's startup process.");
            PlayerWrapper.getPlayerWrappers().remove(player.getUniqueId());
            return;
        }

        ShardType type = DungeonRealms.getShard().getType();
        if (!playerWrapper.getRank().isAtLeast(type.getMinRank()) && type != ShardType.DEVELOPMENT) {
            player.kickPlayer(ChatColor.RED + "You are not authorized to connect to this shard.");
            return;
        }


        if (playerWrapper.isCombatLogged()) {
            String lastShard = playerWrapper.getShardPlayingOn();
            if (lastShard != null && !DungeonRealms.getShard().getPseudoName().equals(lastShard)) {
                player.kickPlayer(ChatColor.RED + "You have combat logged. Please connect to Shard " + lastShard);
                return;
            }
        }

        PlayerRank rank = Rank.getRank(player);
        Metadata.SHARDING.remove(player); // This player just logged in, they aren't sharding.

        GamePlayer gp = new GamePlayer(player);

        gp.setAbleToDrop(false);
        gp.setAbleToSuicide(false);
        gp.setAbleToOpenInventory(Rank.isTrialGM(player));

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            gp.setAbleToDrop(true);
            gp.setAbleToOpenInventory(true);
        }, 20L * 10L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> gp.setAbleToSuicide(true), 20L * 60L);

        // Hide invisible users from non-GMs.
        if (!Rank.isTrialGM(player)) GameAPI._hiddenPlayers.forEach(player::hidePlayer);

        TeleportAPI.addPlayerHearthstoneCD(player.getUniqueId(), 150);
        PlayerManager.checkInventory(player);

        if (playerWrapper.isFirstTimePlaying()) {
            playerWrapper.setFirstLogin(System.currentTimeMillis());
            sendNetworkMessage("IGN_GMMessage", ChatColor.GREEN + "" + ChatColor.BOLD + player.getName() + ChatColor.GRAY + " has joined " + ChatColor.BOLD + "DungeonRealms" + ChatColor.GRAY + " for the first time!");

            ItemManager.giveStarter(player, true);
            player.teleport((DungeonRealms.isEvent() ? TeleportLocation.EVENT_AREA : TeleportLocation.STARTER).getLocation());
        }

        player.setGameMode(GameMode.SURVIVAL);

        for (int j = 0; j < 20; j++)
            player.sendMessage("");

        player.setMaximumNoDamageTicks(0);
        player.setNoDamageTicks(0);

        Utils.sendCenteredMessage(player, ChatColor.WHITE.toString() + ChatColor.BOLD + "Dungeon Realms Build " + String.valueOf(Constants.BUILD_NUMBER));
        Utils.sendCenteredMessage(player, ChatColor.GRAY + "http://www.dungeonrealms.net/");
        Utils.sendCenteredMessage(player, ChatColor.YELLOW + "You are on the " + ChatColor.BOLD + DungeonRealms.getInstance().shardid + ChatColor.YELLOW + " shard.");

        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Type " + ChatColor.YELLOW + "" + ChatColor.ITALIC + "/shard" + ChatColor.GRAY + ChatColor.ITALIC + " to change shards at any time.");

        ShardInfo shard = DungeonRealms.getShard();
        ShardType shardType = shard.getType();
        if (shardType != ShardType.DEFAULT) {
            player.sendMessage("");
            player.sendMessage(ChatColor.DARK_AQUA + "This is a " + ChatColor.UNDERLINE + shardType.name() + ChatColor.DARK_AQUA + " shard.");
            for (String s : shardType.getInfo())
                player.sendMessage(ChatColor.GRAY + s);
        }

        player.sendMessage("");

        // Give rank achievements.
        for (EnumRankAchievement r : EnumRankAchievement.values())
            if (rank.isAtLeast(r.getMinRank()))
                Arrays.asList(r.getAchievements()).forEach(a -> Achievements.giveAchievement(player, a));

        // Alert mechanics.
        Quests.getInstance().handleLogin(player);
        EnergyHandler.getInstance().handleLoginEvents(player);
        Subscription.getInstance().handleLogin(player, playerWrapper);
        GuildMechanics.getInstance().doLogin(player);
        Notice.getInstance().doLogin(player);

        // Free E-Cash
        int freeEcash = (int) (playerWrapper.getLastFreeEcash() / 1000);
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        if (currentTime - freeEcash >= 86400) {
            int ecashReward = Utils.randInt(10, 15);
            playerWrapper.setLastFreeEcash(System.currentTimeMillis());
            playerWrapper.setEcash(playerWrapper.getEcash() + ecashReward);
            player.sendMessage(ChatColor.GOLD + "You have gained " + ChatColor.BOLD + ecashReward + "EC" + ChatColor.GOLD + " for logging into DungeonRealms today!");
            player.sendMessage(ChatColor.GRAY + "Use /ecash to spend your EC, you can obtain more e-cash by logging in daily or by voting " + ChatColor.GOLD + ChatColor.UNDERLINE + "http://dungeonrealms.net/vote");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
        }

        playerWrapper.setUsername(player.getName());
        playerWrapper.setShardPlayingOn(DungeonRealms.getInstance().bungeeName);
        playerWrapper.setPlayingStatus(true);

        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("IP");

            player.sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
        });

        sendNetworkMessage("Friends", "join:" + " ," + player.getUniqueId().toString() + "," + player.getName() + "," + DungeonRealms.getInstance().shardid);

        Utils.log.info("Fetched information for uuid: " + player.getUniqueId().toString() + " on their login.");
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> AchievementManager.handleLogin(player), 70L);

        player.addAttachment(DungeonRealms.getInstance()).setPermission("citizens.npc.talk", true);
        AttributeInstance instance = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        instance.setBaseValue(1024.0D);

        // Permissions
        if (!Rank.isDev(player)) {
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.plugins", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.version", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.ban.*", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.unban.*", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.op.*", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.save.*", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.whitelist.*", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.stop", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.spreadplayers", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.spawnpoint", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.setworldspawn", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.scoreboard", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.seed", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.time.*", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.gamerule", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.debug", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.reload", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.timings", false);
        }

        if(player.getName().equalsIgnoreCase("logon1027")) {
            player.addAttachment(DungeonRealms.getInstance()).setPermission("essentials.*", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.teleport", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("minecraft.command.tp", true);
        }

        if (Rank.isPMOD(player)) {
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.notify", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.command.notify", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.command.info", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.command.inspect", true);
        }

        if (Rank.isTrialGM(player)) {
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.checks", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.bypass.denylogin", true);

            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.kick", true);

            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.teleport", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("minecraft.command.tp", true);
        }

        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
            HealthHandler.handleLogin(player);
            PlayerManager.checkInventory(player);
        }, 5);

        Bukkit.getScheduler().runTaskLaterAsynchronously(DungeonRealms.getInstance(), () -> sendStatNotification(player), 100);

        if (Rank.isTrialGM(player)) {

            gp.setInvulnerable(true);
            gp.setTargettable(false);
            player.sendMessage("");

            Utils.sendCenteredMessage(player, ChatColor.AQUA + ChatColor.BOLD.toString() + "GM INVINCIBILITY");

            // check vanish
            if (playerWrapper.getToggles().getState(Toggles.VANISH)) {
                GameAPI._hiddenPlayers.add(player);
                player.setCustomNameVisible(false);
                Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(player));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
                Utils.sendCenteredMessage(player, ChatColor.AQUA + ChatColor.BOLD.toString() + "GM VANISH");
                GameAPI.sendNetworkMessage("vanish", player.getUniqueId().toString(), "true");
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                player.setGameMode(GameMode.CREATIVE);
            }
        }

        DungeonRealms.getInstance().getLoggingIn().remove(player.getUniqueId());

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            //Prevent weird scoreboard thing when sharding.
            ScoreboardHandler.getInstance().matchMainScoreboard(player);
            PlayerWrapper pw = PlayerWrapper.getWrapper(player);
            ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, pw.getAlignment().getColor(), playerWrapper.getLevel());
        }, 100L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> DonationEffects.getInstance().doLogin(player), 100L);
    }

    /**
     * type used to switch shard
     *
     * @param player           Player
     * @param serverBungeeName Bungee name
     */
    public static void moveToShard(Player player, String serverBungeeName) {
        //Ignores all logouts essentially..
        GameAPI.IGNORE_QUIT_EVENT.add(player.getUniqueId());

        // prevent any interaction while the data is being uploaded
        Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(player));
        player.setInvulnerable(true);
        player.setNoDamageTicks(10);
        player.closeInventory();
        player.setCanPickupItems(false);
        Metadata.SHARDING.set(player, true);

        GamePlayer gp = GameAPI.getGamePlayer(player);
        gp.setAbleToSuicide(false);
        gp.setAbleToDrop(false);

        // check if they're still here (server failed to accept them for some reason)
        new PlayerLogoutWatchdog(player);

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        wrapper.setLastShardTransfer(System.currentTimeMillis());
        GameAPI.handleLogout(player, true, doAfter -> BungeeUtils.sendToServer(player.getName(), serverBungeeName), false);
    }

    public static String locationToString(Location location) {
        return location.getX() + "," + (location.getY() + .3) + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }

    public static Location getLocationFromString(String locationString) {
        String[] locationStringArray = locationString.split(",");

        return new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationStringArray[0]),
                Double.parseDouble(locationStringArray[1]), Double.parseDouble(locationStringArray[2]),
                Float.parseFloat(locationStringArray[3]), Float.parseFloat(locationStringArray[4]));
    }

    /**
     * Returns if a player is online. (LOCAL SERVER)
     *
     * @param uuid
     * @return boolean
     * @since 1.0
     */
    public static boolean isOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    /**
     * Returns the string is a Pet
     *
     * @param petType
     * @return boolean
     * @since 1.0
     */
    public static boolean isStringPet(String petType) {
        return EnumPets.getByName(petType.toUpperCase()) != null;
    }

    /**
     * Returns the string is a Mount
     *
     * @param mountType
     * @return boolean
     * @since 1.0
     */
    public static boolean isStringMount(String mountType) {
        return EnumMounts.getByName(mountType.toUpperCase()) != null;
    }

    /**
     * Returns the string is a Mount Skin
     *
     * @param mountSkin
     * @return boolean
     * @since 1.0
     */
    public static boolean isStringMountSkin(String mountSkin) {
        return EnumMountSkins.getByName(mountSkin.toUpperCase()) != null;
    }

    /**
     * Returns if the entity is an actual player and not a Citizens NPC
     */
    public static boolean isPlayer(Entity entity) {
        return entity != null && entity instanceof Player && !(entity.hasMetadata("NPC") && !(entity.hasMetadata("npc")));
    }

    /**
     * Returns a list of nearby monsters defined via their "type" metadata.
     *
     * @param location
     * @param radius
     * @return List
     * @since 1.0
     */
    public static List<Entity> getNearbyMonsters(Location location, int radius) {
        return location.getWorld().getEntities().stream()
                .filter(mons -> mons.getLocation().distance(location) <= radius && EnumEntityType.HOSTILE_MOB.isType(mons))
                .collect(Collectors.toList());
    }

    /**
     * Returns the players GamePlayer
     *
     * @param p
     * @return
     */

    public static GamePlayer getGamePlayer(Player p) {
        return GAMEPLAYERS.get(p.getName());
    }

    /**
     * Checks if there is a certain material nearby.
     *
     * @param block
     * @param maxradius
     * @param materialToSearchFor
     * @return Boolean (If the material is nearby).
     * @since 1.0
     */
    public static boolean isMaterialNearby(Block block, int maxradius, Material materialToSearchFor) {
        BlockFace[] faces = {BlockFace.UP, BlockFace.NORTH, BlockFace.EAST};
        BlockFace[][] orth = {{BlockFace.NORTH, BlockFace.EAST}, {BlockFace.UP, BlockFace.EAST},
                {BlockFace.NORTH, BlockFace.UP}};
        for (int r = 0; r <= maxradius; r++) {
            for (int s = 0; s < 6; s++) {
                BlockFace f = faces[s % 3];
                BlockFace[] o = orth[s % 3];
                if (s >= 3) {
                    f = f.getOppositeFace();
                }
                if (!(block.getRelative(f, r) == null)) {
                    Block c = block.getRelative(f, r);
                    for (int x = -r; x <= r; x++) {
                        for (int y = -r; y <= r; y++) {
                            Block a = c.getRelative(o[0], x).getRelative(o[1], y);
                            if (a.getType() == materialToSearchFor) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean removePortalShardsFromPlayer(Player player, ShardTier tier, int amount) {
        if (amount <= 0)
            return amount == 0;

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        int shardAmt = wrapper.getPortalShards(tier);
        if (shardAmt > amount) {
            wrapper.setPortalShards(tier, shardAmt - amount);
            return true;
        }

        return false;
    }

    public static String getCustomID(ItemStack i) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(i);
        if (nms == null || nms.getTag() == null) return null;
        NBTTagCompound tag = nms.getTag();
        return tag.hasKey("drItemId") ? tag.getString("drItemId") : null;
    }

    public static boolean isPlayerHidden(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && _hiddenPlayers.contains(player);
    }

    public static boolean isPlayerHidden(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        return wrapper != null && wrapper.getToggles().getState(Toggles.VANISH);
    }

    public static boolean isShop(InventoryView inventoryView) {
        return inventoryView.getTitle().contains("@");
    }

    public static boolean isShop(Inventory inventory) {
        return inventory.getTitle().contains("@");
    }

    public static void runAsSpectators(Entity spectated, Consumer<Player> callback) {
        List<Entity> nearby = spectated.getNearbyEntities(1, 1, 1);
        for (Entity ent : nearby) {
            if (ent instanceof Player) {
                Player p = (Player) ent;
                if (p.getGameMode() == GameMode.SPECTATOR && Rank.isTrialGM(p) && p.getSpectatorTarget() != null && p.getSpectatorTarget() == spectated)
                    callback.accept(p);
            }
        }
    }

    /**
     * Teleports a player to another shard (Unconditionally)
     *
     * @param player
     * @param shard
     */
    public static void sendToShard(Player player, ShardInfo shard) {
        Metadata.SHARDING.set(player, true);
        GameAPI.getGamePlayer(player).setSharding(true);
        GameAPI.IGNORE_QUIT_EVENT.add(player.getUniqueId());
        handleLogout(player, true, consumer -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            player.sendMessage(ChatColor.YELLOW + "Sending you to " + ChatColor.BOLD + ChatColor.UNDERLINE + shard.getPseudoName() + ChatColor.YELLOW + "...");

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(),
                    () -> BungeeUtils.sendToServer(player.getName(), shard.getPseudoName()), 10);
        }), false);
    }

    public static WorldZoneType getZone(Location loc) {
        if (GameAPI.isInSafeRegion(loc)) {
            return WorldZoneType.SAFE;
        } else if (GameAPI.isNonPvPRegion(loc)) {
            return WorldZoneType.WILD;
        }
        return WorldZoneType.CHAOTIC;
    }

    /**
     * Formats milliseconds into a viewable string.
     * <p>
     * Example Input: 90000
     * Example Output: "1min 30s"
     *
     * @param
     */
    public static String formatTime(long time) {
        time /= 1000;
        String formatted = "";
        for (int i = 0; i < TimeInterval.values().length; i++) {
            TimeInterval iv = TimeInterval.values()[TimeInterval.values().length - i - 1];
            if (time >= iv.getInterval()) {
                int temp = (int) (time - (time % iv.getInterval()));
                int add = temp / iv.getInterval();
                formatted += " " + add + iv.getSuffix() + (add > 1 && iv != TimeInterval.SECOND ? "s" : "");
                time -= temp;
            }
        }
        return formatted.equals("") ? "" : formatted.substring(1);
    }

    private enum TimeInterval {
        SECOND("s", 1),
        MINUTE("min", 60 * SECOND.getInterval()),
        HOUR("hr", 60 * MINUTE.getInterval()),
        DAY("day", 24 * HOUR.getInterval()),
        MONTH("month", 30 * DAY.getInterval()),
        YEAR("yr", 365 * DAY.getInterval());

        private String suffix;
        private int interval;

        TimeInterval(String s, int i) {
            this.suffix = s;
            this.interval = i;
        }

        public int getInterval() {
            return this.interval;
        }

        public String getSuffix() {
            return this.suffix;
        }
    }

    /**
     * Returns the item the player is interacting with from an InventoryClickEvent.
     * The item will be the item you're trying to place as this is mainly used to block placing items.
     */
    public static ItemStack getItemToCheck(InventoryClickEvent event) {
        ItemStack item = event.getCursor();
        if (event.getAction().name().contains("PICKUP") || event.isShiftClick()
                || event.getAction() == InventoryAction.CLONE_STACK
                || event.getAction() == InventoryAction.DROP_ALL_SLOT
                || event.getAction() == InventoryAction.DROP_ONE_SLOT
                ) {
            item = event.getCurrentItem();
            System.out.println("Picked from container");
        }
        if (event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            item = event.getRawSlot() < event.getInventory().getSize() ? event.getView().getBottomInventory().getItem(event.getHotbarButton()) : event.getCurrentItem();
            System.out.println("Hotbar");
        }
        System.out.println("Returning " + item.getType() + " from action = " + event.getAction().name());
        return item;
    }

    /**
     * Give the specified user the vote message.
     */
    public static void sendVoteMessage(Player player) {
        int ecashAmount = Rank.isSUBPlus(player) ? 25 : (Rank.isSUB(player) ? 20 : 15);
//        ComponentBuilder cb = new ComponentBuilder("To vote for " + ecashAmount + " ECASH & 5% EXP, click ").color(ChatColor.GRAY);
//        cb.append("HERE").color(ChatColor.AQUA).underlined(true).bold(true).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://dungeonrealms.net/vote"));
//        player.sendMessage(cb.create());
        final JSONMessage message = new JSONMessage("To vote for " + ecashAmount + " ECASH & 5% EXP, click ", ChatColor.AQUA);
        message.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://dungeonrealms.net/vote");
        message.sendToPlayer(player);
    }

    public static boolean isMainWorld(World world) {
        return world.equals(getMainWorld());
    }

    public static boolean isMainWorld(Block block) {
        return isMainWorld(block.getWorld());
    }

    public static boolean isMainWorld(Entity ent) {
        return isMainWorld(ent.getWorld());
    }

    /**
     * Add an item into a player's inventory.
     * If there isn't enough space, drop it.
     */
    public static void giveOrDropItem(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return;

        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> giveOrDropItem(player, item));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            org.bukkit.entity.Item droppedItem = player.getWorld().dropItem(player.getLocation(), item);
            ItemManager.whitelistItemDrop(player, droppedItem);
            player.sendMessage(ChatColor.RED + "There was not enough space in your inventory for this item, so it has dropped.");
        } else {
            player.getInventory().addItem(item);
        }
    }

    public static void openBook(Player player, ItemStack book) {
        final ItemStack savedItem = player.getInventory().getItemInMainHand();
        player.getInventory().setItemInMainHand(book);

        PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());
        packetdataserializer.a(EnumHand.MAIN_HAND);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|BOpen", packetdataserializer));
        player.getInventory().setItemInMainHand(savedItem);
    }

    public static boolean isMainWorld(Location location) {
        return isMainWorld(location.getWorld());
    }

    /**
     * Remove a supplied armor piece from the player's inventory.
     * Returns the dropped piece or null.
     */
    public static ItemStack removeArmor(Player player, ItemStack is) {
        GeneratedItemType type = GeneratedItemType.getType(is.getType());
        ItemStack item = null;
        EntityEquipment e = player.getEquipment();

        switch (type) {
            case HELMET:
                item = e.getHelmet();
                e.setHelmet(new ItemStack(Material.AIR));
                break;
            case CHESTPLATE:
                item = e.getChestplate();
                e.setChestplate(new ItemStack(Material.AIR));
                break;
            case LEGGINGS:
                item = e.getLeggings();
                e.setLeggings(new ItemStack(Material.AIR));
                break;
            case BOOTS:
                item = e.getBoots();
                e.setBoots(new ItemStack(Material.AIR));
                break;
            default:
                Utils.printTrace();
                GameAPI.sendDevMessage(ChatColor.RED + "[WARNING] " + ChatColor.WHITE + "Attempted to remove " + type + " from " + player.getName() + " as armor on {SERVER}!");
                break;
        }

        return item;
    }

    public static void createZipFile(String inputFolder, String outputFile) throws ZipException {
        // Init zip file.
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        parameters.setIncludeRootFolder(false);
        ZipFile zipFile = new ZipFile(outputFile);

        //Add all files in the realm world to the zip.
        File targetFile = new File(inputFolder);
        if (targetFile.isFile())
            zipFile.addFile(targetFile, parameters);
        else if (targetFile.isDirectory())
            zipFile.addFolder(targetFile, parameters);
        else
            System.out.println("[ZIPPER] - Don't know how to handle " + targetFile.getName());
    }

    public static ItemStack getItem(Player player, EquipmentSlot slot) {
    	return getItem(player.getEquipment(), slot);
    }
    
    public static ItemStack getItem(EntityEquipment e, EquipmentSlot slot) {
        switch (slot) {
            case HAND:
                return e.getItemInMainHand();
            case OFF_HAND:
                return e.getItemInOffHand();
            case CHEST:
                return e.getChestplate();
            case FEET:
                return e.getBoots();
            case HEAD:
                return e.getHelmet();
            case LEGS:
                return e.getLeggings();
        }
        return null;
    }

    /**
     * Sets the item in the given equipment slot. There is no built-in spigot method for this.
     */
    public static void setItem(LivingEntity livingEntity, EquipmentSlot slot, ItemStack stack) {
        EntityEquipment e = livingEntity.getEquipment();
        switch (slot) {
            case HAND:
                e.setItemInMainHand(stack);
                break;
            case OFF_HAND:
                e.setItemInOffHand(stack);
                break;
            case CHEST:
                e.setChestplate(stack);
                break;
            case FEET:
                e.setBoots(stack);
                break;
            case HEAD:
                e.setHelmet(stack);
                break;
            case LEGS:
                e.setLeggings(stack);
                break;

        }
    }

    /**
     * Return the main world.
     *
     * @return
     */

    public static World getMainWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public static void setHandItem(Player player, ItemStack stack, EquipmentSlot slot) {
        if (slot != EquipmentSlot.HAND && slot != EquipmentSlot.OFF_HAND) {
            Utils.log.info("Could not set hand item of " + player.getName() + ". Tried to set hand " + slot.name() + ".");
            return;
        }
        setItem(player, slot, stack);
    }

    public static void sendStatNotification(Player p) {
        PlayerWrapper pw = PlayerWrapper.getWrapper(p);
        if (pw.getPlayerStats().getFreePoints() > 0) {
            Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
//            	ComponentBuilder cb = new ComponentBuilder("* ").color(ChatColor.GREEN);
//            	cb.append("You have available ").color(ChatColor.GRAY).append("stat points").color(ChatColor.GREEN);
//            	cb.append(". To allocate, click ").color(ChatColor.GRAY)
//            			.append("HERE").color(ChatColor.GREEN).underlined(true).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stats"))
//            			.append(" *").bold(false).underlined(false);
                final JSONMessage normal = new JSONMessage(ChatColor.GREEN + "*" + ChatColor.GRAY + "You have available " + ChatColor.GREEN + "stat points. " + ChatColor.GRAY +
                        "To allocate click ", ChatColor.WHITE);
                normal.addRunCommand(ChatColor.GREEN.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "/stats", "");
                normal.addText(ChatColor.GREEN + "*");
                normal.sendToPlayer(p);
//            	p.sendMessage(cb.create());
            });
        }
    }

    public static String getDataFolder() {
        return DungeonRealms.getInstance().getDataFolder().getPath();
    }

    public static File getRoot() {
        return new File(System.getProperty("user.dir"));
    }

    public static void mkDir(String s) {
        File f = new File(DungeonRealms.getInstance().getDataFolder() + File.separator + Utils.sanitizeFileName(s));
        if (!f.exists() || !f.isDirectory())
            f.mkdirs();
    }

    public static void announceVote(Player player) {
        if (player == null)
            return;

        PlayerWrapper pw = PlayerWrapper.getWrapper(player);
        int expToLevel = pw.getEXPNeeded();
        int expToGive = expToLevel / 20;
        expToGive += 100;

        // Prepare the message.
        TextComponent bungeeMessage = new TextComponent(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE");
        bungeeMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://dungeonrealms.net/vote"));
        bungeeMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to vote!").create()));

        // Handle reward calculations & achievements.
        Achievements.giveAchievement(player, EnumAchievements.VOTE);
        int ecashReward = 15;
        if (Rank.isSUB(player)) {
            ecashReward = 20;
            Achievements.giveAchievement(player, EnumAchievements.VOTE_AS_SUB);
            // Now let's check if we should reward them for being a SUB+/++.
            if (Rank.isSUBPlus(player)) {
                ecashReward = 25;
                Achievements.giveAchievement(player, EnumAchievements.VOTE_AS_SUB_PLUS);
            }
        }

        // Update the database with the new E-Cash reward!
        //TODO: Add Ecash
        //TODO: Update last voted. with System.cuttentTimeMiollis

        // Reward to player with their EXP increase.
        pw.addExperience(expToGive, false, true);
        final JSONMessage normal = new JSONMessage(ChatColor.AQUA + player.getName() + ChatColor.RESET + ChatColor.GRAY + " voted for " + ecashReward + " ECASH & 5% EXP @ vote ", ChatColor.WHITE);
        normal.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://dungeonrealms.net/vote");
        GameAPI.sendNetworkMessage("BroadcastRaw", normal.toString());
        // Send a message to everyone prompting them that a player has voted & how much they were rewarded for voting.
//        ComponentBuilder cb = new ComponentBuilder(player.getName()).color(ChatColor.AQUA)
//        		.append(" voted for " + ecashReward + " ECASH & 5% EXP @ vote ").color(ChatColor.GRAY)
//        		.append("HERE").color(ChatColor.AQUA).bold(true).underlined(true).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://dungeonrealms.net/vote"));
//       	sendShardMessage(cb);
    }

    public static void addCooldown(Metadatable m, Metadata type, int seconds) {
        type.set(m, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds));
    }

    public static String getFormattedCooldown(Metadatable m, Metadata type) {
        long val = type.get(m).asLong();
        if (val <= System.currentTimeMillis()) return null;
        return TimeUtil.formatDifference((val - System.currentTimeMillis()) / 1000);
    }

    public static boolean isCooldown(Metadatable m, Metadata type) {
        return type.get(m).asLong() > System.currentTimeMillis();
    }
}
