package net.dungeonrealms.game.mechanic.dungeons;

import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.ItemGemNote;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.CrashDetector;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.spawning.MobSpawner;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.lingala.zip4j.core.ZipFile;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Dungeon - The base of our new dungeon system.
 * <p>
 * Created April 28th, 2017.
 *
 * @author Kneesnap
 */
@Getter
public abstract class Dungeon {

    private DungeonType type;
    private int time;
    private int killCount;
    //    private Set<Entity> aliveMonsters = new ConcurrentSet<>();
    private Map<Entity, Location> trackedMonsters = new ConcurrentHashMap<>();
    private Set<MobSpawner> spawns = new ConcurrentSet<>();
    private List<BossType> spawnedBosses = new ArrayList<>();
    private World world;
    private boolean taunted;
    @Getter
    @Setter
    private int maxMobCount;
    private DungeonBoss boss;
    @Setter
    private boolean editMode;
    private List<Player> allowedPlayers = new ArrayList<>(); // Only contains the initial list of players who joined.

    public Dungeon(DungeonType dungeon, List<Player> players) {
        this.type = dungeon;
        this.allowedPlayers = players;
        createWorld(); // Init dungeon.
    }

    /**
     * Start the dungeon.
     */
    public void startDungeon() {
        for (Player player : this.allowedPlayers) {
            PlayerWrapper pw = PlayerWrapper.getWrapper(player);
            pw.setStoredLocation(TeleportLocation.CYRENNICA.getLocation());
            player.teleport(getWorld().getSpawnLocation());
            player.setFallDistance(0F);
            player.sendMessage(ChatColor.RED + getType().getBoss().getName() + "> " + ChatColor.WHITE + "How dare you enter my domain!");
        }
    }

    /**
     * Creates the world.
     */
    protected void setupWorld(String worldName) {
        // Create world.
        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.generateStructures(false);
        World w = Bukkit.getServer().createWorld(worldCreator);
        w.setStorm(false);
        w.setAutoSave(false);
        w.setKeepSpawnInMemory(false);
        w.setPVP(false);
        w.setGameRuleValue("randomTickSpeed", "0");
        Bukkit.getWorlds().add(w);
        this.world = w;

        // Load spawns.
        this.spawns = DungeonManager.getSpawns(getWorld(), getType());

        for (MobSpawner spawn : spawns) {
            maxMobCount += spawn.getSpawnAmount();
        }
//        startDungeon();
    }

    /**
     * Creates the world from a zip file.
     */
    private void createWorld() {
        final String worldName = "DUNGEON_" + System.currentTimeMillis() + "/";
        Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
            Bukkit.getLogger().info("[Dungeons] Creating world '" + worldName + "'.");

            try {
                new ZipFile(getType().getZipFile()).extractAll(worldName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Utils.removeFile(new File(worldName + "/uid.dat"));
            Utils.removeFile(new File(worldName + "/players"));

            Bukkit.getLogger().info("[Dungeons] Successfully created " + getType().getName() + ".");
            Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                setupWorld(worldName);
                startDungeon();
            });
        });

    }

    /**
     * Called when a player kills the boss.
     */
    public void completeDungeon() {
        announce(ChatColor.YELLOW + "You will be teleported out in 30 seconds...");
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> removePlayers(true), 1500L);
        giveShards();
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), this::giveDrops, 5L);

        // Add stats.
        getPlayers().forEach(p -> PlayerWrapper.getWrapper(p).getPlayerGameStats().addStat(getType().getStat()));
    }

    /**
     * Boss drops their items.
     */
    @SuppressWarnings("deprecation")
    private void giveDrops() {
        LivingEntity livingEntity = getBoss().getBukkit();
        Random random = ThreadLocalRandom.current();

        if (random.nextInt(100) < 80) { // 80% chance!
            List<ItemStack> possibleDrops = new ArrayList<>();

            // Get a list of posssible drops
            for (ItemStack is : livingEntity.getEquipment().getArmorContents())
                if (is != null && is.getType() != Material.AIR && is.getTypeId() != 144 && is.getTypeId() != 397)
                    possibleDrops.add(is);
            possibleDrops.add(livingEntity.getEquipment().getItemInMainHand());

            // Drop the item.
            ItemStack drop = possibleDrops.get(random.nextInt(possibleDrops.size()));

            // Remove any enchants.
            ItemMeta meta = drop.getItemMeta();
            meta.getEnchants().clear();
            drop.setItemMeta(meta);

            // Add soulbound.
            VanillaItem item = new VanillaItem(drop);
            item.setSoulbound(true);
            for (Player p : getPlayers())
                item.addSoulboundBypass(p, 60 * 5);

            // Drop the item.
            ItemStack reward = item.generateItem();
            livingEntity.getWorld().dropItem(livingEntity.getLocation(), reward);

            // Alert the players.
            List<String> hoveredChat = new ArrayList<>();
            hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : reward.getType().name()));
            if (meta.hasLore())
                hoveredChat.addAll(meta.getLore());

            final JSONMessage normal = new JSONMessage(ChatColor.DARK_PURPLE + "The boss has dropped: ", ChatColor.DARK_PURPLE);
            normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
            livingEntity.getWorld().getPlayers().forEach(normal::sendToPlayer);
        }

        int gemDrop = getType().getGems();
        int groupSize = (int) getPlayers().size();

        int perPlayerDrop = groupSize == 0 ? 1 : Math.round(gemDrop / groupSize);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            announce(ChatColor.DARK_PURPLE + "The boss has dropped " + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + gemDrop + ChatColor.DARK_PURPLE + " gems.");
            announce(ChatColor.DARK_PURPLE + "Each player receives " + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + perPlayerDrop + ChatColor.DARK_PURPLE + " gems!");

            if (groupSize == 0) {
                Bukkit.getLogger().warning("Tried to give rewards to a dungeon with nobody in it?");
                return;
            }

            String partyMembers = "";
            for (Player player : getPlayers().isEmpty() ? getAllPlayers() : getPlayers()) {

                partyMembers += player.getName() + ", ";

                ItemStack bankNote = new ItemGemNote(boss.getBossType().getName(), perPlayerDrop).generateItem();
                GameAPI.giveOrDropItem(player, bankNote);
                PlayerWrapper.getWrapper(player).addExperience(getType().getXP(), false, true);
            }

            final String adventurers = partyMembers.substring(0, partyMembers.length() - 2);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + ">> " + ChatColor.GOLD + (boss.getBossType().getPrefix().length() > 0 ? "The " + boss.getBossType().getPrefix() + " " : "") + ChatColor.UNDERLINE + boss.getBossType().getName() + ChatColor.RESET + ChatColor.GOLD + " has been slain by a group of adventurers!");
                Bukkit.broadcastMessage(ChatColor.GRAY + "Group: " + adventurers);
            }, 60L);
        }, 5L);

        giveMount();
    }

    /**
     * Attempts to give a mount to a player.
     */
    public void giveMount() {
        Random random = ThreadLocalRandom.current();
        if (getType().getMount() == null || random.nextInt(1000) > getType().getMount().getChance())
            return;

        ItemStack mount = getType().getMount().getMountData().createMountItem(getType().getMount());

        List<Player> players = getPlayers();
        Collections.shuffle(players);

        Player winner = players.get(0);
        PlayerWrapper pw = PlayerWrapper.getWrapper(winner);

        pw.getMountsUnlocked().add(getType().getMount());

        winner.playSound(winner.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
        winner.getInventory().addItem(mount);
        winner.getWorld().playSound(winner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, .3F);
        ParticleAPI.spawnParticle(Particle.VILLAGER_HAPPY, winner.getLocation(), 1, 1, 1, 50, .03F);
        String mountName = getType().getMount().getDisplayName();

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "** " + winner.getName() + ChatColor.GOLD + " has received a " +
                mountName + ChatColor.GOLD + " from the " + getType().getDisplayName() +
                ChatColor.GOLD + " dungeon as a rare drop! " + ChatColor.GOLD + ChatColor.BOLD + "**");
        TitleAPI.sendActionBar(winner, ChatColor.GREEN.toString() + ChatColor.BOLD + "You have received a " +
                mountName + ChatColor.GREEN + "!", 20 * 5);
    }

    /**
     * Teleports players out of the dungeon.
     *
     * @param success - Was the dungeon completed successfully?
     */
    public void removePlayers(boolean success) {
        announce(ChatColor.RED + getType().getBoss().getName() + "> " + ChatColor.RESET + "You have failed, Adventurers.");
        for (Player p : getAllPlayers()) {
            if (success)
                Achievements.giveAchievement(p, getType().getAchievement());
            p.teleport(TeleportLocation.CYRENNICA.getLocation());
            DungeonManager.removeDungeonItems(p);
        }
    }

    public void giveShards() {
        int totalShards = Utils.randInt(getType().getMinShards(), getType().getMaxShards());
        ShardTier tier = getType().getShardTier();

        getPlayers().forEach(p -> {
            p.sendMessage(tier.getColor() + "You have gained " + ChatColor.UNDERLINE + totalShards + " Portal Shards"
                    + tier.getColor() + " for completing this dungeon.");
            PlayerWrapper pw = PlayerWrapper.getWrapper(p);
            pw.setPortalShards(tier, pw.getPortalShards(tier) + totalShards);
        });
    }

    /**
     * Gets a list of all players in the dungeon, regardless of staff status.
     */
    public List<Player> getAllPlayers() {
        return getWorld() != null ? getWorld().getPlayers() : new ArrayList<>();
    }

    /**
     * Get a list of players in the dungeon.
     */
    public List<Player> getPlayers() {
        return getAllPlayers().stream().filter(p -> p.getGameMode() == GameMode.SURVIVAL).collect(Collectors.toList());
    }

    /**
     * Teleports an entity back to their spawner.
     */
    public void returnToSpawner(Entity e) {
        Location l = getTrackedMonsters().get(e);
        if (l != null)
            e.teleport(l);
    }

    /**
     * Handle updating the entity.
     * Removes it if it's dead, etc.
     */
    public void updateMob(Entity e) {
        if (e.isValid())
            return;

        getTrackedMonsters().remove(e);

        if (!e.hasMetadata("invalid")) {
            increaseKillCount();
        } else {
            maxMobCount--;
        }
    }

    /**
     * Increase the kill counter.
     */
    public void increaseKillCount() {
        this.killCount++;
    }

    /**
     * Increase the timer
     */
    public void increaseTimer() {
        this.time++;
    }

    /**
     * Gets the amount of mobs left needed to kill.
     */
    public int getKillsLeft() {
        return Math.max(0, (int) (getMaxMobCount() * 0.8D) - getKillCount());
    }

    /**
     * Can the boss spawn now?
     */
    public boolean canBossSpawn() {
        return getKillsLeft() == 0;
    }

    /**
     * Announce a message to all players in the dungeon.
     */
    public void announce(String s) {
        getAllPlayers().forEach(p -> p.sendMessage(s));
    }

    /**
     * Taunts, if possible.
     */
    public boolean attemptTaunt() {
        if (isTaunted() || !canBossSpawn())
            return false;
        announce(ChatColor.RED + getType().getBoss().getName() + "> " + ChatColor.WHITE + "Do you really wish to fight me?");
        this.taunted = true;
        return true;
    }

    /**
     * Removes this dungeon, and teleports players out.
     * If any players are still here, it treats it as if they lost the dungeon.
     */
    public void remove() {
        if (CrashDetector.crashDetected)
            return;

        Bukkit.getLogger().info("[Dungeons] Terminating " + getType().getName() + ", instance '" + getWorld().getName() + "'.");
        // Move players out.
        removePlayers(false);

        boolean save = isEditMode();

        if (save) {
            // Remove entities.
            getWorld().getEntities().stream().filter(e -> !(e instanceof Player)).forEach(Entity::remove);
            // Unload chunks.
            Arrays.stream(getWorld().getLoadedChunks()).forEach(c -> c.unload(true));

        }

        Bukkit.unloadWorld(getWorld(), save);

        if (save) {
            try {
                Bukkit.getLogger().info("Saving " + getType().getName() + " from " + getWorld().getName());
                GameAPI.createZipFile(getWorld().getName() + "/", getType().getZipFile().getPath());
                GameAPI.sendStaffMessage(PlayerRank.GM, ChatColor.GREEN + "Saved modified '" + getType().getName() + "' dungeon on {SERVER}" + ChatColor.GREEN + ".");
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().warning("Failed to save editted dungeon " + getType().getName() + ".");
            }
        }

        Bukkit.getWorlds().remove(getWorld());

        Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
            Utils.removeFile(new File(getWorld().getName()));
            Utils.removeFile(new File("plugins/WorldGuard/worlds/" + getWorld().getName()));
        });

        Bukkit.getLogger().info("[Dungeons] Successfully terminated.");
        DungeonManager.getDungeons().remove(this);
    }

    public boolean hasSpawned(BossType type) {
        return getSpawnedBosses().contains(type);
    }

    /**
     * Spawns a dungeon boss.
     * Should only be called on non-special mobs.
     * Spawns at the mobs defined location.
     *
     * @return
     */
    public DungeonBoss spawnBoss(BossType type) {
        assert !type.isSpecial();
        return spawnBoss(type, type.getLocation(getWorld()));
    }

    /**
     * Spawns the specified dungeon boss type at the given location.
     */
    public DungeonBoss spawnBoss(BossType type, Location loc) {
        // We've already spawned this boss.
        if (hasSpawned(type))
            return null;

        DungeonBoss boss = null;

        try {
            net.minecraft.server.v1_9_R2.World w = ((CraftWorld) getWorld()).getHandle();
            net.minecraft.server.v1_9_R2.EntityInsentient e = type.getMonster().getClazz().getDeclaredConstructor(net.minecraft.server.v1_9_R2.World.class).newInstance(w);
            e.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
            w.addEntity(e, SpawnReason.CUSTOM);
            Entity ent = e.getBukkitEntity();
            ent.teleport(loc);
            boss = (DungeonBoss) e;
            boss.createEntity(100);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("Failed to construct " + type.getName() + ".");
            return null;
        }

        getSpawnedBosses().add(type);
        if (type.getSound() != null)
            getWorld().playSound(loc, type.getSound(), type.getVolume(), type.getPitch());

        if (type.isFinalBoss())
            this.boss = boss;
        return boss;
    }
}
