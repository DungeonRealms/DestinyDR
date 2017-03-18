package net.dungeonrealms;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.player.rank.Subscription;
import net.dungeonrealms.common.game.util.AsyncUtils;
import net.dungeonrealms.common.game.util.CooldownProvider;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.game.achievements.AchievementManager;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.anticheat.PacketLogger;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import net.dungeonrealms.game.mastery.*;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.CurrencyTab;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.combat.CombatLogger;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.player.notice.Notice;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.ElementalDamage;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.shops.Shop;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.dungeonrealms.network.GameClient;
import net.minecraft.server.v1_9_R2.MinecraftServer;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagList;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.glow.GlowAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.activation.UnknownObjectException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Nick on 9/17/2015.
 */
@SuppressWarnings("unchecked")
public class GameAPI {

    /**
     * Thread-safe ConcurrentHashMap. Constant time searches instead of linear for
     * CopyOnWriteArrayList
     */
    public static Map<String, GamePlayer> GAMEPLAYERS = new ConcurrentHashMap<>();
    public static Set<Player> _hiddenPlayers = new HashSet<>();

    //public static CooldownProvider SAVE_DATA_COOLDOWN = new CooldownProvider();

    /**
     * Used to avoid double saving player data
     */
    public static Set<UUID> IGNORE_QUIT_EVENT = new HashSet<>();


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
     * Method for calculating how many players we are retaining.
     *
     * @param retentionPolicy Calculate for who joined in seconds
     * @return Returns how many players are we actually retaining
     */
    public static int calculatePlayerRetention(long retentionPolicy) {
        // GRAB ALL DOCUMENTS //
        FindIterable<Document> all = DatabaseInstance.playerData
                .find(Filters.gte(EnumData.FIRST_LOGIN.getKey(), (System.currentTimeMillis()) - (retentionPolicy * 1000)));

        final int[] retention = {0};

        // RUN CHECK BLOCK FOR EACH DOCUMENT //
        all.forEach(new com.mongodb.Block<Document>() {
            @Override
            public void apply(Document document) {
                int minsPlayed = (Integer) DatabaseAPI.getInstance().getData(EnumData.TIME_PLAYED, document);
                int level = (Integer) DatabaseAPI.getInstance().getData(EnumData.LEVEL, document);
                int bankGems = (Integer) DatabaseAPI.getInstance().getData(EnumData.GEMS, document);

                // APPLY STATIC RETENTION POLICY //
                if (minsPlayed >= 300 && level >= 8 && bankGems > 0)
                    retention[0]++;
            }
        });
        return retention[0];
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
            ApplicableRegionSet set = WorldGuardPlugin.inst().getRegionManager(location.getWorld())
                    .getApplicableRegions(location);
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


    public static ItemStack makeItemUntradeable(ItemStack item) {
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(ChatColor.GRAY + "Untradeable");
            meta.setLore(lore);
        }
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("subtype", "starter");
        nbtItem.setInteger("untradeable", 1);
        return nbtItem.getItem();
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

    public static Item.ItemTier getItemTier(ItemStack stack) {
        if (stack.getType() == Material.AIR || stack == null)
            return null;
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        if (!nms.hasTag() || nms.hasTag() && nms.getTag().hasKey("itemTier")) return null;

        return Item.ItemTier.getByTier(nms.getTag().getInt("itemTier"));
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
        GamePlayer gp = GameAPI.getGamePlayer(player);
        int level = gp.getLevel();
        int mob_level = kill.getMetadata("level").get(0).asInt();
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
        int chance = RandomHelper.getRandomNumberBetween(1, 1000);
        if (chance <= 10) {
            return new ItemGenerator().setRarity(Item.ItemRarity.UNIQUE).setTier(Item.ItemTier.getByTier(tier)).getArmorSet();
        } else if (chance <= 30) {
            return new ItemGenerator().setRarity(Item.ItemRarity.RARE).setTier(Item.ItemTier.getByTier(tier)).getArmorSet();
        } else if (chance <= 150) {
            return new ItemGenerator().setRarity(Item.ItemRarity.UNCOMMON).setTier(Item.ItemTier.getByTier(tier)).getArmorSet();
        } else {
            return new ItemGenerator().setRarity(Item.ItemRarity.COMMON).setTier(Item.ItemTier.getByTier(tier)).getArmorSet();
        }
    }

    public static Item.ItemRarity getItemRarity(boolean isElite) {
        int chance = RandomHelper.getRandomNumberBetween(1, 1000);
        if (isElite) chance *= 0.9;
        if (chance <= 10) {
            return Item.ItemRarity.UNIQUE;
        } else if (chance <= 30) {
            return Item.ItemRarity.RARE;
        } else if (chance <= 150) {
            return Item.ItemRarity.UNCOMMON;
        } else {
            return Item.ItemRarity.COMMON;
        }
    }

    public static ChatColor getTierColor(int tier) {
        if (tier == 1) {
            return ChatColor.WHITE;
        }
        if (tier == 2) {
            return ChatColor.GREEN;
        }
        if (tier == 3) {
            return ChatColor.AQUA;
        }
        if (tier == 4) {
            return ChatColor.LIGHT_PURPLE;
        }
        if (tier == 5) {
            return ChatColor.YELLOW;
        }
        return ChatColor.WHITE;
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

        final long restartTime = (Bukkit.getOnlinePlayers().size() * 25) + 100; // second per player plus 5 seconds

        ShopMechanics.deleteAllShops(true);

        Bukkit.getServer().setWhitelist(true);
        DungeonRealms.getInstance().setAcceptPlayers(false);
        DungeonRealms.getInstance().saveConfig();
        CombatLog.getInstance().getCOMBAT_LOGGERS().values().forEach(CombatLogger::handleTimeOut);
        Bukkit.getScheduler().cancelAllTasks();
        PacketLogger.INSTANCE.onDisable();
        GameAPI.logoutAllPlayers();

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN in 5s");
            DatabaseInstance.playerData.updateMany(Filters.eq("info.current", DungeonRealms.getInstance().bungeeName), new
                    Document(EnumOperators.$SET.getUO(), new Document("info.isPlaying", false)));
            DungeonRealms.getInstance().mm.stopInvocation();
            AsyncUtils.pool.shutdown();

            DatabaseInstance.mongoClient.close();
        }, restartTime);

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), Bukkit::shutdown, restartTime + 20 * 5);
    }


    public static void handleCrash() {
        Bukkit.getServer().setWhitelist(true);
        DungeonRealms.getInstance().setAcceptPlayers(false);
        DungeonRealms.getInstance().saveConfig();

        Constants.log.info("called handleCrash()...");

        final long terminateTime = (ScoreboardHandler.getInstance().PLAYER_SCOREBOARDS.size() * 1000) + 10000;

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

        ShopMechanics.deleteAllShops(true);
        Constants.log.info("Saved all player shops successfully.");

        Constants.log.info("Saving all player realms.");

        Realms.getInstance().saveAllRealms();

        Constants.log.info("Saved all player realms successfully.");

        Constants.log.info("Saving all players' sessions...");

        final long currentTime = System.currentTimeMillis();
        ScoreboardHandler.getInstance().PLAYER_SCOREBOARDS.keySet()
                .stream().forEach(uuid -> savePlayerData(uuid, false, doAfter -> {
            IGNORE_QUIT_EVENT.add(uuid);
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_PLAYING, true, false);
            GameAPI.sendNetworkMessage("MoveSessionToken", uuid.toString(), "false");
        }));

        System.out.println("Successfully saved all sessions in " + String.valueOf(System.currentTimeMillis() - currentTime) + "ms");

        DungeonRealms.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> DungeonRealms.getInstance().mm.stopInvocation());
        AsyncUtils.pool.shutdown();
        DatabaseInstance.mongoClient.close();
    }

    /**
     * @param pLevel
     * @param mob_level
     * @param reduction
     * @return integer
     */
    private static int calculateXP(int pLevel, int mob_level, double reduction) {
        int expToGive = (int) (((pLevel * 5) + 45) * (1 + (0.07 * (pLevel + (mob_level - pLevel)))));
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


    public static String getServerLoad() {
        double tps = MinecraftServer.getServer().recentTps[0];
        return ((tps >= 19.90) ? ChatColor.GREEN + "Very Low" : (tps > 19.0) ? ChatColor.GREEN + "Low" : (tps > 15.0) ? ChatColor.YELLOW + "Medium" : ChatColor.RED + "High");
    }


    /**
     * Requests an update for cached player data on target
     * player's server
     *
     * @param uuid Target
     */
    public static void updatePlayerData(UUID uuid) {
        // CHECK IF LOCAL //
        if (Bukkit.getPlayer(uuid) != null) return; // their player data has already been updated in PLAYERS

        // SENDS PACKET TO MASTER SERVER //
        sendNetworkMessage("Update", uuid.toString());
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
        getClient().sendNetworkMessage(task, message, contents);
    }

    public static void sendDevMessage(String message, String... contents) {
        getClient().sendNetworkMessage("DEVMessage", message.replace("{SERVER}", DungeonRealms.getInstance().bungeeName), contents);
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


    public static void setMobElement(net.minecraft.server.v1_9_R2.Entity ent, String element) {
        ent.getBukkitEntity().setMetadata("element", new FixedMetadataValue(DungeonRealms.getInstance(), element));
        String name = ent.getCustomName();
        String[] splitName = name.split(" ", 2);


        ElementalDamage damage = ElementalDamage.getFromName(element);
        if (damage != null) {
            if (damage == ElementalDamage.PURE)
                name = damage.getPrefixColor() + damage.getElementalDamagePrefix() + " " + name;
            else
                name = damage.getPrefixColor() + (splitName.length == 1 ? damage.getName() + " " + splitName[0] : splitName[0] + " " + damage.getName() + " " + splitName[1]);
        }
//        switch (element) {
//            case "pure":
//                name = ChatColor.GOLD + "Holy " + name;
//                break;
//            case "fire":
//                name = ChatColor.RED + (splitName.length == 1 ? "Fire " + splitName[0] : splitName[0] + " Fire " + splitName[1]);
//                break;
//            case "ice":
//                name = ChatColor.BLUE + (splitName.length == 1 ? "Ice " + splitName[0] : splitName[0] + " Ice " + splitName[1]);
//                break;
//            case "poison":
//                name = ChatColor.DARK_GREEN + (splitName.length == 1 ? "Poison " + splitName[0] : splitName[0] + " Poison " + splitName[1]);
//                break;
//            default:
//                break;
//        }

        ent.setCustomName(name.trim());
        ent.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), name.trim()));
        if (GameAPI.isWeapon(((LivingEntity) ent.getBukkitEntity()).getEquipment().getItemInMainHand())) {
            EnchantmentAPI.addGlow(((LivingEntity) ent.getBukkitEntity()).getEquipment().getItemInMainHand());
        }
    }

    public static boolean isMobElemental(LivingEntity ent) {
        return ent.hasMetadata("element");
    }

    public static String getMobElement(LivingEntity ent) {
        return ent.getMetadata("element").get(0).asString();
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
//        if (!location.getWorld().equals(Bukkit.getWorlds().get(0))) {
//            return false;
//        }
        ApplicableRegionSet region = getWorldGuard().getRegionManager(location.getWorld())
                .getApplicableRegions(location);
        return region.getFlag(DefaultFlag.PVP) != null && !region.allows(DefaultFlag.PVP)
                && region.getFlag(DefaultFlag.MOB_DAMAGE) != null && !region.allows(DefaultFlag.MOB_DAMAGE);
    }

    public static boolean isNonPvPRegion(Location location) {
        ApplicableRegionSet region = getWorldGuard().getRegionManager(location.getWorld())
                .getApplicableRegions(location);
        return region.getFlag(DefaultFlag.PVP) != null && !region.allows(DefaultFlag.PVP);
    }

    public static boolean isNonMobDamageRegion(Location location) {
        ApplicableRegionSet region = getWorldGuard().getRegionManager(location.getWorld())
                .getApplicableRegions(location);
        return region.getFlag(DefaultFlag.MOB_DAMAGE) != null && !region.allows(DefaultFlag.MOB_DAMAGE);
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
        List<Player> playersNearby = new ArrayList<>();
        for (Player player : location.getWorld().getPlayers()) {
            if ((!GameAPI.isPlayer(player) || GameAPI._hiddenPlayers.contains(player)) && !ignoreVanish) {
                continue;
            }
            if (location.distanceSquared(player.getLocation()) <= radius * radius) {
                if (!playersNearby.contains(player)) {
                    playersNearby.add(player);
                }
            }
        }
        return playersNearby;
    }

    public static boolean arePlayersNearby(Location location, int radius) {
        for (Player player : location.getWorld().getPlayers()) {
            if (!GameAPI.isPlayer(player) || GameAPI._hiddenPlayers.contains(player)) {
                continue;
            }
            if (location.distanceSquared(player.getLocation()) <= radius * radius) {
                return true;
            }
        }

        return false;
    }

    /**
     * Saves player data
     *
     * @param uuid
     * @since 1.0
     */
    public static boolean savePlayerData(UUID uuid, boolean async, Consumer<BulkWriteResult> doAfter) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null || DungeonRealms.getInstance().getLoggingIn().contains(player.getUniqueId())) {
            return false;
        }
        String name = (String) DatabaseAPI.getInstance().getData(EnumData.USERNAME, player.getUniqueId());
        if (name == null || name.length() < 1)
            return false;

        List<UpdateOneModel<Document>> operations = new ArrayList<>();
        Bson searchQuery = Filters.eq("info.uuid", uuid.toString());

        // BANK AND COLLECTION BIN
        if (BankMechanics.storage.containsKey(uuid)) {
            Inventory inv = BankMechanics.getInstance().getStorage(uuid).inv;
            if (inv != null)
                operations.add(new UpdateOneModel<>(searchQuery, new Document(EnumOperators.$SET.getUO(), new Document(EnumData.INVENTORY_STORAGE.getKey(), ItemSerialization.toString(inv)))));

            inv = BankMechanics.getInstance().getStorage(uuid).collection_bin;
            if (inv != null)
                operations.add(new UpdateOneModel<>(searchQuery, new Document(EnumOperators.$SET.getUO(), new Document(EnumData.INVENTORY_COLLECTION_BIN.getKey(), ItemSerialization.toString(inv)))));

            //Currency Tab?
        }

        // PLAYER ARMOR AND INVENTORY
        Inventory inv = player.getInventory();
        ArrayList<String> armor = new ArrayList<>();
        for (ItemStack stack : player.getEquipment().getArmorContents())
            if (stack == null || stack.getType() == Material.AIR) armor.add("");
            else armor.add(ItemSerialization.itemStackToBase64(stack));
        ItemStack offHand = player.getEquipment().getItemInOffHand();
        if (offHand == null || offHand.getType() == Material.AIR) armor.add("");
        else armor.add(ItemSerialization.itemStackToBase64(offHand));

        operations.add(new UpdateOneModel<>(searchQuery, new Document(EnumOperators.$SET.getUO(), new Document(EnumData.ARMOR.getKey(), armor))));
        operations.add(new UpdateOneModel<>(searchQuery, new Document(EnumOperators.$SET.getUO(), new Document(EnumData.INVENTORY.getKey(), ItemSerialization.toString(inv)))));

        String locationAsString;

        // LOCATION
        if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
            locationAsString = player.getLocation().getX() + "," + (player.getLocation().getY()) + ","
                    + player.getLocation().getZ() + "," + player.getLocation().getYaw() + ","
                    + player.getLocation().getPitch();
            operations.add(new UpdateOneModel<>(searchQuery, new Document(EnumOperators.$SET.getUO(), new Document(EnumData.CURRENT_LOCATION.getKey(), locationAsString))));
        }

        // MULE INVENTORY
        if (MountUtils.inventories.containsKey(uuid))
            operations.add(new UpdateOneModel<>(searchQuery, new Document(EnumOperators.$SET.getUO(), new Document(EnumData.INVENTORY_MULE.getKey(), ItemSerialization.toString(MountUtils.inventories.get(uuid))))));

        // LEVEL AND STATISTICS
        if (GAMEPLAYERS.size() > 0) {
            GamePlayer gp = GameAPI.getGamePlayer(player);
            if (gp != null) {
                operations.add(new UpdateOneModel<>(searchQuery, new Document(EnumOperators.$SET.getUO(), new Document(EnumData.EXPERIENCE.getKey(), gp.getPlayerEXP()))));
                gp.getPlayerStatistics().updatePlayerStatistics();
                gp.getStats().updateDatabase(false);
            }
        }

        // MISC
        operations.add(new UpdateOneModel<>(searchQuery, new Document(EnumOperators.$SET.getUO(), new Document(EnumData.CURRENT_FOOD.getKey(), player.getFoodLevel()))));
        operations.add(new UpdateOneModel<>(searchQuery, new Document(EnumOperators.$SET.getUO(), new Document(EnumData.HEALTH.getKey(), HealthHandler.getInstance().getPlayerHPLive(player)))));

        KarmaHandler.getInstance().saveToMongo(player);

        //  QUEST DATA  //
        Quests.getInstance().savePlayerToMongo(player);

        DatabaseAPI.getInstance().bulkUpdate(operations, async, doAfter);
        return true;
    }

    /**
     * Safely logs out the player, updates their database inventories etc.
     *
     * @param uuid
     * @param async should always be true unless you need a callback from this method and are asyncing it via a
     *              different method.
     * @since 1.0
     */
    public static void handleLogout(UUID uuid, boolean async, Consumer<BulkWriteResult> doAfter) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        if (DungeonRealms.getInstance().getLoggingIn().contains(player.getUniqueId())) return;

        Utils.log.info("Handling logout for " + uuid.toString());
        DungeonRealms.getInstance().getLoggingOut().add(player.getName());

        // Fix invalid session IDs
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_PLAYING, false, true, true);

        GuildMechanics.getInstance().doLogout(player);

        // HANDLE REALM LOGOUT SYNC //
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Realms.getInstance().doLogout(player));

        Chat.listenForMessage(player, null, null);

        // save player data
        savePlayerData(uuid, async, doAfterSave -> {
            List<UpdateOneModel<Document>> operations = new ArrayList<>();
            Bson searchQuery = Filters.eq("info.uuid", uuid.toString());

            for (DamageTracker tracker : HealthHandler.getInstance().getMonsterTrackers().values()) {
                tracker.removeDamager(player);
            }
            if (player.getWorld().getName().contains("DUNGEON")) {
                for (ItemStack stack : player.getInventory().getContents()) {
                    if (stack != null && stack.getType() != Material.AIR) {
                        if (DungeonManager.getInstance().isDungeonItem(stack)) {
                            player.getInventory().remove(stack);
                        }
                    }
                }
            }
            if (BankMechanics.shopPricing.containsKey(player.getName())) {
                player.getInventory().addItem(BankMechanics.shopPricing.get(player.getName()));
                BankMechanics.shopPricing.remove(player.getName());
            }
            if (GameAPI._hiddenPlayers.contains(player)) {
                GameAPI._hiddenPlayers.remove(player);
            }
            if (!DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) {
                Utils.log.info(player.getUniqueId() + " has already been saved.");
                return;
            }
            if (CombatLog.isInCombat(player)) {
                if (!DuelingMechanics.isDueling(uuid)) {
                    if (!GameAPI.isNonPvPRegion(player.getLocation())) {
                        //CombatLog.handleCombatLogger(player);
                    }
                }
            }
            MountUtils.inventories.remove(uuid);
            operations.add(new UpdateOneModel<>(searchQuery, new Document(EnumOperators.$SET.getUO(), new Document(EnumData.LAST_LOGOUT.getKey(), System.currentTimeMillis()))));
            EnergyHandler.getInstance().handleLogoutEvents(player);
            HealthHandler.getInstance().handleLogoutEvents(player);
            KarmaHandler.getInstance().handleLogoutEvents(player);
            Quests.getInstance().handleLogoutEvents(player);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                ScoreboardHandler.getInstance().removePlayerScoreboard(player);
            });
            if (EntityAPI.hasPetOut(uuid)) {
                net.minecraft.server.v1_9_R2.Entity pet = EntityMechanics.PLAYER_PETS.get(uuid);
                pet.dead = true;
                if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(pet)) {
                    DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(pet);
                }
                EntityAPI.removePlayerPetList(uuid);
            }
            if (EntityAPI.hasMountOut(uuid)) {
                net.minecraft.server.v1_9_R2.Entity mount = EntityMechanics.PLAYER_MOUNTS.get(uuid);
                if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(mount)) {
                    DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(mount);
                }
                if (mount.isAlive()) { // Safety check
                    if (mount.passengers != null) {
                        mount.passengers.forEach(passenger -> passenger = null);
                    }
                    mount.dead = true;
                }
                EntityAPI.removePlayerMountList(uuid);
            }

            if (Affair.getInstance().isInParty(player)) {
                Affair.getInstance().removeMember(player, false);
            }

            operations.add(new UpdateOneModel<>(searchQuery, new Document(EnumOperators.$SET.getUO(), new Document(EnumData.IS_PLAYING.getKey(), false))));

            DatabaseAPI.getInstance().bulkUpdate(operations, async, doAfterAfterUpdate -> {
                DungeonRealms.getInstance().getLoggingOut().remove(player.getName());
                DatabaseAPI.getInstance().PLAYERS.remove(player.getUniqueId());
                GAMEPLAYERS.remove(player.getName());
                Utils.log.info("Saved information for uuid: " + uuid.toString() + " on their logout.");

                if (doAfter != null)
                    doAfter.accept(doAfterSave);
            });
        });
        return;
    }

    /**
     * Safely logs out all players when the server restarts. Saves their data async before.
     *
     * @since 1.0
     */
    public static void logoutAllPlayers() {
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);

        for (int i = 0; i < players.length; i++) {
            final Player player = players[i];
            final boolean sub = Rank.isSubscriber(player);

            player.sendMessage(ChatColor.AQUA + ">>> This DungeonRealms shard is " + ChatColor.BOLD + "RESTARTING.");

            if (!DungeonRealms.getInstance().isDrStopAll) {
                player.sendMessage(" ");
                player.sendMessage(ChatColor.GRAY + "Your current game session has been paused while you are transferred.");
                player.sendMessage(" ");
            }

            // Handle pvp log first
            if (CombatLog.inPVP(player)) CombatLog.removeFromPVP(player);

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
                }

                if (DungeonRealms.getInstance().isDrStopAll) {

                    // SEND THEM TO THE LOBBY NORMALLY INSTEAD //
                    BungeeUtils.sendToServer(player.getName(), "Lobby");
                    return;
                } else GameAPI.IGNORE_QUIT_EVENT.add(player.getUniqueId());

                // upload data and send to server
                GameAPI.handleLogout(player.getUniqueId(), true, consumer -> {
                    if (CombatLog.isInCombat(player)) CombatLog.removeFromCombat(player);
                    String name = player.getName();
                    DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 5); //Prevents dungeon entry for 5 seconds.
                    if (ShopMechanics.ALLSHOPS != null && !ShopMechanics.ALLSHOPS.isEmpty()) {
                        // Second shop deletion handler
                        ShopMechanics.getShop(name).deleteShop(true);
                    }
                    // Move
                    GameAPI.sendNetworkMessage("MoveSessionToken", player.getUniqueId().toString(), String.valueOf(sub));
                });

            }, (i + 1) * 4);
        }
    }

    public static void sendStopAllServersPacket() {
        sendNetworkMessage("Stop", "");
    }

    public static void handleLogin(UUID uuid) {
        if (Bukkit.getPlayer(uuid) == null) {
            return;
        }
        Player player = Bukkit.getPlayer(uuid);

        if (!DatabaseAPI.getInstance().PLAYERS.containsKey(uuid)) {
            player.kickPlayer(ChatColor.RED + "Unable to grab your data, please reconnect!");
            return;
        } else if (player != null) {
            player.sendMessage(ChatColor.GREEN + "Successfully received your data, loading...");

            if (!DungeonRealms.getInstance().canAcceptPlayers() && !Rank.isDev(player)) {
                player.kickPlayer(ChatColor.RED + "This shard has not finished it's startup process.");
                return;
            } else if (DungeonRealms.getInstance().isSubscriberShard && Rank.getInstance().getRank(player.getUniqueId()).equalsIgnoreCase("default")) {
                player.kickPlayer(ChatColor.RED + "You are " + ChatColor.UNDERLINE + "not" + ChatColor.RED + " authorized to connect to a subscriber only shard.\n\n" +
                        ChatColor.GRAY + "Subscriber at http://www.dungeonrealms.net/shop to gain instant access!");
                return;
            } else if ((DungeonRealms.getInstance().isYouTubeShard && !Rank.isYouTuber(player)) || (DungeonRealms.getInstance().isSupportShard && !Rank.isSupport(player))) {
                player.kickPlayer(ChatColor.RED + "You are " + ChatColor.UNDERLINE + "not" + ChatColor.RED + " authorized to connect to this shard.");
                return;
            }
        } else {
            return;
        }

        createNewData(player);

        try {
            if ((Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_COMBAT_LOGGED, uuid)) {
                if (!DatabaseAPI.getInstance().getData(EnumData.CURRENTSERVER, uuid).equals(DungeonRealms.getShard().getPseudoName())) {
                    String lastShard = ShardInfo.getByPseudoName((String) DatabaseAPI.getInstance().getData(EnumData.CURRENTSERVER, uuid)).getShardID();
                    player.kickPlayer(ChatColor.RED + "You have combat logged. Please connect to Shard " + lastShard);
                    return;
                }
            }
        } catch (NullPointerException ignored) {
        }

        if (player.hasMetadata("sharding")) player.removeMetadata("sharding", DungeonRealms.getInstance());

        // todo: finish anticheat system
        //AntiCheat.getInstance().getUids().addAll((HashSet<String>)DatabaseAPI.getInstance().getData(EnumData.ITEMUIDS, uuid));

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

        DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 60);
        //Prevent players entering a dungeon as they spawn.

        String playerInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY, uuid);
        if (playerInv != null && playerInv.length() > 0 && !playerInv.equalsIgnoreCase("null")) {
            ItemStack[] items = ItemSerialization.fromString(playerInv, 36).getContents();
            player.getInventory().setContents(items);
            player.updateInventory();
        }
        List<String> playerArmor = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ARMOR, player.getUniqueId());
        int i = -1;
        ItemStack[] armorContents = new ItemStack[4];
        ItemStack offHand = new ItemStack(Material.AIR);
        for (String armor : playerArmor) {
            i++;
            if (i <= 3) { //Normal armor piece
                if (armor.equals("null") || armor.equals("")) {
                    armorContents[i] = new ItemStack(Material.AIR);
                } else {
                    armorContents[i] = ItemSerialization.itemStackFromBase64(armor);
                }
            } else {
                if (armor.equals("null") || armor.equals("")) {
                    offHand = new ItemStack(Material.AIR);
                } else {
                    offHand = ItemSerialization.itemStackFromBase64(armor);
                }

            }
            player.getEquipment().setArmorContents(armorContents);
            player.getEquipment().setItemInOffHand(offHand);

            player.updateInventory();
        }

        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
            String source = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_STORAGE, uuid);
            if (source != null && source.length() > 0 && !source.equalsIgnoreCase("null")) {
                int size = (int) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_LEVEL, uuid);
                //Auto set the inventory size based off level? min 9, max 54
                Inventory inv = ItemSerialization.fromString(source, Math.max(9, Math.min(54, size * 9)));
                Storage storageTemp = new Storage(uuid, inv);
                BankMechanics.storage.put(uuid, storageTemp);
            } else {
                Storage storageTemp = new Storage(uuid);
                BankMechanics.storage.put(uuid, storageTemp);
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                // Anticheat
                AntiDuplication.getInstance().handleLogin(player);
            });

        });

        CurrencyTab currencyTab = new CurrencyTab(player.getUniqueId());
        currencyTab.loadCurrencyTab(tab -> {
            if (tab.hasAccess)
                Bukkit.getLogger().info("Loaded currency tab for " + player.getName());
        });
        BankMechanics.getInstance().getCurrencyTab().put(player.getUniqueId(), currencyTab);

        String invString = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_MULE, player.getUniqueId());
        int muleLevel = (int) DatabaseAPI.getInstance().getData(EnumData.MULELEVEL, player.getUniqueId());
        if (muleLevel > 3) {
            muleLevel = 3;
        }
        MuleTier tier = MuleTier.getByTier(muleLevel);
        Inventory muleInv = null;
        if (tier != null) {
            muleInv = Bukkit.createInventory(player, tier.getSize(), "Mule Storage");
            if (!invString.equalsIgnoreCase("") && !invString.equalsIgnoreCase("empty") && invString.length() > 4) {
                //Make sure the inventory is as big as we need
                muleInv = ItemSerialization.fromString(invString, tier.getSize());
            }
        }
        if (!invString.equalsIgnoreCase("") && !invString.equalsIgnoreCase("empty") && invString.length() > 4 && muleInv != null)
            MountUtils.inventories.put(player.getUniqueId(), muleInv);
        TeleportAPI.addPlayerHearthstoneCD(uuid, 150);
        if (!DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, uuid).equals("")) {
            String[] locationString = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, uuid))
                    .split(",");
            player.teleport(new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationString[0]),
                    Double.parseDouble(locationString[1]), Double.parseDouble(locationString[2]),
                    Float.parseFloat(locationString[3]), Float.parseFloat(locationString[4])));
        } else {
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.FIRST_LOGIN, System.currentTimeMillis(), true);
            //TutorialMechanics.getInstance().doLogin(player);
             /*PLAYER IS NEW*/
            //sendNetworkMessage("Broadcast", ChatColor.translateAlternateColorCodes('&', "&e" + player.getName() + " &6has joined &6&lDungeon Realms &6for the first time!"));
            ItemManager.giveStarter(player, true);

            // Fix missing journal & portal rune
            player.getInventory().setItem(8, ItemManager.createCharacterJournal(Bukkit.getPlayer(uuid)));
            player.getInventory().setItem(7, ItemManager.createRealmPortalRune(uuid));

            if (DungeonRealms.getInstance().isEventShard) {
                int level = 50;
                GameAPI.getGamePlayer(player).updateLevel(level, false, true);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.LEVEL, level, true);
                player.teleport(TeleportLocation.EVENT_AREA.getLocation());
            } else {
                player.teleport(TeleportLocation.STARTER.getLocation());
            }
        }

        // Essentials
        //Subscription.getInstance().handleJoin(player);
        Rank.getInstance().doGet(uuid);

        // Scoreboard Safety

        player.setGameMode(GameMode.SURVIVAL);

        for (int j = 0; j < 20; j++) {
            player.sendMessage("");
        }
        player.setMaximumNoDamageTicks(0);
        player.setNoDamageTicks(0);

        Utils.sendCenteredMessage(player, ChatColor.WHITE.toString() + ChatColor.BOLD + "Dungeon Realms Build " + String.valueOf(Constants.BUILD_NUMBER));
        Utils.sendCenteredMessage(player, ChatColor.GRAY + "http://www.dungeonrealms.net/");
        Utils.sendCenteredMessage(player, ChatColor.YELLOW + "You are on the " + ChatColor.BOLD + DungeonRealms.getInstance().shardid + ChatColor.YELLOW + " shard.");

        player.sendMessage(new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Type " + ChatColor.YELLOW.toString() + ChatColor.ITALIC + "/shard" + ChatColor.GRAY.toString() + ChatColor.ITALIC + " to change your shard instance at any time.",
        });

        if (DungeonRealms.getInstance().isMasterShard) {
            player.sendMessage(new String[]{
                    "",
                    ChatColor.DARK_AQUA + "This is the Dungeon Realms " + ChatColor.UNDERLINE + "MASTER" + ChatColor.DARK_AQUA + " shard.",
                    ChatColor.GRAY + "Changes made on this shard will be deployed to all other shards as a " + ChatColor.UNDERLINE + "content patch" + ChatColor.GRAY + "."
            });
        }
        if (DungeonRealms.getInstance().isSupportShard && Rank.isSupport(player)) {
            player.sendMessage(new String[]{
                    "",
                    ChatColor.DARK_AQUA + "This is a " + ChatColor.UNDERLINE + "CUSTOMER SUPPORT" + ChatColor.DARK_AQUA + " shard."
            });
        }
        if (DungeonRealms.getInstance().isRoleplayShard) {
            player.sendMessage(new String[]{
                    "",
                    ChatColor.DARK_AQUA + "This is a " + ChatColor.UNDERLINE + "ROLEPLAY" + ChatColor.DARK_AQUA + " shard. Local chat should always be in character, Global/Trade chat may be OOC.",
                    ChatColor.GRAY + "Please be respectful to those who want to roleplay. You " + ChatColor.UNDERLINE + "will" + ChatColor.GRAY + " be banned for trolling / local OOC."
            });
        }
        if (DungeonRealms.getInstance().isBrazilianShard) {
            player.sendMessage(new String[]{
                    "",
                    ChatColor.DARK_AQUA + "This is a " + ChatColor.UNDERLINE + "BRAZILIAN" + ChatColor.DARK_AQUA + " shard.",
                    ChatColor.GRAY + "The official language of this server is " + ChatColor.UNDERLINE + "Portuguese."
            });
        }
        if (DungeonRealms.getInstance().isBetaShard) {
            player.sendMessage(new String[]{
                    "",
                    ChatColor.DARK_AQUA + "This is a " + ChatColor.UNDERLINE + "BETA" + ChatColor.DARK_AQUA + " shard.",
                    ChatColor.GRAY + "You will be testing " + ChatColor.UNDERLINE + "new" + ChatColor.GRAY + " and " + ChatColor.UNDERLINE + "unfinished" + ChatColor.GRAY + " versions of Dungeon Realms.",
                    ChatColor.GRAY + "Report all bugs at: " + ChatColor.BOLD + ChatColor.UNDERLINE + "http://bug.dungeonrealms.net/"
            });
        }
        if (DungeonRealms.getInstance().isEventShard) {
            player.sendMessage(new String[]{
                    "",
                    ChatColor.DARK_AQUA + "This is an " + ChatColor.UNDERLINE + "EVENT" + ChatColor.DARK_AQUA + " shard.",
                    ChatColor.GRAY.toString() + ChatColor.ITALIC + "Please be aware that data is not synchronized with the live shard.",
                    ChatColor.GRAY.toString() + ChatColor.ITALIC + "This shard is only accessible for a limited time.",
            });
        }

        player.sendMessage("");

        // Player Achievements
        // Don't use a switch because flowing through isn't possible due to different criteria.
        if (Rank.isDev(player)) {
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.DEVELOPER);
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.INFECTED);
        }

        if (Rank.isTrialGM(player)) {
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.GAME_MASTER);
        }

        if (Rank.isSupport(player)) {
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SUPPORT_AGENT);
        }

        if (Rank.isPMOD(player)) {
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.PLAYER_MOD);
        }
        if (Rank.isSubscriber(player)) {
            String rank = Rank.getInstance().getRank(player.getUniqueId()).toLowerCase();
            // We don't want to award PMODs with subscriber ranks because this is a rank that can be lost.
            // If they lose it, we don't want to account them for paying for a rank they've not.
            if (!rank.equals("pmod")) {
                Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SUBSCRIBER);
                if (!rank.equals("sub") && !rank.equals("hiddenmod")) {
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SUBSCRIBER_PLUS);
                    if (!rank.equals("sub+")) {
                        Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SUBSCRIBER_PLUS_PLUS);
                    }
                }
            }
        }

        // Quests
        if (Quests.isEnabled())
            Quests.getInstance().handleLogin(player);

        // Fatigue
        EnergyHandler.getInstance().handleLoginEvents(player);

        // Alignment
        KarmaHandler.getInstance().handleLoginEvents(player);

        // Subscription
        Subscription.getInstance().handleLogin(player);

        // Guilds
        GuildMechanics.getInstance().doLogin(player);

        // Notices
        Notice.getInstance().doLogin(player);

        createNewData(player);

        // Newbie Protection
        //ProtectionHandler.getInstance().handleLogin(player);
        //Unfinished, correct way to remove it was never implemented. Should be after 3 PvP attacks.

        // Free E-Cash
        int freeEcash = (int) (Long.valueOf(DatabaseAPI.getInstance().getData(EnumData.FREE_ECASH, uuid).toString()) / 1000);
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        if (currentTime - freeEcash >= 86400) {
            int ecashReward = Utils.randInt(10, 15);
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.FREE_ECASH, System.currentTimeMillis(), true);
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, EnumData.ECASH, ecashReward, true);
            player.sendMessage(new String[]{
                    ChatColor.GOLD + "You have gained " + ChatColor.BOLD + ecashReward + "EC" + ChatColor.GOLD + " for logging into DungeonRealms today!",
                    ChatColor.GRAY + "Use /ecash to spend your EC, you can obtain more e-cash by logging in daily or by voting " + ChatColor.GOLD + ChatColor.UNDERLINE + "http://dungeonrealms.net/vote"
            });
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
        }

        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.USERNAME, player.getName().toLowerCase(), true);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.CURRENTSERVER, DungeonRealms.getInstance().bungeeName, true);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_PLAYING, true, true);

        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("IP");

            player.sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
        });

        sendNetworkMessage("Friends", "join:" + " ," + player.getUniqueId().toString() + "," + player.getName() + "," + DungeonRealms.getInstance().shardid);

        Utils.log.info("Fetched information for uuid: " + uuid.toString() + " on their login.");
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> AchievementManager.getInstance().handleLogin(player.getUniqueId()), 70L);
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

        // calculate attributes and check inventory
        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
            GameAPI.calculateAllAttributes(player);
            PlayerManager.checkInventory(uuid);
        }, 2 * 20L);

        if (gp.getPlayer() != null) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
                if (gp.getStats().freePoints > 0) {
                    final JSONMessage normal = new JSONMessage(ChatColor.GREEN + "*" + ChatColor.GRAY + "You have available " + ChatColor.GREEN + "stat points. " + ChatColor.GRAY +
                            "To allocate click ", ChatColor.WHITE);
                    normal.addRunCommand(ChatColor.GREEN.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "/stats");
                    normal.addText(ChatColor.GREEN + "*");
                    normal.sendToPlayer(gp.getPlayer());
                }
            }, 100);
        }

        if (Rank.isTrialGM(player)) {
            HealthHandler.getInstance().setPlayerMaxHPLive(player, 10000);
            HealthHandler.getInstance().setPlayerHPLive(player, 10000);

            gp.setInvulnerable(true);
            gp.setTargettable(false);
            player.sendMessage("");

            Utils.sendCenteredMessage(player, ChatColor.AQUA + ChatColor.BOLD.toString() + "GM INVINCIBILITY");

            // check vanish
            final Object isVanished = DatabaseAPI.getInstance().getData(EnumData.TOGGLE_VANISH, player.getUniqueId());
            if (isVanished != null && (Boolean) isVanished) {
                GameAPI._hiddenPlayers.add(player);
                player.setCustomNameVisible(false);
                Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(player));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
                Utils.sendCenteredMessage(player, ChatColor.AQUA + ChatColor.BOLD.toString() + "GM VANISH");
                GameAPI.sendNetworkMessage("vanish", uuid.toString(), "true");
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                player.setGameMode(GameMode.CREATIVE);
            }
        }

        DungeonRealms.getInstance().getLoggingIn().remove(player.getUniqueId());

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            //Prevent weird scoreboard thing when sharding.
            ScoreboardHandler.getInstance().matchMainScoreboard(player);
            ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, gp.getPlayerAlignmentDB().getAlignmentColor(), gp.getLevel());
        }, 100L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            DonationEffects.getInstance().doLogin(player);
        }, 100L);
    }

    /**
     * Creates data that was not present on the original release of DR.
     * (Prevents NPEs)
     */
    private static void createNewData(Player player) {
        UUID uuid = player.getUniqueId();
        createIfMissing(uuid, EnumData.TOGGLE_DAMAGE_INDICATORS, true);
        createIfMissing(uuid, EnumData.QUEST_DATA, new JsonArray().toString());
        createIfMissing(uuid, EnumData.TOGGLE_GLOW, true);
    }

    private static void createIfMissing(UUID uuid, EnumData data, Object setTo) {
        if (DatabaseAPI.getInstance().getData(data, uuid) == null)
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, data, setTo, true);
    }

    /**
     * type used to switch shard
     *
     * @param player           Player
     * @param serverBungeeName Bungee name
     */
    public static void moveToShard(Player player, String serverBungeeName) {
        GameAPI.IGNORE_QUIT_EVENT.add(player.getUniqueId());

        // prevent any interaction while the data is being uploaded
        Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(player));
        player.setInvulnerable(true);
        player.setNoDamageTicks(10);
        player.closeInventory();
        player.setCanPickupItems(false);
        player.setMetadata("sharding", new FixedMetadataValue(DungeonRealms.getInstance(), true));

        GamePlayer gp = GameAPI.getGamePlayer(player);
        gp.setAbleToSuicide(false);
        gp.setAbleToDrop(false);

        // check if they're still here (server failed to accept them for some reason)
        new PlayerLogoutWatchdog(player);

        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.LAST_SHARD_TRANSFER, System.currentTimeMillis(), true,
                doAfter -> GameAPI.handleLogout(player.getUniqueId(), true,
                        consumer -> BungeeUtils.sendToServer(player.getName(), serverBungeeName))
        );
    }

    static void backupDatabase() {
        if (Bukkit.getOnlinePlayers().size() == 0) return;
        DungeonRealms.getInstance().getLogger().info("Beginning Mongo Database Backup");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) {
                return;
            }
            UUID uuid = player.getUniqueId();
            savePlayerData(uuid, true, doAfter -> Utils.log.info("Backed up information for uuid: " + uuid.toString()));
        }
        DungeonRealms.getInstance().getLogger().info("Completed Mongo Database Backup");
    }

    public static String locationToString(Location location) {
        return location.getX() + "," + (location.getY() + 1) + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
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
     * Returns the string is a Particle Trail
     *
     * @param trailType
     * @return boolean
     * @since 1.0
     */
    public static boolean isStringTrail(String trailType) {
        return ParticleAPI.ParticleEffect.getByName(trailType.toUpperCase()) != null;
    }

    /**
     * Returns if the entity is an actual player and not a Citizens NPC
     *
     * @param entity
     * @return boolean
     * @since 1.0
     */
    public static boolean isPlayer(Entity entity) {
        return entity instanceof Player && !(entity.hasMetadata("NPC") && !(entity.hasMetadata("npc")));
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
                .filter(mons -> mons.getLocation().distance(location) <= radius && mons.hasMetadata("type")
                        && mons.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile"))
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

    public static boolean removePortalShardsFromPlayer(Player player, int shardTier, int amount) {
        if (amount <= 0) {
            return true;
            // Someone done fucked up and made it remove a negative amount.
            // Probably Chase.
        }
        EnumData dataToCheck;
        switch (shardTier) {
            case 1:
                dataToCheck = EnumData.PORTAL_SHARDS_T1;
                break;
            case 2:
                dataToCheck = EnumData.PORTAL_SHARDS_T2;
                break;
            case 3:
                dataToCheck = EnumData.PORTAL_SHARDS_T3;
                break;
            case 4:
                dataToCheck = EnumData.PORTAL_SHARDS_T4;
                break;
            case 5:
                dataToCheck = EnumData.PORTAL_SHARDS_T5;
                break;
            default:
                return false;
        }
        int playerPortalKeyShards = (int) DatabaseAPI.getInstance().getData(dataToCheck, player.getUniqueId());
        if (playerPortalKeyShards <= 0) {
            return false;
        }
        if (playerPortalKeyShards - amount >= 0) {
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, dataToCheck, (amount * -1),
                    true);
            return true;
        } else {
            return false;
        }
    }

    public static File getRemoteDataFolder() {
        String filePath = DungeonRealms.getInstance().getDataFolder().getAbsolutePath();
        File file = DungeonRealms.getInstance().getDataFolder();
        if (filePath.contains("/home/servers")) {
            if (filePath.contains("d1")) {
                filePath = "d1";
            } else if (filePath.contains("d2")) {
                filePath = "d2";
            } else if (filePath.contains("d3")) {
                filePath = "d3";
            } else if (filePath.contains("d4")) {
                filePath = "d4";
            } else if (filePath.contains("d5")) {
                filePath = "d5";
            }
            String webRoot = "/home/servers/" + filePath + "/";
            file = new File(webRoot, DungeonRealms.getInstance().getDataFolder() + "");
        }
        return file;
    }

    public static void setAttributeVal(Item.AttributeType type, Integer[] val, Player p) {
        GameAPI.getGamePlayer(p).setAttributeVal(type, val);
    }

    public static Integer[] changeAttributeVal(Item.AttributeType type, Integer[] difference, Player p) {
        if (GameAPI.getGamePlayer(p) != null) {
            if (type != null) {
                return GameAPI.getGamePlayer(p).changeAttributeVal(type, difference);
            }
        }
        return new Integer[]{0, 0};
    }

    public static int getStaticAttributeVal(Item.AttributeType type, Player p) {
        return GameAPI.getGamePlayer(p).getStaticAttributeVal(type);
    }

    /**
     * Gets the value of a player's specified attribute. Must be a non-range
     * attribute.
     *
     * @param type
     * @param p
     * @return if a ranged attribute, throws an error message and returns -1.
     */
    public static Integer[] getRangedAttributeVal(Item.AttributeType type, Player p) {
        if (GameAPI.getGamePlayer(p) != null) {
            if (type != null) {
                return GameAPI.getGamePlayer(p).getRangedAttributeVal(type);
            }
        }
        return new Integer[]{0, 0};
    }

    /**
     * Given an attribute, gets the total value of the attribute from the player's
     * armor and weapon if applicable. Even if the AttributeType passed is an Armor
     * or Weapon Attribute Type, the type will still try to calculate the total
     * from the player's armor or weapon if applicable. For the attributes damage
     * and health, takes into account benefits given from stats (str, dex, vit, int).
     *
     * @param type - an attribute, can be either an armor or weapon attribute
     * @param p    - the player to calculate the total value for
     * @return - the total value of the attribute from the player's equipment. If the
     * attribute has ranged values, the first index is the min and second the max.
     * Otherwise, the first index is the value.
     * @since 2.0
     */
    public static int[] calculateAttribute(Item.AttributeType type, Player p) {
        if (type instanceof Item.ArmorAttributeType) { // armor type
            Item.ArmorAttributeType armorType = (Item.ArmorAttributeType) type;
            ItemStack[] armorSet = p.getInventory().getArmorContents();
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = null;
            NBTTagCompound tag = null;

            for (ItemStack armor : armorSet) {
                if (!GameAPI.isArmor(armor)) {
                    continue;
                }
                nmsStack = CraftItemStack.asNMSCopy(armor);
                tag = nmsStack.getTag();

                if (tag.hasKey(type.getNBTName()) || tag.hasKey(type.getNBTName() + "Max")) {

                }
            }

            // check if a weapon can also have this attribute
            if (Item.WeaponAttributeType.getByName(armorType.getName()) != null) {

            }
        } else if (type instanceof Item.WeaponAttributeType) {
            Item.WeaponAttributeType weaponType = (Item.WeaponAttributeType) type;

            // check if armor can also have this attribute
            if (Item.ArmorAttributeType.getByName(weaponType.getName()) != null) {

            }
        }

        return new int[]{0, 0};
    }

    public static String getItemUID(ItemStack i) {
        if (!(GameAPI.isWeapon(i))) return "";
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return "";
        if (tag.hasKey("u")) return tag.getString("u");
        return "";
    }

    /**
     * Calculates the value for all attributes and loads it into memory. Calculates
     * both armor and weapon attributes. Called on player login.
     *
     * @param p - Player that needs attribute calculation
     * @return - the HashMap in the player attributes object containing each attribute
     * as a key along with respective total attribute value as the value.
     * @since 2.0
     */
    public static Map<String, Integer[]> calculateAllAttributes(Player p) {
        Map<String, Integer[]> attributes = new HashMap<>();
        GamePlayer gp = GameAPI.getGamePlayer(p);
        assert gp != null;

        // calculate from armor and weapon, then update the gp attributes property
        calculateArmorAttributes(attributes, p.getInventory().getArmorContents(), true);
        calculateWeaponAttributes(attributes, p.getInventory().getItemInMainHand(), true);

        gp.setCurrentWeapon(getItemUID(p.getEquipment().getItemInMainHand()));

        // add stat bonuses from the stat menu
        changeAttributeVal(attributes, Item.ArmorAttributeType.STRENGTH, gp.getStats().strPoints);
        changeAttributeVal(attributes, Item.ArmorAttributeType.DEXTERITY, gp.getStats().dexPoints);
        changeAttributeVal(attributes, Item.ArmorAttributeType.INTELLECT, gp.getStats().intPoints);
        changeAttributeVal(attributes, Item.ArmorAttributeType.VITALITY, gp.getStats().vitPoints);

        // apply stat bonuses (str, dex, int, and vit)
        applyStatBonuses(attributes, gp);

        // set the player's attribute field
        gp.setAttributes(attributes);

        if (!gp.isAttributesLoaded())
            HealthHandler.getInstance().handleLoginEvents(p);

        // so energy regen doesn't start before attributes have been loaded
        gp.setAttributesLoaded(true);

        return attributes;
    }

    public static void calculateAllAttributes(LivingEntity ent, Map<String, Integer[]> attributes) {
        ItemStack[] armorSet = ent.getEquipment().getArmorContents().clone();

        // check if we have a skull
        int intTier = ent.getMetadata("tier").get(0).asInt();
        Item.ItemTier tier = Item.ItemTier.getByTier(intTier);
        if (armorSet[3].getType() == Material.SKULL_ITEM && (intTier >= 3 || new Random().nextInt(10) <= (6 + intTier))) {
            // if we have a skull we need to generate a helmet so mob stats are calculated correctly
            armorSet[3] = new ItemGenerator().setTier(tier).setRarity(GameAPI.getItemRarity(ent.hasMetadata("elite"))).generateItem().getItem();
        }

        calculateArmorAttributes(attributes, armorSet, true);
        calculateWeaponAttributes(attributes, ent.getEquipment().getItemInMainHand(), true);
        applyStatBonuses(attributes);
    }

    /**
     * Calculates the difference in attributes when a player switches weapons. To save processing
     * power, only called when the player attempts to attack a mob with a new weapon.
     * <p>
     * NOTE: if attributes like str are added to weapons, this type will have to be called
     * whenever a player switches weapons.
     *
     * @param p
     * @param newWeapon
     */
    public static void handlePlayerWeaponSwitch(Player p, ItemStack newWeapon, ItemStack oldWeapon) {
        GamePlayer gp = GameAPI.getGamePlayer(p);
        assert gp != null;

        if (!GameAPI.isWeapon(newWeapon) && !GameAPI.isWeapon(oldWeapon)) return;

        if (newWeapon == null || newWeapon.getType() == Material.AIR) {
            List<String> oldModifiers = GameAPI.getModifiers(oldWeapon);
            net.minecraft.server.v1_9_R2.NBTTagCompound oldTag = CraftItemStack.asNMSCopy(oldWeapon).getTag();
            // iterate through to get decreases from stats not in the new armor
            oldModifiers.forEach(modifier -> {
                Item.WeaponAttributeType type = Item.WeaponAttributeType.getByNBTName(modifier);
                // calculate new values
                Integer[] newTotalVal = type.isRange()
                        ? new Integer[]{gp.getRangedAttributeVal(type)[0] - oldTag.getInt(modifier + "Min"),
                        gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier + "Max")}
                        : new Integer[]{0, gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier)};
                gp.setAttributeVal(type, newTotalVal);
            });
        } else {
            List<String> newModifiers = GameAPI.getModifiers(newWeapon);
            net.minecraft.server.v1_9_R2.NBTTagCompound newTag = CraftItemStack.asNMSCopy(newWeapon).getTag();

            if (oldWeapon != null && oldWeapon.getType() != Material.AIR) {
                List<String> oldModifiers = GameAPI.getModifiers(oldWeapon);
                net.minecraft.server.v1_9_R2.NBTTagCompound oldTag = CraftItemStack.asNMSCopy(oldWeapon).getTag();

                // tbh, the milliseconds saved by writing this type and handleArmorDifferences probably wasn't
                // worth the effort...

                // get differences
                if (newModifiers != null) {
                    newModifiers.forEach(modifier -> {
                        // get the attribute type to determine if we need a percentage or not and to get the
                        // correct display name
                        Item.WeaponAttributeType type = Item.WeaponAttributeType.getByNBTName(modifier);

                        if (type.isRange()) {
                            gp.changeAttributeVal(type, new Integer[]{newTag.getInt(modifier + "Min") - ((oldTag != null) ?
                                    oldTag.getInt(modifier + "Min") : 0), newTag.getInt(modifier + "Max") - ((oldTag != null) ?
                                    oldTag.getInt(modifier + "Max") : 0)});
                        } else {
                            gp.changeAttributeVal(type, new Integer[]{0, newTag.getInt(modifier) - ((oldTag != null) ? oldTag
                                    .getInt(modifier) : 0)});
                        }
                    });
                }

                if (oldModifiers != null) {
                    // iterate through to get decreases from stats not in the new armor
                    oldModifiers.removeAll(newModifiers);
                    oldModifiers.forEach(modifier -> {
                        Item.WeaponAttributeType type = Item.WeaponAttributeType.getByNBTName(modifier);
                        Integer[] newTotalVal = type.isRange()
                                ? new Integer[]{gp.getRangedAttributeVal(type)[0] - oldTag.getInt(modifier + "Min"),
                                gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier + "Max")}
                                : new Integer[]{0, gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier)};
                        gp.setAttributeVal(type, newTotalVal);
                    });
                }
            } else {
                newModifiers.forEach(modifier -> {
                    // get the attribute type to determine if we need a percentage or not and to get the
                    // correct display name
                    Item.WeaponAttributeType type = Item.WeaponAttributeType.getByNBTName(modifier);
                    // calculate new values
                    Integer[] newTotalVal = type.isRange()
                            ? new Integer[]{gp.getRangedAttributeVal(type)[0] + newTag.getInt(modifier + "Min"),
                            gp.getRangedAttributeVal(type)[1] + newTag.getInt(modifier + "Max")}
                            : new Integer[]{0, gp.getRangedAttributeVal(type)[1] + newTag.getInt(modifier)};
                    gp.setAttributeVal(type, newTotalVal);
                });
            }
        }

        gp.setCurrentWeapon(GameAPI.getItemUID(newWeapon));
    }

    /**
     * Given an attributes Map, applies the appropriate bonuses to various
     * non-damage related attributes from the strength, vitality, dexterity,
     * and intellect values in the Map.
     *
     * @param attributes
     */
    public static void applyStatBonuses(Map<String, Integer[]> attributes) {
        // STRENGTH BONUSES
        float strength = (float) attributes.getOrDefault("strength", new Integer[]{0, 0})[1];
        changeAttributeVal(attributes, Item.ArmorAttributeType.ARMOR, strength * 0.03f);
        changeAttributeVal(attributes, Item.ArmorAttributeType.BLOCK, strength * 0.017f);

        // DEXTERITY BONUSES
        float dexterity = (float) attributes.getOrDefault("dexterity", new Integer[]{0, 0})[1];
        changeAttributeValPercentage(attributes, Item.ArmorAttributeType.DAMAGE, dexterity * 0.03f);
        changeAttributeVal(attributes, Item.ArmorAttributeType.DODGE, dexterity * 0.017f);
        changeAttributeVal(attributes, Item.WeaponAttributeType.ARMOR_PENETRATION, dexterity * 0.02f);

        // INTELLECT BONUSES
        float intellect = (float) attributes.getOrDefault("intellect", new Integer[]{0, 0})[1];
        changeAttributeVal(attributes, Item.ArmorAttributeType.ENERGY_REGEN, intellect * 0.015f);
        changeAttributeVal(attributes, Item.WeaponAttributeType.CRITICAL_HIT, intellect * 0.025f);

        // VITALITY BONUSES
        float vitality = (float) attributes.getOrDefault("vitality", new Integer[]{0, 0})[1];
        changeAttributeValPercentage(attributes, Item.ArmorAttributeType.HEALTH_POINTS, vitality * 0.034f);
        changeAttributeVal(attributes, Item.ArmorAttributeType.HEALTH_REGEN, vitality * 0.03f);
    }

    public static void applyStatBonuses(Map<String, Integer[]> attributes, GamePlayer gp) {
        Map<Item.AttributeType, Float> attributeBonusesFromStats = gp.getAttributeBonusesFromStats();
        // STRENGTH BONUSES
        float strength = (float) attributes.getOrDefault("strength", new Integer[]{0, 0})[1];
        changeAttributeVal(attributes, Item.ArmorAttributeType.ARMOR, strength * 0.03f);
        attributeBonusesFromStats.put(Item.ArmorAttributeType.ARMOR, strength * 0.03f);
        changeAttributeVal(attributes, Item.ArmorAttributeType.BLOCK, strength * 0.017f);
        attributeBonusesFromStats.put(Item.ArmorAttributeType.BLOCK, strength * 0.017f);

        // DEXTERITY BONUSES
        float dexterity = (float) attributes.getOrDefault("dexterity", new Integer[]{0, 0})[1];
        changeAttributeVal(attributes, Item.ArmorAttributeType.DAMAGE, dexterity * 0.03f);
        attributeBonusesFromStats.put(Item.ArmorAttributeType.DAMAGE, dexterity * 0.03f);
        changeAttributeVal(attributes, Item.ArmorAttributeType.DODGE, dexterity * 0.017f);
        attributeBonusesFromStats.put(Item.ArmorAttributeType.DODGE, dexterity * 0.017f);
        changeAttributeVal(attributes, Item.WeaponAttributeType.ARMOR_PENETRATION, dexterity * 0.02f);
        attributeBonusesFromStats.put(Item.WeaponAttributeType.ARMOR_PENETRATION, dexterity * 0.02f);

        // INTELLECT BONUSES
        float intellect = (float) attributes.getOrDefault("intellect", new Integer[]{0, 0})[1];
        changeAttributeVal(attributes, Item.ArmorAttributeType.ENERGY_REGEN, intellect * 0.015f);
        attributeBonusesFromStats.put(Item.ArmorAttributeType.ENERGY_REGEN, intellect * 0.015f);
        changeAttributeVal(attributes, Item.WeaponAttributeType.CRITICAL_HIT, intellect * 0.025f);
        attributeBonusesFromStats.put(Item.WeaponAttributeType.CRITICAL_HIT, intellect * 0.025f);

        // VITALITY BONUSES
        float vitality = (float) attributes.getOrDefault("vitality", new Integer[]{0, 0})[1];
        changeAttributeValPercentage(attributes, Item.ArmorAttributeType.HEALTH_POINTS, vitality * 0.034f);
        attributeBonusesFromStats.put(Item.ArmorAttributeType.HEALTH_POINTS, vitality * 0.034f);
        changeAttributeVal(attributes, Item.ArmorAttributeType.HEALTH_REGEN, vitality * 0.03f);
        attributeBonusesFromStats.put(Item.ArmorAttributeType.HEALTH_REGEN, vitality * 0.03f);
    }

    /**
     * Recalculates the stat bonuses for a player by reading their attributesBonusesFromStats property in their GamePlayer.
     *
     * @param attributes
     * @param attributeBonusesFromStats
     * @param gp
     */
    public static void recalculateStatBonuses(Map<String, Integer[]> attributes, Map<Item.AttributeType, Float> attributeBonusesFromStats, GamePlayer gp) {
        attributeBonusesFromStats.entrySet().forEach(entry -> {
            if (entry.getKey().isPercentage() || entry.getKey().equals(Item.ArmorAttributeType.HEALTH_REGEN)) {
                changeAttributeVal(attributes, Item.ArmorAttributeType.HEALTH_REGEN, -Math.round(entry.getValue()));
            } else {
                gp.changeAttributeValPercentage(entry.getKey(), -Math.round(entry.getValue()));
            }
        });
        applyStatBonuses(attributes, gp);
    }

    /**
     * Changes the value of an attribute by the given difference. This difference
     * may be positive or negative.
     *
     * @param type
     * @param difference
     * @return the new value of the attribute
     */
    private static Integer[] changeAttributeVal(Map<String, Integer[]> attributes, Item.AttributeType type, float difference) {
        Integer[] oldVal = attributes.getOrDefault(type.getNBTName(), new Integer[]{0, 0});
        Integer[] newTotalVal = new Integer[]{Math.round(oldVal[0] + difference), Math.round(oldVal[1] + difference)};
        attributes.put(type.getNBTName(), newTotalVal);
        return newTotalVal;
    }

    /**
     * Changes the value of an attribute by a percent difference. This difference
     * may be positive or negative.
     *
     * @param type
     * @param percentDifference
     * @return the new value of the attribute
     */
    private static Integer[] changeAttributeValPercentage(Map<String, Integer[]> attributes, Item.AttributeType type, float percentDifference) {
        int newLow = (int) Math.round(attributes.get(type.getNBTName())[0] * ((percentDifference + 100.) / 100.));
        int newHigh = (int) Math.round(attributes.get(type.getNBTName())[1] * ((percentDifference + 100.) / 100.));
        if (newLow < 0) newLow = 0;
        if (newHigh < 0) newHigh = 0;
        attributes.put(type.getNBTName(), new Integer[]{newLow, newHigh});
        return new Integer[]{newLow, newHigh};
    }

    /**
     * Puts the attributes on an armorset in the given attributes map. Does not
     * necessarily update the gameplayer attributes field. Called in the
     * calculateAllAttributes type.
     *
     * @param attributes
     * @param armorSet
     * @param includeAbsentAttributes if true add all attributes not present
     *                                to map with values 0, 0
     */
    public static void calculateArmorAttributes(Map<String, Integer[]> attributes, ItemStack[] armorSet, boolean includeAbsentAttributes) {
        // populate the map with default values if necessary
        if (includeAbsentAttributes) {
            for (Item.ArmorAttributeType type : Item.ArmorAttributeType.values()) {
                attributes.putIfAbsent(type.getNBTName(), new Integer[]{0, 0});
            }
        }
        // iterate through armorset
        for (ItemStack armor : armorSet) {
            if (!GameAPI.isArmor(armor)) continue;

            List<String> modifiers = GameAPI.getModifiers(armor);
            NBTTagCompound tag = CraftItemStack.asNMSCopy(armor).getTag();
            assert tag != null;

            modifiers.forEach(modifier -> {
                Item.ArmorAttributeType type = Item.ArmorAttributeType.getByNBTName(modifier);
                assert type != null;

                if (type.isRange()) {
                    attributes.put(type.getNBTName(), new Integer[]{attributes.getOrDefault(modifier, new
                            Integer[]{0, 0})[0] + tag.getInt(modifier + "Min"), attributes.getOrDefault(modifier, new
                            Integer[]{0, 0})[1] + tag.getInt(modifier + "Max")});
                } else {
                    attributes.put(type.getNBTName(), new Integer[]{0, attributes.get(modifier)[1] + tag.getInt(modifier)});
                }
            });
        }
    }

    /**
     * Puts the attributes on a given weapon in the given attributes map. Does
     * not necessarily update the gameplayer attributes field. Called in the
     * calculateAllAttributes type.
     *
     * @param attributes
     * @param weapon
     * @param includeAbsentAttributes if true add all attributes not present
     *                                to map with values 0, 0
     */
    public static void calculateWeaponAttributes(Map<String, Integer[]> attributes, ItemStack weapon, boolean includeAbsentAttributes) {
        // populate the map with default values
        if (includeAbsentAttributes) {
            for (Item.WeaponAttributeType type : Item.WeaponAttributeType.values()) {
                attributes.putIfAbsent(type.getNBTName(), new Integer[]{0, 0});
            }
        }

        if (GameAPI.isWeapon(weapon)) {
            List<String> modifiers = GameAPI.getModifiers(weapon);
            NBTTagCompound tag = CraftItemStack.asNMSCopy(weapon).getTag();
            assert tag != null;

            modifiers.forEach(modifier -> {
                Item.WeaponAttributeType type = Item.WeaponAttributeType.getByNBTName(modifier);
                assert type != null;

                if (type.isRange()) {
                    attributes.put(type.getNBTName(), new Integer[]{attributes.getOrDefault(modifier, new
                            Integer[]{0, 0})[0] + tag.getInt(modifier + "Min"), attributes.getOrDefault(modifier, new
                            Integer[]{0, 0})[1] + tag.getInt(modifier + "Max")
                    });
                } else {
                    attributes.put(type.getNBTName(), new Integer[]{0, attributes.getOrDefault(modifier, new
                            Integer[]{0, 0})[1] + tag.getInt(modifier)});
                }
            });
        }
    }

    /**
     * Gets all the modifier names of an item.
     *
     * @param item
     * @return - null if the item does not contain any modifiers
     */
    public static List<String> getModifiers(ItemStack item) {
        if (item == null) return null;
        List<String> modifiersList = new ArrayList<>();
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        if (!nmsStack.hasTag()) return null;
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return null;
        if (tag.hasKey("modifiers")) {
            NBTTagList list = tag.getList("modifiers", 8);
            for (int i = 0; i < list.size(); i++) {
                modifiersList.add(list.getString(i));
            }
            return modifiersList;
        }
        return null;
    }

    public static boolean isWeapon(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return false;
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return !(nms == null || nms.getTag() == null) && nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("weapon");
    }

    public static boolean isArmor(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return false;
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return !(nms == null || nms.getTag() == null) && nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("armor");
    }

    /**
     * @param is
     * @return
     */
    public static boolean isOrb(ItemStack is) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(is);
        return !(nms == null || nms.getTag() == null) && is.getType() == Material.MAGMA_CREAM && nms.getTag() != null && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("orb");
    }

    public static String getCustomID(ItemStack i) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(i);
        if (nms == null || nms.getTag() == null) return null;
        NBTTagCompound tag = nms.getTag();
        return tag.hasKey("drItemId") ? tag.getString("drItemId") : null;
    }

    /*
     This stuff is disabled because Soulbound achieves the same thing, and we should avoid giving items that we take back later.
     (Because there are way too many ways to lose those items such as breaking, dropping, etc. etc.)
     
     public static ItemStack setQuestBound(ItemStack item, String owner, UUID uuid){
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add(ChatColor.DARK_RED + "Quest Item");
        meta.setLore(lore);
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setInteger("questBound", 1);
        nbtItem.setString("owner", owner);
        nbtItem.setString("ownerUUID", uuid.toString());
        return nbtItem.getItem();
    }
    
    public static boolean isQuestBound(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        if (nms == null || nms.getTag() == null) {
            return false;
        }
        NBTTagCompound tag = nms.getTag();
        return tag.hasKey("questBound") && tag.getInt("questBound") == 1;
    }
    
    /**
     * Is this player the original owner of the quest item?
     * This will return true if the item is not questbound.
     * 
     *
    public static boolean isRightfulOwnerOfQuestItem(Player player, ItemStack item){
    	if(!isQuestBound(item))
    		return true;
    	net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        if (nms == null || nms.getTag() == null) return false;
        NBTTagCompound tag = nms.getTag();
        boolean isOwner = tag.hasKey("ownerUUID") && tag.getString("ownerUUID").equals(player.getUniqueId().toString());
        if(!isOwner)
        	player.sendMessage(ChatColor.RED + "This quest item does not belong to you.");
    	return isOwner;
    }*/

    public static boolean isItemTradeable(ItemStack itemStack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
        if (nms != null && nms.getTag() != null) {
            if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("important")) {
                return false;
            }
            if (nms.getTag().hasKey("subtype") && nms.getTag().getString("subtype").equalsIgnoreCase("starter")) {
                return false;
            }
            if (nms.getTag().hasKey("untradeable") && nms.getTag().getInt("untradeable") == 1) {
                return false;
            }
        }
//        if(isItemSoulbound(itemStack))return false;
        return true;
    }

    public static boolean isItemDroppable(ItemStack itemStack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
        if (nms != null && nms.getTag() != null) {
            if (nms.getTag().hasKey("subtype") && nms.getTag().getString("subtype").equalsIgnoreCase("nondrop")) {
                return false;
            }
        }
        return true;
    }

    public static boolean isItemUntradeable(ItemStack item) {
        return !isItemTradeable(item);
    }

    public static boolean isItemSoulbound(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        if (nms == null || nms.getTag() == null) return false;
        NBTTagCompound tag = nms.getTag();
        return (tag.hasKey("soulbound") && tag.getInt("soulbound") == 1);
    }

    public static boolean isItemPermanentlyUntradeable(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        if (nms == null || nms.getTag() == null) return false;
        NBTTagCompound tag = nms.getTag();
        return (tag.hasKey("untradeable") && tag.getInt("untradeable") == 1) || (tag.hasKey("puntradeable") && tag.getInt("puntradeable") == 1);
    }

    /**
     * Spawn our Entity at Location
     * <p>
     * Use SpawningMechanics.getMob for Entity
     * lvlRange = "high" or "low"
     *
     * @param location
     * @param entity
     * @param tier
     * @param lvlRange
     */
    public void spawnMonsterAt(Location location, net.minecraft.server.v1_9_R2.Entity entity, int tier, String lvlRange) {
        net.minecraft.server.v1_9_R2.World world = ((CraftWorld) location.getWorld()).getHandle();
        int level = Utils.getRandomFromTier(tier, "low");
        EntityStats.setMonsterRandomStats(entity, level, tier);
        String lvlName = ChatColor.AQUA.toString() + "[Lvl. " + level + "] ";
        int hp = entity.getBukkitEntity().getMetadata("currentHP").get(0).asInt();
        String customName = entity.getBukkitEntity().getMetadata("customname").get(0).asString();
        entity.setCustomName(lvlName + ChatColor.RESET + customName);
        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
        world.addEntity(entity, SpawnReason.CUSTOM);
        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
    }

    public static boolean isPlayerHidden(UUID uuid) {
        if (Bukkit.getPlayer(uuid) != null) return _hiddenPlayers.contains(Bukkit.getPlayer(uuid));
        final Object isVanished = DatabaseAPI.getInstance().getData(EnumData.TOGGLE_VANISH, uuid);
        return isVanished != null && (Boolean) isVanished;
    }

    public static boolean isPlayerHidden(Document document) {
        final Object isVanished = DatabaseAPI.getInstance().getData(EnumData.TOGGLE_VANISH, document);
        return isVanished != null && (Boolean) isVanished;
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
                if (p.getGameMode() == GameMode.SPECTATOR && Rank.isTrialGM(p) && p.getSpectatorTarget() != null && p.getSpectatorTarget() == spectated) {
                    callback.accept(p);
                }
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
        player.setMetadata("sharding", new FixedMetadataValue(DungeonRealms.getInstance(), true));
        GameAPI.getGamePlayer(player).setSharding(true);
        GameAPI.IGNORE_QUIT_EVENT.add(player.getUniqueId());
        handleLogout(player.getUniqueId(), true, consumer -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            player.sendMessage(ChatColor.YELLOW + "Sending you to " + ChatColor.BOLD + ChatColor.UNDERLINE + shard.getPseudoName() + ChatColor.YELLOW + "...");

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(),
                    () -> BungeeUtils.sendToServer(player.getName(), shard.getPseudoName()), 10);
        }));
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
    	int ecashAmount = 15;
        if (Rank.isSubscriberPlus(player)) {
            ecashAmount = 25;
        } else if (Rank.isSubscriber(player)) {
            ecashAmount = 20;
        }
        final JSONMessage message = new JSONMessage("To vote for " + ecashAmount + " ECASH & 5% EXP, click ", ChatColor.AQUA);
        message.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://dungeonrealms.net/vote");
        message.sendToPlayer(player);
    }

	public static boolean isMainWorld(Location location) {
		return location.getWorld().equals(Bukkit.getWorlds().get(0));
	}
}
