package net.dungeonrealms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.banks.Storage;
import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.mounts.EnumMounts;
import net.dungeonrealms.entities.types.pets.EnumPets;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.entities.utils.MountUtils;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.handlers.EnergyHandler;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.handlers.KarmaHandler;
import net.dungeonrealms.handlers.ScoreboardHandler;
import net.dungeonrealms.items.Item;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.Armor;
import net.dungeonrealms.items.armor.Armor.ArmorModifier;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mastery.*;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.mechanics.PlayerManager;
import net.dungeonrealms.miscellaneous.ItemBuilder;
import net.dungeonrealms.miscellaneous.RandomHelper;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.mongo.achievements.AchievementManager;
import net.dungeonrealms.notice.Notice;
import net.dungeonrealms.rank.Rank;
import net.dungeonrealms.teleportation.TeleportAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.activation.UnknownObjectException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created by Nick on 9/17/2015.
 */
public class API {

    public static CopyOnWriteArrayList<GamePlayer> GAMEPLAYERS = new CopyOnWriteArrayList<>();

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

    /**
     * @param player
     * @param kill
     * @return Integer
     */
    public static int getMonsterExp(Player player, org.bukkit.entity.Entity kill) {
        int level = API.getGamePlayer(player).getStats().getLevel();
        int mob_level = kill.getMetadata("level").get(0).asInt();
        int xp = 0;
        if (mob_level > level + 10) {  // limit mob xp calculation to 10 levels above player level
            xp = calculateXP(player, kill, level + 10);
        } else {
            xp = calculateXP(player, kill, mob_level);
        }
        return xp;
    }

    public static ItemStack[] getTierArmor(int tier) {
		int chance = RandomHelper.getRandomNumberBetween(1, 1000);
		if(chance == 1){
			return new ArmorGenerator().nextArmor(tier, ArmorModifier.LEGENDARY);
		}
		if(chance <= 10)
			return new ArmorGenerator().nextArmor(tier, ArmorModifier.RARE);
		else if(chance > 10 && chance <= 50)
			return new ArmorGenerator().nextArmor(tier, ArmorModifier.UNCOMMON);
		else
        return new ArmorGenerator().nextArmor(tier, ArmorModifier.COMMON);
    }

    public static ArmorModifier getArmorModifier(){
		int chance = RandomHelper.getRandomNumberBetween(1, 500);
		if (chance == 1) {
			return ArmorModifier.LEGENDARY;
		} else if (chance <= 10) {
			return ArmorModifier.UNIQUE;
		} else if (chance > 10 && chance <= 50) {
			return ArmorModifier.RARE;
		} else if (chance > 50 && chance <= 200) {
            return ArmorModifier.UNCOMMON;
        } else {
            return ArmorModifier.COMMON;
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
        int xp = (int) (((pLevel * 5) + 45) * (1 + 0.05 * (pLevel + (mob_level - pLevel)))); // patch 1.9 exp formula
        return xp;
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
     * Gets players UUID from Name. ASYNC.
     *
     * @param name
     * @return
     */
    public static UUID getUUIDFromName(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name + "?at=" + (System.currentTimeMillis() / 1000l));

            Reader in = new InputStreamReader(url.openStream());
            Object json = JSONValue.parse(in);

            JSONObject object = (JSONObject) json;

            String rawInput = (String) object.get("id");
            StringBuilder input = new StringBuilder(rawInput);

            input.insert(8, "-");
            input.insert(13, "-");
            input.insert(18, "-");
            input.insert(23, "-");

            return UUID.fromString(input.toString());
        } catch (Exception ex) {
            Utils.log.warning("[API] [getUUIDFromName] an invalid name has been inputted!");
        }
        return null;
    }

    /**
     * Gets players name from UUID. ASYNC.
     *
     * @param uuid
     * @return
     */
    public static String getNameFromUUID(UUID uuid) {

        NameFetcher fetcher = new NameFetcher(Collections.singletonList(uuid));

        try {
            return fetcher.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "BOB";
    }

    /**
     * Gets the WorldGuard plugin.
     *
     * @return
     * @since 1.0
     */
    private static WorldGuardPlugin getWorldGuard() {
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
        if (!location.getWorld().equals(Bukkit.getWorlds().get(0))) {
            return false;
        }
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
        return location.getWorld().getPlayers().stream()
                .filter(player -> location.distance(player.getLocation()) <= radius).collect(Collectors.toList());
    }

    /**
     * Safely logs out the player, updates their mongo inventories etc.
     *
     * @param uuid
     * @since 1.0
     */
    public static void handleLogout(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_PLAYING, false, false);
        if (BankMechanics.storage.containsKey(uuid)) {
            Inventory inv = BankMechanics.storage.get(uuid).inv;
            if (inv != null) {
                String serializedInv = ItemSerialization.toString(inv);
                BankMechanics.storage.remove(uuid);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_STORAGE, serializedInv,
                        false);
            }
        }
        PlayerInventory inv = player.getInventory();

        ArrayList<String> armor = new ArrayList<String>();
        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                armor.add("null");
            } else {
                armor.add(ItemSerialization.itemStackToBase64(itemStack));
            }
        }
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, armor, false);

        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, ItemSerialization.toString(inv),
                false);
        if(MountUtils.inventories.containsKey(uuid)){
        	DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_MULE, ItemSerialization.toString(MountUtils.inventories.get(uuid)), false);
        	MountUtils.inventories.remove(uuid);
        }
        String locationAsString = "-367,86,390,0,0"; // Cyrennica
        if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
            locationAsString = player.getLocation().getX() + "," + (player.getLocation().getY() + 0.5) + ","
                    + player.getLocation().getZ() + "," + player.getLocation().getYaw() + ","
                    + player.getLocation().getPitch();
        }
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, false);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.LAST_LOGOUT, System.currentTimeMillis() / 1000l, false);
        EnergyHandler.getInstance().handleLogoutEvents(player);
        HealthHandler.getInstance().handleLogoutEvents(player);
        KarmaHandler.getInstance().handleLogoutEvents(player);
        ScoreboardHandler.getInstance().removePlayerScoreboard(player);
        if (EntityAPI.hasPetOut(uuid)) {
            net.minecraft.server.v1_8_R3.Entity pet = Entities.PLAYER_PETS.get(uuid);
            pet.dead = true;
            EntityAPI.removePlayerPetList(uuid);
        }
        if (EntityAPI.hasMountOut(uuid)) {
            net.minecraft.server.v1_8_R3.Entity mount = Entities.PLAYER_MOUNTS.get(uuid);
            mount.dead = true;
            EntityAPI.removePlayerMountList(uuid);
        }
        if (GAMEPLAYERS.size() > 0)
            GAMEPLAYERS.stream().filter(gPlayer -> gPlayer.getPlayer().getName().equalsIgnoreCase(player.getName())).forEach(gPlayer -> {
                gPlayer.getStats().updateDatabase();
                GAMEPLAYERS.remove(gPlayer);
            });
    }

    /**
     * Safely logs out all players when the server restarts
     *
     * @since 1.0
     */
    public static void logoutAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handleLogout(player.getUniqueId());
            player.kickPlayer("Server Restarting!");
        }
    }

    /**
     * Safely logs in the player, giving them their items, their storage and
     * their cooldowns
     *
     * @since 1.0
     */
    public static void handleLogin(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        if (!DatabaseAPI.getInstance().PLAYERS.containsKey(uuid)) {
            player.kickPlayer(ChatColor.RED + "Unable to grab your data.. rejoin!");
        } else {
            if (player != null) {
                player.sendMessage(ChatColor.GREEN + "Successfully received your data.. loading now...");
            }
        }

        GamePlayer gp = new GamePlayer(player);
        API.GAMEPLAYERS.add(gp);

       List<String> playerArmor = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ARMOR, player.getUniqueId());
        int i = -1;
        ItemStack[] armorContents = new ItemStack[4];
        for (String armor : playerArmor) {
            i++;
            if (armor.equals("null") || armor.equals("")) {
                armorContents[i] = new ItemStack(Material.AIR);
            } else {
                armorContents[i] = ItemSerialization.itemStackFromBase64(armor);
            }
        }
        player.getInventory().setArmorContents(armorContents);
        /*for(int i = 0; i  < playerArmor.size(); i++){
        	String armorStr = playerArmor.get(i);
        	if(armorStr.equalsIgnoreCase("null") || armorStr == null)
        		continue;
        	player.getInventory().getArmorContents()[i] = ItemSerialization.itemStackFromBase64(armorStr);
        }*/

        AchievementManager.getInstance().handleLogin(player);
        String playerInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY, uuid);
        if (playerInv != null && playerInv.length() > 0 && !playerInv.equalsIgnoreCase("null")) {
            ItemStack[] items = ItemSerialization.fromString(playerInv).getContents();
            player.getInventory().setContents(items);
        }
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

            player.getInventory().addItem(new ItemBuilder().setItem(new ItemGenerator().getDefinedStack(Item.ItemType.AXE, Item.ItemTier.TIER_1, Item.ItemModifier.UNCOMMON))
                    .setNBTString("subtype", "starter").build());

            player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.HELMET, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                    .setNBTString("subtype", "starter").build());
            player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.CHESTPLATE, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                    .setNBTString("subtype", "starter").build());
            player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.LEGGINGS, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                    .setNBTString("subtype", "starter").build());
            player.getInventory().addItem(new ItemBuilder().setItem(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.BOOTS, Armor.ArmorTier.TIER_1, Armor.ArmorModifier.COMMON))
                    .setNBTString("subtype", "starter").build());

            player.getInventory().addItem(new ItemBuilder().setItem(new ItemStack(Material.BREAD, 10)).setNBTString("subtype", "starter").build());

            player.teleport(new Location(Bukkit.getWorlds().get(0), -367 + new Random().nextInt(4), 86, 390 + new Random().nextInt(4), 0f, 0f));

        }
        PlayerManager.checkInventory(uuid);
        EnergyHandler.getInstance().handleLoginEvents(player);
        HealthHandler.getInstance().handleLoginEvents(player);
        KarmaHandler.getInstance().handleLoginEvents(player);
        // Essentials
        //Subscription.getInstance().handleJoin(player);
        Rank.getInstance().doGet(uuid);
        // Guilds
        Guild.getInstance().doGet(uuid);
        Guild.getInstance().doLogin(player);

        // Notices
        Notice.getInstance().doLogin(player);

        // Scoreboard Safety
        ScoreboardHandler.getInstance().matchMainScoreboard(player);

        player.setGameMode(GameMode.SURVIVAL);

        player.sendMessage(ChatColor.GREEN + "Character loaded, have fun. ;-)");

        player.setMaximumNoDamageTicks(0);
    }

    /**
     * Returns if a player is online. (LOCAL SERVER)
     *
     * @param uuid
     * @return boolean
     * @since 1.0
     */
    public static boolean isOnline(UUID uuid) {
        try {
            return Bukkit.getServer().getPlayer(uuid).isOnline();
        } catch (Exception e) {
            return false;
        }
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
        return entity instanceof Player && !(entity.hasMetadata("NPC") || entity.hasMetadata("npc"));
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
        for (GamePlayer gPlayer : GAMEPLAYERS) {
            if (gPlayer.getPlayer().getName().equalsIgnoreCase(p.getName()))
                return gPlayer;
        }
        return null;
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
    /**
     * Spawn our Entity at Location
     *
     * Use SpawningMechanics.getMob for Entity
     * lvlRange = "high" or "low"
     * @param location
     * @param entity
     * @param tier
     * @param lvlRange
     */
    public void spawnMonsterAt(Location location, net.minecraft.server.v1_8_R3.Entity entity, int tier, String lvlRange){
    	net.minecraft.server.v1_8_R3.World world = ((CraftWorld)location.getWorld()).getHandle();
		int level = Utils.getRandomFromTier(tier, "low");
		MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, level);
		EntityStats.setMonsterRandomStats(entity, level, tier);
        String lvlName =  ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
        int hp = entity.getBukkitEntity().getMetadata("currentHP").get(0).asInt();
		String customName = entity.getBukkitEntity().getMetadata("customname").get(0).asString();
		entity.setCustomName(lvlName + ChatColor.RESET + customName + ChatColor.RED.toString() + "❤ " + ChatColor.RESET + hp);
		entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
		world.addEntity(entity, SpawnReason.CUSTOM);
		entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);

    }

    public static File getRemoteDataFolder(){
    	String filePath = DungeonRealms.getInstance().getDataFolder().getAbsolutePath();
    	File file = DungeonRealms.getInstance().getDataFolder();
    	if(filePath.contains("/home/servers")){
    		if(filePath.contains("d1")){
    			filePath = "d1";
    		}else if(filePath.contains("d2")){
    			filePath = "d2";
    		}else if(filePath.contains("d3")){
    			filePath = "d3";
    		}else if(filePath.contains("d4")){
    			filePath = "d4";
    		}else if(filePath.contains("d5")){
    			filePath = "d5";
    		}
    		String webRoot = "/home/servers/" + filePath+ "/";
    		file = new File(webRoot, DungeonRealms.getInstance().getDataFolder() +"");
    	}
    	return file;
    }

}
