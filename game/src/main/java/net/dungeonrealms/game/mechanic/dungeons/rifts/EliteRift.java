package net.dungeonrealms.game.mechanic.dungeons.rifts;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemArmorShield;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.dungeons.*;
import net.dungeonrealms.game.mechanic.rifts.RiftPortal;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.world.entity.type.monster.boss.RiftEliteBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.lingala.zip4j.core.ZipFile;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 6/7/2017.
 */
public class EliteRift extends Dungeon {


    @Getter
    private ConcurrentHashMap<Location, Tuple<MaterialData, Long>> blockTypes = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentHashMap<Player, Long> lastMovements = new ConcurrentHashMap<>();
    @Getter
    private CopyOnWriteArrayList<Entity> minions = new CopyOnWriteArrayList<>();
    @Getter
    private MapData map;
    @Setter
    private int ourTier = 1;
    private int taskID = -1;
    private long lastMinionSpawn;
    @Getter
    private long startTime;
    private int totalSpawnedMinions = 0;

    @Getter
    private boolean completed = false;
    //@Getter
    //private Block currentBlackHole;
    @Getter
    private List<Block> blackHoles = new CopyOnWriteArrayList<>();

    public EliteRift(List<Player> players) {
        super(DungeonType.ELITE_RIFT, players);
        map = MapData.values()[ThreadLocalRandom.current().nextInt(MapData.values().length)];
    }

    @Override
    protected void createWorld() {
        final String worldName = "DUNGEON_" + System.currentTimeMillis() + "/";
        Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
            Bukkit.getLogger().info("[Dungeons] Creating world '" + worldName + "'.");

            try {
                new ZipFile(getZipFile(map.getWorldName())).extractAll(worldName);
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


    public File getZipFile(String name) {
        return new File(GameAPI.getDataFolder() + "/dungeons/" + name + ".zip");
    }

    public void setTier(int tier) {
        if (tier < 1 || tier > 5) throw new IllegalArgumentException("Tier is out of range!");
    }

    private boolean canSpawnAMinion() {
        return !isFinished() && System.currentTimeMillis() - lastMinionSpawn > 5000 && getNumberOfSpawnedMinions() < 15 && totalSpawnedMinions < 50;
    }

    public int getNumberOfSpawnedMinions() {
        int toReturn = 0;
        for (int k = 0; k < minions.size(); k++) {
            Entity ent = minions.get(k);
            if (ent == null || ent.isDead()) {
                minions.remove(k);
                continue;
            }
            toReturn++;
        }

        return toReturn;
    }


    public void spawnAMinion() {
        if (!canSpawnAMinion()) return;

        Location loc = getRandomMinionLocation();
        EnumMonster monsterType = EnumMonster.RiftMinion;
        LivingEntity le = (LivingEntity) EntityAPI.spawnCustomMonster(loc, monsterType, Utils.randInt(25, 50), ourTier, null, "Rift Minion");
        le.setRemoveWhenFarAway(false);
        le.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(60);
        getWorld().playSound(loc, Sound.ENTITY_ENDERMEN_TELEPORT, 1f, 1f);
        ParticleAPI.spawnParticle(Particle.PORTAL, loc.clone().add(.5, 1, .5), .24F, 1F, .24F, 30, .1F);
        minions.add(le);
        lastMinionSpawn = System.currentTimeMillis();
        totalSpawnedMinions++;
    }

    private Location getRandomMinionLocation() {
        Location center = getMap().getCenterLocation().clone();
        center.add(0, 4, 0);
        center.setWorld(getWorld());
        center.add(ThreadLocalRandom.current().nextInt(getMap().getMapRadius()), 0, ThreadLocalRandom.current().nextInt(getMap().getMapRadius()));
        return center;
    }

    public void tick() {
        if (getWorld() == null) return;

        RiftEliteBoss boss = (RiftEliteBoss) this.boss;
        spawnAMinion();

        if (bossInLava()) handleBossInLava();
        else if (!boss.isInAir() && ThreadLocalRandom.current().nextInt(100) == 4) boss.jump(4);

        repairBlocksNaturally();

        if (!isFinished() && System.currentTimeMillis() - startTime > 10000) {
            Player target = boss.getTarget();
            if (target != null) {
                if (target.getWorld() == getWorld()) {
                    Long lastMovement = lastMovements.get(target);
                    if (lastMovement == null || (System.currentTimeMillis() - lastMovement > 3000)) {
                        handlePlayerNotMoving(target);
                    }
                }
            }
        }

    }


    public Block getNearestBlackHole(Player player) {
        double lowestDistance = Double.MAX_VALUE;
        Block toReturn = null;
        for (Block block : getBlackHoles()) {
            double distance = block.getLocation().distanceSquared(player.getLocation());
            if (distance > (10 * 10)) continue;
            if (distance < lowestDistance) {
                toReturn = block;
                lowestDistance = distance;
            }
        }

        return toReturn;
    }

    public void clearBlackHoles() {
        for (Block block : blackHoles) {
            if (block == null || block.getType() != Material.END_GATEWAY) continue;
            block.setType(Material.AIR);
        }

        blackHoles.clear();

    }

    private void pullPlayersToBlackHole() {
        RiftEliteBoss boss = (RiftEliteBoss) this.boss;
        if (!boss.getStage().equals(RiftEliteBoss.BossStage.BLACK_HOLE) || getBlackHoles().isEmpty()) return;
        for (Player player : getWorld().getPlayers()) {
            Block blackHole = getNearestBlackHole(player);
            if (blackHole == null) continue;
            double distance = blackHole.getLocation().distanceSquared(player.getLocation());
            Vector current = player.getVelocity();
            Vector vel = current.add(blackHole.getLocation().toVector().subtract(player.getLocation().toVector()));
            int maxDistance = 100;

            player.setVelocity(vel.multiply(0.0003 * (maxDistance - distance)));
            ((CraftPlayer) player).getHandle().velocityChanged = true; //keep running.
        }
    }

    private boolean bossInLava() {
        if (boss == null || boss.getBukkit() == null || boss.getBukkit().isDead()) return false;
        Block standing = boss.getBukkit().getLocation().getBlock();
        Block under = boss.getBukkit().getLocation().clone().subtract(0, 1, 0).getBlock();
        return standing.getType().equals(Material.LAVA) || standing.getType().equals(Material.STATIONARY_LAVA) || under.getType().equals(Material.LAVA) || under.getType().equals(Material.STATIONARY_LAVA);
    }

    private void handleBossInLava() {
        RiftEliteBoss boss = (RiftEliteBoss) getBoss();
        boss.jump(4);
        Block bl = boss.getBukkit().getLocation().subtract(0, 1, 0).getBlock();
        repairBlocks(bl.getLocation(), 3);
    }

    public void repairBlocksNaturally() {
        repairBlocksNaturally(false);
    }

    public void repairBlocksNaturally(boolean overrideTime) {
        for (Map.Entry<Location, Tuple<MaterialData, Long>> entry : blockTypes.entrySet()) {
            Location blockLoc = entry.getKey();
            Tuple<MaterialData, Long> values = entry.getValue();
            MaterialData data = values.a();
            Long time = values.b();
            if (overrideTime || System.currentTimeMillis() - time > 20000) {
                Block b = getWorld().getBlockAt(blockLoc);
                b.setType(data.getItemType());
                b.setData(data.getData());
                blockTypes.remove(blockLoc);
            }
        }
    }

    public void regenerateSpawnLocation() {
        /*Location spawn = map.getSpawnLocation().clone();
        //Location newBlockLoc = new Location(getWorld(), x, center.getBlock().getY(), z);
        for (Map.Entry<Location, Tuple<MaterialData, Long>> entry : blockTypes.entrySet()) {
            Location mapLoc = entry.getKey();
            if (mapLoc.getX() == spawn.getX() && mapLoc.getZ() == spawn.getZ()) {
                Tuple<MaterialData, Long> values = entry.getValue();
                MaterialData data = values.a();
                Block b = getWorld().getBlockAt(mapLoc);
                b.setType(data.getItemType());
                b.setData(data.getData());
                blockTypes.remove(mapLoc);
            }
        }*/
        repairBlocks(map.getSpawnLocation().clone(), 2);
    }

    private void repairBlocks(Location center, int radius) {
        if (center.getWorld() == null && getWorld() != null) {
            center.setWorld(getWorld());
        }
        if (center.getWorld() == null) return;
        Block block = center.getBlock();
        for (int x = block.getX() - radius; x < block.getX() + radius; x++) {
            for (int z = block.getZ() - radius; z < block.getZ() + radius; z++) {
                Location newBlockLoc = new Location(getWorld(), x, block.getY(), z);
                for (Map.Entry<Location, Tuple<MaterialData, Long>> entry : blockTypes.entrySet()) {
                    Location mapLoc = entry.getKey();
                    if (mapLoc.getX() == newBlockLoc.getX() && mapLoc.getZ() == newBlockLoc.getZ()) {
                        Tuple<MaterialData, Long> values = entry.getValue();
                        MaterialData data = values.a();
                        Block b = getWorld().getBlockAt(mapLoc);
                        b.setType(data.getItemType());
                        b.setData(data.getData());
                        blockTypes.remove(mapLoc);
                    }
                }
            }
        }
    }

    private void handlePlayerNotMoving(Player player) {
        RiftEliteBoss boss = (RiftEliteBoss) this.boss;
        if (boss.getStage().equals(RiftEliteBoss.BossStage.LAVA_TRAIL) && (System.currentTimeMillis() - boss.getLastStageSwitch() > 5000)) {
            Block bl = player.getLocation().subtract(0, 1, 0).getBlock();
            for (int x = bl.getX() - 2; x < bl.getX() + 2; x++) {
                for (int z = bl.getZ() - 2; z < bl.getZ() + 2; z++) {
                    Location newBlockLoc = new Location(getWorld(), x, bl.getY(), z);
                    Block newBlock = newBlockLoc.getBlock();
                    if (newBlock == null || newBlock.getType().equals(Material.STATIONARY_LAVA) || newBlock.getType().equals(Material.AIR) || newBlock.getType().equals(Material.LAVA))
                        continue;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        MaterialData data = new MaterialData(newBlock.getType(), newBlock.getData());
                        this.getBlockTypes().put(newBlock.getLocation(), new Tuple<>(data, System.currentTimeMillis()));
                        newBlock.setType(Material.STATIONARY_LAVA);
                    }, 5);
                }
            }
        }
    }

    @Override
    public void addPlayer(Player player) {
        if (!this.allowedPlayers.contains(player))
            this.allowedPlayers.add(player);


        this.lastMovements.put(player, System.currentTimeMillis());
        Location location = map.getSpawnLocation();
        location.setWorld(getWorld());
        PlayerWrapper pw = PlayerWrapper.getWrapper(player);
        pw.setStoredLocation(player.getLocation());
        GameAPI.teleport(player, location);
        player.setVelocity(new Vector(0, 0, -(.5F + Math.random())));
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 4, 1.4F);
        player.setFallDistance(0F);

        if (getWorld() != null)
            regenerateSpawnLocation();
    }

    @Override
    public void startDungeon() {
        Location spawnLoc = map.getSpawnLocation();
        for (Player player : this.allowedPlayers) {
            PlayerWrapper pw = PlayerWrapper.getWrapper(player);
            pw.setStoredLocation(TeleportLocation.CYRENNICA.getLocation());
            GameAPI.teleport(player, new Location(getWorld(), spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ()));
            player.setFallDistance(0F);
            player.sendMessage(ChatColor.RED + getType().getBoss().getName() + "> " + ChatColor.WHITE + "How dare you enter my domain!");
        }
    }


    @Override
    public void giveShards() {
        //No shards for these rifts.
    }

    @Override
    public boolean canBossSpawn(boolean other) {
        return true;
    }

    @Override
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
        //this.spawns = DungeonManager.getSpawns(getWorld(), getType());

        //for (MobSpawner spawn : spawns)
        //  maxMobCount += spawn.getSpawnAmount();
    }

    @Override
    public ItemWeapon getGeneralMobWeapon() {
        return (ItemWeapon) new ItemWeapon().setTier(ourTier).setRarity(Item.ItemRarity.UNIQUE);
    }

    @Override
    public void giveMount() {
        //No mount
    }

    @Override
    public ItemArmor getGeneralMobArmorSet() {
        return (ItemArmor) new ItemArmor().setTier(ourTier).setMaxRarity(Item.ItemRarity.UNIQUE, 3);
    }

    @Override
    public DungeonBoss spawnBoss(BossType type) {
        Location loc = map.getBossLocation();
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (isFinished()) {
                    cancel();
                    return;
                }

                if (ticks++ % 5 == 0) tick();
                pullPlayersToBlackHole();

            }
        }.runTaskTimer(DungeonRealms.getInstance(), 1, 1);
        return spawnBoss(type, new Location(getWorld(), loc.getX(), loc.getY(), loc.getZ()));
    }

    @Override
    public DungeonBoss spawnBoss(BossType type, Location loc) {
        // We've already spawned this boss.
        if (hasSpawned(type))
            return null;

        RiftEliteBoss boss = null;

        try {
            net.minecraft.server.v1_9_R2.World w = ((CraftWorld) getWorld()).getHandle();
            net.minecraft.server.v1_9_R2.EntityInsentient e = type.getMonster().getClazz().getDeclaredConstructor(net.minecraft.server.v1_9_R2.World.class).newInstance(w);
            e.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
            w.addEntity(e, CreatureSpawnEvent.SpawnReason.CUSTOM);
            Entity ent = e.getBukkitEntity();
            ent.teleport(loc);
            boss = (RiftEliteBoss) e;
            boss.setOurTier(ourTier);
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

        this.startTime = System.currentTimeMillis();
        for (Player player : getAllowedPlayers())
            player.sendMessage(ChatColor.RED + getType().getBoss().getName() + "> " + ChatColor.WHITE + "You traveled all this way just to die adventurer?!");

        return boss;
    }

    @Override
    public void removePlayers(boolean success) {
        if (!success)
            announce(ChatColor.RED + getType().getBoss().getName() + "> " + ChatColor.RESET + "You have failed, Adventurers.");
        for (Player p : getAllPlayers()) {
            GameAPI.teleport(p, TeleportLocation.CYRENNICA.getLocation());

            DungeonManager.removeDungeonItems(p);
        }

        RiftPortal associate = RiftPortal.getPortalFromDungeon(this);
        if (associate != null) {
            associate.removePortals(false);
        }

//        this.remove();
        if (taskID > -1) Bukkit.getScheduler().cancelTask(taskID);
    }

    @Override
    protected void giveDrops() {
        LivingEntity livingEntity = getBoss().getBukkit();

        // Drop the item.
        ItemStack drop = new ItemArmorShield().setTier(ourTier).setRarity(ThreadLocalRandom.current().nextInt(100) > 60 ? Item.ItemRarity.UNIQUE : Item.ItemRarity.RARE).setGlowing(true).generateItem();

        drop.getEnchantments().keySet().forEach(ench -> drop.removeEnchantment(ench));
        EnchantmentAPI.removeGlow(drop);

        // Remove any enchants.
        ItemMeta meta = drop.getItemMeta();
        drop.setItemMeta(meta);


        // Drop the item.
        ItemStack reward = drop;
        livingEntity.getWorld().dropItem(livingEntity.getLocation(), reward);

        // Alert the players.
        List<String> hoveredChat = new ArrayList<>();
        hoveredChat.add(meta.hasDisplayName() ? meta.getDisplayName() : reward.getType().name());
        if (meta.hasLore())
            hoveredChat.addAll(meta.getLore());

        final JSONMessage normal = new JSONMessage(ChatColor.DARK_PURPLE + "The Rift Lurker has dropped: ", ChatColor.DARK_PURPLE);
        normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
        livingEntity.getWorld().getPlayers().forEach(normal::sendToPlayer);

        String partyMembers = "";
        for (Player player : getPlayers().isEmpty() ? getAllPlayers() : getPlayers()) {

            partyMembers += player.getName() + ", ";

            PlayerWrapper.getWrapper(player).addExperience(getType().getXP(), false, true, true);
        }
        final String adventurers = partyMembers.substring(0, partyMembers.length() - 2);
        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + ">> " + Item.ItemTier.getByTier(ourTier).getColor() + ChatColor.BOLD + "The Rift Lurker" + ChatColor.RESET + ChatColor.GOLD + " has been slain by a group of brave adventurers!");
        Bukkit.broadcastMessage(ChatColor.GRAY + "Group: " + adventurers);
    }


    @Override
    public void completeDungeon() {
        if (finished) return;
        finished = true;
        completed = true;
        announce(ChatColor.YELLOW + "You will be teleported out in 15 seconds...");
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> removePlayers(true), 300);
        giveShards();
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), this::giveDrops, 5L);

        for (Map.Entry<Location, Tuple<MaterialData, Long>> entry : blockTypes.entrySet()) {
            Location blockLoc = entry.getKey();
            Tuple<MaterialData, Long> values = entry.getValue();
            MaterialData data = values.a();
            Block b = getWorld().getBlockAt(blockLoc);
            b.setType(data.getItemType());
            b.setData(data.getData());
            blockTypes.remove(blockLoc);
        }

        for (Entity ent : minions) {
            if (ent == null) continue;
            ent.remove();
        }

//        RiftPortal portal = RiftPortal.getPortalFromDungeon(this);
//        if (portal != null) {
//            portal.removePortals(false);
//        }
    }
}
