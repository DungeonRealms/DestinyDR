package net.dungeonrealms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.dungeonrealms.game.achievements.AchievementManager;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.handlers.*;
import net.dungeonrealms.game.mastery.*;
import net.dungeonrealms.game.mechanics.DungeonManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mechanics.PlayerManager;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.player.notice.Notice;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.player.rank.Subscription;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMounts;
import net.dungeonrealms.game.world.entities.types.pets.EnumPets;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.dungeonrealms.game.world.items.Item.*;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagList;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.activation.UnknownObjectException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Nick on 9/17/2015.
 */
@SuppressWarnings("unchecked")
public class API {

    /**
     * Thread-safe ConcurrentHashMap. Constant time searches instead of linear for
     * CopyOnWriteArrayList
     */
    public static Map<String, GamePlayer> GAMEPLAYERS = new ConcurrentHashMap<>();
    public static Set<Player> _hiddenPlayers = new HashSet<>();

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

    public static ItemTier getItemTier(ItemStack stack) {
        if (stack.getType() == Material.AIR || stack == null)
            return null;
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        if (!nms.hasTag() || nms.hasTag() && nms.getTag().hasKey("itemTier")) return null;

        return ItemTier.getByTier(nms.getTag().getInt("itemTier"));
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
        GamePlayer gp = API.getGamePlayer(player);
        int level = gp.getLevel();
        int mob_level = kill.getMetadata("level").get(0).asInt();
        int xp = 0;
        if (mob_level > level + 20) {  // limit mob xp calculation to 10 levels above player level
            xp = calculateXP(player, kill, level + 20);
        } else {
            xp = calculateXP(player, kill, mob_level);
        }
        return xp;
    }

    public static ItemStack[] getTierArmor(int tier) {
        int chance = RandomHelper.getRandomNumberBetween(1, 1000);
        if (chance <= 10) {
            return new ItemGenerator().setRarity(ItemRarity.UNIQUE).setTier(ItemTier.getByTier(tier)).getArmorSet();
        } else if (chance <= 30) {
            return new ItemGenerator().setRarity(ItemRarity.RARE).setTier(ItemTier.getByTier(tier)).getArmorSet();
        } else if (chance <= 150) {
            return new ItemGenerator().setRarity(ItemRarity.UNCOMMON).setTier(ItemTier.getByTier(tier)).getArmorSet();
        } else {
            return new ItemGenerator().setRarity(ItemRarity.COMMON).setTier(ItemTier.getByTier(tier)).getArmorSet();
        }
    }

    public static ItemRarity getItemRarity(boolean isElite) {
        int chance = RandomHelper.getRandomNumberBetween(1, 1000);
        if (isElite) chance *= 0.9;
        if (chance <= 10) {
            return ItemRarity.UNIQUE;
        } else if (chance <= 30) {
            return ItemRarity.RARE;
        } else if (chance <= 150) {
            return ItemRarity.UNCOMMON;
        } else {
            return ItemRarity.COMMON;
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

    /**
     * @param player
     * @param kill
     * @param mob_level
     * @return integer
     */
    private static int calculateXP(Player player, Entity kill, int mob_level) {
        int pLevel = API.getGamePlayer(player).getStats().getLevel();
        return (int) (((pLevel * 5) + 45) * (1 + (0.07 * (pLevel + (mob_level - pLevel)))));
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


    /**
     * Requests an update for cached data on target
     * player's server
     *
     * @param uuid Target
     */
    public static void updatePlayerData(UUID uuid) {
        // SENDS PACKET ON MESSAGING CHANNEL //
        NetworkAPI.getInstance().sendNetworkMessage("DungeonRealms", "Update", uuid.toString());
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
    public static IAsyncWorldEdit getAsyncWorldEdit() {
        Plugin plugin = DungeonRealms.getInstance().getServer().getPluginManager().getPlugin("AsyncWorldEdit");
        if (plugin == null || !(plugin instanceof IAsyncWorldEdit)) {
            try {
                throw new UnknownObjectException("getAsyncWorldEdit() of API.class is RETURNING NULL!");
            } catch (UnknownObjectException e) {
                e.printStackTrace();
            }
        }
        return (IAsyncWorldEdit) plugin;
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
                throw new UnknownObjectException("getWorldGuard() of API.class is RETURNING NULL!");
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

    /**
     * Gets the a list of nearby players from a location within a given radius
     *
     * @param location
     * @param radius
     * @since 1.0
     */
    public static List<Player> getNearbyPlayers(Location location, int radius) {
        List<Player> playersNearby = new ArrayList<>();
        for (Player player : location.getWorld().getPlayers()) {
            if (!API.isPlayer(player) || API._hiddenPlayers.contains(player)) {
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

    /**
     * Safely logs out the player, updates their mongo inventories etc.
     *
     * @param uuid
     * @since 1.0
     */
    public static void handleLogout(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (!DatabaseAPI.getInstance().PLAYER_TIME.containsKey(uuid) || DatabaseAPI.getInstance().PLAYER_TIME.get(uuid) <= 5) {
            //Dont save.
            DatabaseAPI.getInstance().PLAYER_TIME.remove(uuid);
            return;
        }
        DatabaseAPI.getInstance().PLAYER_TIME.remove(uuid);
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
        if (!DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) {
            return;
        }
        if (CombatLog.isInCombat(player)) {
            if (!DuelingMechanics.isDueling(uuid)) {
                if (!API.isNonPvPRegion(player.getLocation())) {
                    CombatLog.handleCombatLogger(player);
                }
            }
        }

        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_PLAYING, false, false);
        if (BankMechanics.storage.containsKey(uuid)) {
            Inventory inv = BankMechanics.getInstance().getStorage(uuid).inv;
            if (inv != null) {
                String serializedInv = ItemSerialization.toString(inv);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_STORAGE, serializedInv, false);
            }
            inv = BankMechanics.getInstance().getStorage(uuid).collection_bin;
            if (inv != null) {
                String serializedInv = ItemSerialization.toString(inv);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, serializedInv, false);
            }
            BankMechanics.storage.remove(uuid);
        }
        Inventory inv = player.getInventory();
        ArrayList<String> armor = new ArrayList<>();
        for (ItemStack stack : player.getEquipment().getArmorContents()) {
            if (stack == null || stack.getType() == Material.AIR) {
                armor.add("");
            } else {
                armor.add(ItemSerialization.itemStackToBase64(stack));
            }
        }
        ItemStack offHand = player.getEquipment().getItemInOffHand();
        if (offHand == null || offHand.getType() == Material.AIR) {
            armor.add("");
        } else {
            armor.add(ItemSerialization.itemStackToBase64(offHand));
        }
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, armor, false);

        if (MountUtils.inventories.containsKey(uuid)) {
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_MULE, ItemSerialization.toString(MountUtils.inventories.get(uuid)), false);
            MountUtils.inventories.remove(uuid);
        }


        if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
            String locationAsString = "-367,86,390,0,0"; // Cyrennica
            locationAsString = player.getLocation().getX() + "," + (player.getLocation().getY() + 0.5) + ","
                    + player.getLocation().getZ() + "," + player.getLocation().getYaw() + ","
                    + player.getLocation().getPitch();
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, false);
        } else {
            //Dungeon or realm, should already have their last main world location saved.
        }
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.LAST_LOGOUT, System.currentTimeMillis() / 1000L, false);
        EnergyHandler.getInstance().handleLogoutEvents(player);
        HealthHandler.getInstance().handleLogoutEvents(player);
        KarmaHandler.getInstance().handleLogoutEvents(player);
        ScoreboardHandler.getInstance().removePlayerScoreboard(player);
        if (EntityAPI.hasPetOut(uuid)) {
            net.minecraft.server.v1_9_R2.Entity pet = Entities.PLAYER_PETS.get(uuid);
            pet.dead = true;
            EntityAPI.removePlayerPetList(uuid);
        }
        if (EntityAPI.hasMountOut(uuid)) {
            net.minecraft.server.v1_9_R2.Entity mount = Entities.PLAYER_MOUNTS.get(uuid);
            mount.dead = true;
            EntityAPI.removePlayerMountList(uuid);
        }
        String inventory = ItemSerialization.toString(inv);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, inventory, false);
        if (GAMEPLAYERS.size() > 0) {
            GamePlayer gp = API.getGamePlayer(player);
            if (gp != null) {
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.EXPERIENCE, gp.getPlayerEXP(), false);
                gp.getPlayerStatistics().updatePlayerStatistics();
                gp.getStats().updateDatabase(false);
                GAMEPLAYERS.remove(player.getName());
            }
        }
        DungeonRealms.getInstance().getLoggingOut().remove(player.getName());
        Utils.log.info("Saved information for uuid: " + uuid.toString() + " on their logout.");
    }

    /**
     * Safely logs out all players when the server restarts
     *
     * @since 1.0
     */
    public static void logoutAllPlayers(boolean customStop) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.AQUA + ">>> This DungeonRealms shard is " + ChatColor.BOLD + "RESTARTING.");
            if (CombatLog.isInCombat(player)) {
                CombatLog.removeFromCombat(player);
            }
            if (customStop) {
                API.handleLogout(player.getUniqueId());
                DungeonRealms.getInstance().getLoggingOut().add(player.getName());
                DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 5); //Prevents dungeon entry for 5 seconds.
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    NetworkAPI.getInstance().sendToServer(player.getName(), "Lobby");
                    DungeonRealms.getInstance().getLoggingOut().remove(player.getName());
                }, 10);
            }
        }
    }

    /**
     * Safely logs in the player, giving them their items, their storage and
     * their cooldowns
     *
     * @since 1.0
     */
    public static void handleLogin(UUID uuid) {
        if (Bukkit.getPlayer(uuid) == null) {
            return;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (!DatabaseAPI.getInstance().PLAYERS.containsKey(uuid)) {
            player.kickPlayer(ChatColor.RED + "Unable to grab your data, please reconnect!");
            return;
        } else {
            if (player != null) {
                player.sendMessage(ChatColor.GREEN + "Successfully received your data, loading...");

                if (!DungeonRealms.getInstance().hasFinishedSetup() && !Rank.isDev(player)) {
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
        }

        GamePlayer gp = new GamePlayer(player);

        DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 60);
        //Prevent players entering a dungeon as they spawn.

        String playerInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY, uuid);
        if (playerInv != null && playerInv.length() > 0 && !playerInv.equalsIgnoreCase("null")) {
            ItemStack[] items = ItemSerialization.fromString(playerInv, 36).getContents();
            player.getInventory().setContents(items);
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
        }
        player.getEquipment().setArmorContents(armorContents);
        player.getEquipment().setItemInOffHand(offHand);

        // calculate attributes
        API.calculateAllAttributes(player);

        String source = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_STORAGE, uuid);
        if (source != null && source.length() > 0 && !source.equalsIgnoreCase("null")) {
            Inventory inv = ItemSerialization.fromString(source);
            Storage storageTemp = new Storage(uuid, inv);
            BankMechanics.storage.put(uuid, storageTemp);
        } else {
            Storage storageTemp = new Storage(uuid);
            BankMechanics.storage.put(uuid, storageTemp);
        }
        TeleportAPI.addPlayerHearthstoneCD(uuid, 150);
        if (!DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, uuid).equals("")) {
            String[] locationString = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, uuid))
                    .split(",");
            player.teleport(new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationString[0]),
                    Double.parseDouble(locationString[1]), Double.parseDouble(locationString[2]),
                    Float.parseFloat(locationString[3]), Float.parseFloat(locationString[4])));
        } else {
            /**
             PLAYER IS NEW
             */
            player.teleport(new Location(Bukkit.getWorlds().get(0), 990, 32, -147, 72.9f, -3.3f));
            player.sendMessage(new String[]{
                    ChatColor.AQUA + "Welcome to DungeonRealms! Talk to the guides scattered around the island to get yourself acquainted, then meet the Ship Captain at the docks. Or type /skip"
            });

        }

        PlayerManager.checkInventory(uuid);

        // Essentials
        //Subscription.getInstance().handleJoin(player);
        Rank.getInstance().doGet(uuid);

        // Scoreboard Safety
        ScoreboardHandler.getInstance().matchMainScoreboard(player);

        player.setGameMode(GameMode.SURVIVAL);

        for (int j = 0; j < 20; j++) {
            player.sendMessage("");
        }

        player.setMaximumNoDamageTicks(0);

        player.sendMessage(new String[]{
                "               " + ChatColor.WHITE.toString() + ChatColor.BOLD + "Dungeon Realms Patch " + String.valueOf(DungeonRealms.version),
                ChatColor.GRAY + "               http://www.dungeonrealms.net/",
                ChatColor.YELLOW + "                You are on the " + ChatColor.BOLD + DungeonRealms.getInstance().shardid + ChatColor.YELLOW + " shard.",
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
        if (Rank.isGM(player)) {
            HealthHandler.getInstance().setPlayerHPLive(player, 10000);
            gp.setInvulnerable(true);
            player.sendMessage(new String[]{
                    "",
                    ChatColor.AQUA + ChatColor.BOLD.toString() + "                 GM INVINCIBILITY",
            });
        }

        player.sendMessage("");

        // Player Achievements
        // Don't use a switch because flowing through isn't possible due to different criteria.
        if (Rank.isDev(player)) {
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.DEVELOPER);
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.INFECTED);
        }

        if (Rank.isGM(player)) {
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
                if (!rank.equals("sub")) {
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SUBSCRIBER_PLUS);
                    if (!rank.equals("sub+")) {
                        Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SUBSCRIBER_PLUS_PLUS);
                    }
                }
            }
        }

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

        // Newbie Protection
        ProtectionHandler.getInstance().handleLogin(player);

        // Free E-Cash
        int freeEcash = (int) (Long.valueOf(DatabaseAPI.getInstance().getData(EnumData.FREE_ECASH, uuid).toString()) / 1000);
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        if (currentTime - freeEcash >= 86400) {
            int ecashReward = Utils.randInt(10, 15);
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.FREE_ECASH, System.currentTimeMillis(), true);
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, EnumData.ECASH, ecashReward, true);
            player.sendMessage(new String[]{
                    ChatColor.GOLD + "You have gained " + ChatColor.BOLD + ecashReward + "EC" + ChatColor.GOLD + " for logging into DungeonRealms today!",
                    ChatColor.GRAY + "Use /ecash to spend your EC, you can obtain more e-cash by logging in daily or by visiting " + ChatColor.GOLD + ChatColor.UNDERLINE + "http://www.dungeonrealms.net/shop"
            });
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
        }

        //
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
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.USERNAME, player.getName().toLowerCase(), false);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.CURRENTSERVER, DungeonRealms.getInstance().bungeeName, true);
        Utils.log.info("Fetched information for uuid: " + uuid.toString() + " on their login.");
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> AchievementManager.getInstance().handleLogin(player.getUniqueId()), 70L);
        player.addAttachment(DungeonRealms.getInstance()).setPermission("citizens.npc.talk", true);
        AttributeInstance instance = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        instance.setBaseValue(2.0D);
        DungeonRealms.getInstance().getLoggingOut().remove(player.getName());
        ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, gp.getPlayerAlignment().getAlignmentColor(), gp.getLevel());

        // Permissions
        if (!player.isOp() && !Rank.isDev(player)) {
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.plugins", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.version", false);
        }

        if (Rank.isGM(player)) {
            player.addAttachment(DungeonRealms.getInstance()).setPermission("essentials.*", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("citizens.*", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("worldedit.*", true);

            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.checks", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.bypass.denylogin", true);

            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.gamemode", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("minecraft.command.gamemode", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.teleport", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("minecraft.command.tp", true);
        }

        if (Rank.isPMOD(player)) {
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.notify", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.command.notify", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.command.info", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.command.inspect", true);
        }
    }

    static void backupDatabase() {
        if (Bukkit.getOnlinePlayers().size() == 0) return;
        AsyncUtils.pool.submit(() -> {
                    DungeonRealms.getInstance().getLogger().info("Beginning Mongo Database Backup");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) {
                            return;
                        }
                        UUID uuid = player.getUniqueId();
                        if (BankMechanics.storage.containsKey(uuid)) {
                            Inventory inv = BankMechanics.getInstance().getStorage(uuid).inv;
                            if (inv != null) {
                                String serializedInv = ItemSerialization.toString(inv);
                                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_STORAGE, serializedInv, false);
                            }
                            inv = BankMechanics.getInstance().getStorage(uuid).collection_bin;
                            if (inv != null) {
                                String serializedInv = ItemSerialization.toString(inv);
                                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, serializedInv, false);
                            }
                        }
                        Inventory inv = player.getInventory();
                        ArrayList<String> armor = new ArrayList<>();
                        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
                            if (itemStack == null || itemStack.getType() == Material.AIR) {
                                armor.add("null");
                            } else {
                                armor.add(ItemSerialization.itemStackToBase64(itemStack));
                            }
                        }
                        ItemStack offHand = player.getEquipment().getItemInOffHand();
                        if (offHand == null || offHand.getType() == Material.AIR) {
                            armor.add("");
                        } else {
                            armor.add(ItemSerialization.itemStackToBase64(offHand));
                        }
                        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, armor, false);
                        if (MountUtils.inventories.containsKey(uuid)) {
                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_MULE, ItemSerialization.toString(MountUtils.inventories.get(uuid)), false);
                        }

                        if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                            String locationAsString = "-367,86,390,0,0"; // Cyrennica
                            locationAsString = player.getLocation().getX() + "," + (player.getLocation().getY() + 0.5) + ","
                                    + player.getLocation().getZ() + "," + player.getLocation().getYaw() + ","
                                    + player.getLocation().getPitch();
                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, false);
                        } else {
                            //Dungeon or realm, should already have their last main world location saved.
                        }
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_FOOD, player.getFoodLevel(), false);
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HEALTH, HealthHandler.getInstance().getPlayerHPLive(player), false);
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT, KarmaHandler.getInstance().getPlayerRawAlignment(player), false);
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT_TIME, KarmaHandler.getInstance().getAlignmentTime(player), false);
                        String inventory = ItemSerialization.toString(inv);
                        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, inventory, false);
                        if (API.GAMEPLAYERS.size() > 0) {
                            GamePlayer gp = API.getGamePlayer(player);
                            if (gp != null) {
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.EXPERIENCE, gp.getPlayerEXP(), false);
                                gp.getPlayerStatistics().updatePlayerStatistics();
                                gp.getStats().updateDatabase(false);
                            }
                        }
                        DungeonRealms.getInstance().getLoggingOut().remove(player.getName());
                        Utils.log.info("Backed up information for uuid: " + uuid.toString());
                    }
                    DungeonRealms.getInstance().getLogger().info("Completed Mongo Database Backup");
                }

        );
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

    public static void setAttributeVal(AttributeType type, Integer[] val, Player p) {
        API.getGamePlayer(p).setAttributeVal(type, val);
    }

    public static Integer[] changeAttributeVal(AttributeType type, Integer[] difference, Player p) {
        return API.getGamePlayer(p).changeAttributeVal(type, difference);
    }

    public static int getStaticAttributeVal(AttributeType type, Player p) {
        return API.getGamePlayer(p).getStaticAttributeVal(type);
    }

    /**
     * Gets the value of a player's specified attribute. Must be a non-range
     * attribute.
     *
     * @param type
     * @param p
     * @return if a ranged attribute, throws an error message and returns -1.
     */
    public static Integer[] getRangedAttributeVal(AttributeType type, Player p) {
        return API.getGamePlayer(p).getRangedAttributeVal(type);
    }

    /**
     * Given an attribute, gets the total value of the attribute from the player's
     * armor and weapon if applicable. Even if the AttributeType passed is an Armor
     * or Weapon Attribute Type, the method will still try to calculate the total
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
    public static int[] calculateAttribute(AttributeType type, Player p) {
        if (type instanceof ArmorAttributeType) { // armor type
            ArmorAttributeType armorType = (ArmorAttributeType) type;
            ItemStack[] armorSet = p.getInventory().getArmorContents();
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = null;
            NBTTagCompound tag = null;

            for (ItemStack armor : armorSet) {
                if (!API.isArmor(armor)) {
                    continue;
                }
                nmsStack = CraftItemStack.asNMSCopy(armor);
                tag = nmsStack.getTag();

                if (tag.hasKey(type.getNBTName()) || tag.hasKey(type.getNBTName() + "Max")) {

                }
            }

            // check if a weapon can also have this attribute
            if (WeaponAttributeType.getByName(armorType.getName()) != null) {

            }
        } else if (type instanceof WeaponAttributeType) {
            WeaponAttributeType weaponType = (WeaponAttributeType) type;

            // check if armor can also have this attribute
            if (ArmorAttributeType.getByName(weaponType.getName()) != null) {

            }
        }

        return new int[]{0, 0};
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
        GamePlayer gp = API.getGamePlayer(p);
        assert gp != null;

        // calculate from armor and weapon, then update the gp attributes property
        calculateArmorAttributes(attributes, p.getInventory().getArmorContents(), true);
        calculateWeaponAttributes(attributes, p.getInventory().getItemInMainHand(), true);

        gp.setCurrentWeapon(p.getInventory().getItemInMainHand());

        // add stat bonuses from the stat menu
        changeAttributeVal(attributes, ArmorAttributeType.STRENGTH, gp.getStats().strPoints);
        changeAttributeVal(attributes, ArmorAttributeType.DEXTERITY, gp.getStats().dexPoints);
        changeAttributeVal(attributes, ArmorAttributeType.INTELLECT, gp.getStats().intPoints);
        changeAttributeVal(attributes, ArmorAttributeType.VITALITY, gp.getStats().vitPoints);

        // apply stat bonuses (str, dex, int, and vit)
        applyStatBonuses(attributes, gp);

        // set the player's attribute field
        gp.setAttributes(attributes);

        // so energy regen doesn't start before attributes have been loaded
        gp.setAttributesLoaded(true);

        HealthHandler.getInstance().handleLoginEvents(p);

        return attributes;
    }

    public static void calculateAllAttributes(LivingEntity ent, Map<String, Integer[]> attributes) {
        ItemStack[] armorSet = ent.getEquipment().getArmorContents().clone();

        // check if we have a skull
        if (armorSet[3].getType() == Material.SKULL_ITEM) {
            ItemTier tier = ItemTier.getByTier(ent.getMetadata("tier").get(0).asInt());
            // if we have a skull we need to generate a helmet so mob stats are calculated correctly
            armorSet[3] = new ItemGenerator().setTier(tier).setRarity(API.getItemRarity(ent.hasMetadata("elite"))).generateItem().getItem();
        }

        calculateArmorAttributes(attributes, armorSet, true);
        calculateWeaponAttributes(attributes, ent.getEquipment().getItemInMainHand(), true);
        applyStatBonuses(attributes);
    }

    /**
     * Calculates the difference in attributes when a player switches weapons. To save processing
     * power, only called when the player attempts to attack a mob with a new weapon.
     * <p>
     * NOTE: if attributes like str are added to weapons, this method will have to be called
     * whenever a player switches weapons.
     *
     * @param p
     * @param newWeapon
     */
    public static void handlePlayerWeaponSwitch(Player p, ItemStack newWeapon, ItemStack oldWeapon) {
        GamePlayer gp = API.getGamePlayer(p);
        assert gp != null;

        if (!API.isWeapon(newWeapon) && !API.isWeapon(oldWeapon)) return;

        if (newWeapon == null || newWeapon.getType() == Material.AIR) {
            List<String> oldModifiers = API.getModifiers(oldWeapon);
            net.minecraft.server.v1_9_R2.NBTTagCompound oldTag = CraftItemStack.asNMSCopy(oldWeapon).getTag();
            // iterate through to get decreases from stats not in the new armor
            oldModifiers.stream().forEach(modifier -> {
                WeaponAttributeType type = WeaponAttributeType.getByNBTName(modifier);
                // calculate new values
                Integer[] newTotalVal = type.isRange()
                        ? new Integer[]{gp.getRangedAttributeVal(type)[0] - oldTag.getInt(modifier + "Min"),
                        gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier + "Max")}
                        : new Integer[]{0, gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier)};
                gp.setAttributeVal(type, newTotalVal);
            });
        } else {
            List<String> newModifiers = API.getModifiers(newWeapon);
            net.minecraft.server.v1_9_R2.NBTTagCompound newTag = CraftItemStack.asNMSCopy(newWeapon).getTag();

            if (oldWeapon != null && oldWeapon.getType() != Material.AIR) {
                List<String> oldModifiers = API.getModifiers(oldWeapon);
                net.minecraft.server.v1_9_R2.NBTTagCompound oldTag = CraftItemStack.asNMSCopy(oldWeapon).getTag();

                // tbh, the milliseconds saved by writing this method and handleArmorDifferences probably wasn't
                // worth the effort...

                // get differences
                if (newModifiers != null) {
                    newModifiers.stream().forEach(modifier -> {
                        // get the attribute type to determine if we need a percentage or not and to get the
                        // correct display name
                        WeaponAttributeType type = WeaponAttributeType.getByNBTName(modifier);

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
                    oldModifiers.stream().forEach(modifier -> {
                        WeaponAttributeType type = WeaponAttributeType.getByNBTName(modifier);
                        Integer[] newTotalVal = type.isRange()
                                ? new Integer[]{gp.getRangedAttributeVal(type)[0] - oldTag.getInt(modifier + "Min"),
                                gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier + "Max")}
                                : new Integer[]{0, gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier)};
                        gp.setAttributeVal(type, newTotalVal);
                    });
                }
            } else {
                newModifiers.stream().forEach(modifier -> {
                    // get the attribute type to determine if we need a percentage or not and to get the
                    // correct display name
                    WeaponAttributeType type = WeaponAttributeType.getByNBTName(modifier);
                    // calculate new values
                    Integer[] newTotalVal = type.isRange()
                            ? new Integer[]{gp.getRangedAttributeVal(type)[0] + newTag.getInt(modifier + "Min"),
                            gp.getRangedAttributeVal(type)[1] + newTag.getInt(modifier + "Max")}
                            : new Integer[]{0, gp.getRangedAttributeVal(type)[1] + newTag.getInt(modifier)};
                    gp.setAttributeVal(type, newTotalVal);
                });
            }
        }

        gp.setCurrentWeapon(newWeapon);
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
        changeAttributeValPercentage(attributes, ArmorAttributeType.ARMOR, strength * 0.03f);
        changeAttributeValPercentage(attributes, ArmorAttributeType.BLOCK, strength * 0.017f);

        // DEXTERITY BONUSES
        float dexterity = (float) attributes.getOrDefault("dexterity", new Integer[]{0, 0})[1];
        changeAttributeValPercentage(attributes, ArmorAttributeType.DAMAGE, dexterity * 0.03f);
        changeAttributeValPercentage(attributes, ArmorAttributeType.DODGE, dexterity * 0.017f);
        changeAttributeValPercentage(attributes, WeaponAttributeType.ARMOR_PENETRATION, dexterity * 0.02f);

        // INTELLECT BONUSES
        float intellect = (float) attributes.getOrDefault("intellect", new Integer[]{0, 0})[1];
        changeAttributeValPercentage(attributes, ArmorAttributeType.ENERGY_REGEN, intellect * 0.015f);
        changeAttributeValPercentage(attributes, WeaponAttributeType.CRITICAL_HIT, intellect * 0.025f);

        // VITALITY BONUSES
        float vitality = (float) attributes.getOrDefault("vitality", new Integer[]{0, 0})[1];
        changeAttributeValPercentage(attributes, ArmorAttributeType.HEALTH_POINTS, vitality * 0.034f);
        changeAttributeVal(attributes, ArmorAttributeType.HEALTH_REGEN, vitality * 0.03f);
    }

    public static void applyStatBonuses(Map<String, Integer[]> attributes, GamePlayer gp) {
        Map<AttributeType, Float> attributeBonusesFromStats = gp.getAttributeBonusesFromStats();
        // STRENGTH BONUSES
        float strength = (float) attributes.getOrDefault("strength", new Integer[]{0, 0})[1];
        changeAttributeValPercentage(attributes, ArmorAttributeType.ARMOR, strength * 0.03f);
        attributeBonusesFromStats.put(ArmorAttributeType.ARMOR, strength * 0.03f);
        changeAttributeValPercentage(attributes, ArmorAttributeType.BLOCK, strength * 0.017f);
        attributeBonusesFromStats.put(ArmorAttributeType.BLOCK, strength * 0.017f);

        // DEXTERITY BONUSES
        float dexterity = (float) attributes.getOrDefault("dexterity", new Integer[]{0, 0})[1];
        changeAttributeValPercentage(attributes, ArmorAttributeType.DAMAGE, dexterity * 0.03f);
        attributeBonusesFromStats.put(ArmorAttributeType.DAMAGE, dexterity * 0.03f);
        changeAttributeValPercentage(attributes, ArmorAttributeType.DODGE, dexterity * 0.017f);
        attributeBonusesFromStats.put(ArmorAttributeType.DODGE, dexterity * 0.017f);
        changeAttributeValPercentage(attributes, WeaponAttributeType.ARMOR_PENETRATION, dexterity * 0.02f);
        attributeBonusesFromStats.put(WeaponAttributeType.ARMOR_PENETRATION, dexterity * 0.02f);

        // INTELLECT BONUSES
        float intellect = (float) attributes.getOrDefault("intellect", new Integer[]{0, 0})[1];
        changeAttributeValPercentage(attributes, ArmorAttributeType.ENERGY_REGEN, intellect * 0.015f);
        attributeBonusesFromStats.put(ArmorAttributeType.ENERGY_REGEN, intellect * 0.015f);
        changeAttributeValPercentage(attributes, WeaponAttributeType.CRITICAL_HIT, intellect * 0.025f);
        attributeBonusesFromStats.put(WeaponAttributeType.CRITICAL_HIT, intellect * 0.025f);

        // VITALITY BONUSES
        float vitality = (float) attributes.getOrDefault("vitality", new Integer[]{0, 0})[1];
        changeAttributeValPercentage(attributes, ArmorAttributeType.HEALTH_POINTS, vitality * 0.034f);
        attributeBonusesFromStats.put(ArmorAttributeType.ENERGY_REGEN, vitality * 0.034f);
        changeAttributeVal(attributes, ArmorAttributeType.HEALTH_REGEN, vitality * 0.03f);
        attributeBonusesFromStats.put(ArmorAttributeType.ENERGY_REGEN, vitality * 0.03f);
    }

    public static void recalculateStatBonuses(Map<String, Integer[]> attributes, Map<AttributeType, Float> attributeBonusesFromStats, GamePlayer gp) {
        attributeBonusesFromStats.entrySet().stream().forEach(entry -> {
            if (entry.getKey().equals(ArmorAttributeType.HEALTH_REGEN)) {
                changeAttributeVal(attributes, ArmorAttributeType.HEALTH_REGEN, -entry.getValue());
            }
            gp.changeAttributeValPercentage(entry.getKey(), -entry.getValue());
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
    private static Integer[] changeAttributeVal(Map<String, Integer[]> attributes, AttributeType type, float difference) {
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
    private static Integer[] changeAttributeValPercentage(Map<String, Integer[]> attributes, AttributeType type, float percentDifference) {
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
     * calculateAllAttributes method.
     *
     * @param attributes
     * @param armorSet
     * @param includeAbsentAttributes if true add all attributes not present
     *                                to map with values 0, 0
     */
    public static void calculateArmorAttributes(Map<String, Integer[]> attributes, ItemStack[] armorSet, boolean includeAbsentAttributes) {
        // populate the map with default values if necessary
        if (includeAbsentAttributes) {
            for (ArmorAttributeType type : ArmorAttributeType.values()) {
                attributes.putIfAbsent(type.getNBTName(), new Integer[]{0, 0});
            }
        }
        // iterate through armorset
        for (ItemStack armor : armorSet) {
            if (!API.isArmor(armor)) continue;

            List<String> modifiers = API.getModifiers(armor);
            NBTTagCompound tag = CraftItemStack.asNMSCopy(armor).getTag();
            assert tag != null;

            modifiers.stream().forEach(modifier -> {
                ArmorAttributeType type = ArmorAttributeType.getByNBTName(modifier);
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
     * calculateAllAttributes method.
     *
     * @param attributes
     * @param weapon
     * @param includeAbsentAttributes if true add all attributes not present
     *                                to map with values 0, 0
     */
    public static void calculateWeaponAttributes(Map<String, Integer[]> attributes, ItemStack weapon, boolean includeAbsentAttributes) {
        // populate the map with default values
        if (includeAbsentAttributes) {
            for (WeaponAttributeType type : WeaponAttributeType.values()) {
                attributes.putIfAbsent(type.getNBTName(), new Integer[]{0, 0});
            }
        }

        if (API.isWeapon(weapon)) {
            List<String> modifiers = API.getModifiers(weapon);
            NBTTagCompound tag = CraftItemStack.asNMSCopy(weapon).getTag();
            assert tag != null;

            modifiers.stream().forEach(modifier -> {
                WeaponAttributeType type = WeaponAttributeType.getByNBTName(modifier);
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
        List<String> modifiersList = new ArrayList<String>();
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
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        if (nms == null || nms.getTag() == null) return false;
        return nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("weapon");
    }

    public static boolean isArmor(ItemStack stack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        if (nms == null || nms.getTag() == null) return false;
        return nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("armor");
    }

    /**
     * @param is
     * @return
     */
    public static boolean isOrb(ItemStack is) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(is);
        if (nms == null || nms.getTag() == null) return false;
        return is.getType() == Material.MAGMA_CREAM && nms.getTag() != null && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("orb");
    }

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
        if (!tag.hasKey("soulbound")) return false;
        return tag.getInt("soulbound") == 1;
    }

    public static boolean isItemPermanentlyUntradeable(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        if (nms == null || nms.getTag() == null) return false;
        NBTTagCompound tag = nms.getTag();
        if (!tag.hasKey("untradeable")) return false;
        return tag.getInt("untradeable") == 1;
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
        MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, level);
        EntityStats.setMonsterRandomStats(entity, level, tier);
        String lvlName = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
        int hp = entity.getBukkitEntity().getMetadata("currentHP").get(0).asInt();
        String customName = entity.getBukkitEntity().getMetadata("customname").get(0).asString();
        entity.setCustomName(lvlName + ChatColor.RESET + customName);
        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
        world.addEntity(entity, SpawnReason.CUSTOM);
        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);

    }
}
