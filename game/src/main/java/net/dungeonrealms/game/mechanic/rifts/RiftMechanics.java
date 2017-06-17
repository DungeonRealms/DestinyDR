package net.dungeonrealms.game.mechanic.rifts;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.functional.ItemRiftCrystal;
import net.dungeonrealms.game.item.items.functional.ItemRiftFragment;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mechanic.ReflectionAPI;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.item.Item;
import net.minecraft.server.v1_9_R2.EntityArrow;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by iFamasssxD.
 */
public class RiftMechanics implements GenericMechanic, Listener {

    private static RiftMechanics instance;

    public static RiftMechanics getInstance() {
        if (instance == null) instance = new RiftMechanics();
        return instance;
    }
//    private Map<Location, Rift> worldRiftLocations = new HashMap<>();

    @Getter
    private List<WorldRift> worldRiftLocations = new LinkedList<>();

    @Getter
    @Setter
    private WorldRift activeRift = null; //Currently active rift, 1 in a world at a time.

    private File file;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    private int RESPAWN_TIME = 60 * 60;

    //1 Hour
//    private int RESPAWN_TIME = 5 * 60 + 20;

    @Getter
    @Setter
    private int spawnTimer = RESPAWN_TIME;

    private WorldRift nextRift;

    @Override
    public void startInitialization() {
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        this.loadRifts();
        this.nextRift = getRandomRift();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {

            if (spawnTimer <= 0) {
                this.spawnTimer = RESPAWN_TIME;
                this.spawnRift();
            } else {
                spawnTimer--;

                if (spawnTimer == 60 * 5 && nextRift != null) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (Trinket.hasActiveTrinket(player, Trinket.UPCOMING_RIFT, true)) {
                            Bukkit.getLogger().info("Sending alert to " + player.getName() + " 5minutes early for next rift..");
                            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 1.4F);
                            player.sendMessage("");
                            player.sendMessage(ChatColor.GRAY + "You feel an unworldly presence near " + ChatColor.LIGHT_PURPLE + this.nextRift.getNearbyCity() + ChatColor.GRAY + "...");
                            player.sendMessage("");
                        }
                    }
                }
            }
            //Every hour?
        }, 20, 20);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            if (this.activeRift != null)
                this.activeRift.onRiftTick();

        }, 20, 20);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (RiftPortal portal : RiftPortal.getRiftPortalMap().values())
                portal.onUpdate();
        }, 20, 5);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (Player pl : GameAPI.asyncTracker) {
                //Alive for alteast 10 seconds?
                if (pl.isOnline() && pl.getTicksLived() >= 20 * 10) {
                    for (int i = 0; i < pl.getInventory().getSize(); i++) {
                        ItemStack item = pl.getInventory().getItem(i);
                        //Should be a nono, but I want this to be fast so pls dun change
                        if (item != null && item.getType() != Material.AIR && item.getType() == Material.PRISMARINE_SHARD && item.getAmount() >= ItemRiftFragment.RIFT_COST) {
                            int newFragment = item.getAmount() - ItemRiftFragment.RIFT_COST;

                            PersistentItem persis = PersistentItem.constructItem(item);
                            if (persis != null && persis instanceof ItemRiftFragment) {
                                ItemRiftFragment fragment = (ItemRiftFragment) persis;

                                ItemRiftCrystal newCrystal = new ItemRiftCrystal(fragment.getFragmentTier(), 1);
                                if (newFragment <= 0) {
                                    //Just set this slot to the new one?
                                    pl.getInventory().setItem(i, newCrystal.generateItem());
                                } else {
                                    item.setAmount(newFragment);
                                    //Add
                                    GameAPI.giveOrDropItem(pl, newCrystal.generateItem());
                                }

                                pl.updateInventory();
//                                pl.playSound(pl.getLocation(), Sound.BLOCK_ANVIL_USE, .5F, .8F);
                                pl.playSound(pl.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 1.1F);
                                pl.playSound(pl.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1, .4F);
                                pl.playSound(pl.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1, 1.1F);
                                pl.sendMessage(fragment.getFragmentTier().getColor().toString() + ChatColor.BOLD + "* Your bag rustles as your Rift Fragments combine *");
                            }
                        }
                    }
                }
            }
        }, 20, 20 * 3);
    }

    @SneakyThrows
    private void loadRifts() {
        this.file = new File(DungeonRealms.getInstance().getDataFolder(), "rifts.json");
        if (!this.file.exists())
            this.file.createNewFile();


        JsonReader reader = new JsonReader(new FileReader(this.file));
        this.worldRiftLocations = gson.fromJson(reader, new TypeToken<LinkedList<WorldRift>>() {
        }.getType());
        if (this.worldRiftLocations == null) {
            this.worldRiftLocations = new LinkedList<>();
        }
    }

    @SneakyThrows
    public void saveRifts() {
        if (worldRiftLocations != null) {
            String string = gson.toJson(worldRiftLocations, new TypeToken<LinkedList<WorldRift>>() {
            }.getType());

            if (this.file == null) {
                Bukkit.getLogger().info("File is null!!!!: " + string);
                return;
            }
            FileWriter writer = new FileWriter(this.file);
            writer.write(string);
            writer.flush();
            writer.close();
        }
    }

    @Override
    public void stopInvocation() {
        if (this.activeRift != null) {
            this.activeRift.destroy();
        }
        this.saveRifts();
    }

    public void spawnRift() {
        //Spawn this rift, destroying the other one.
        if (this.activeRift != null) {
            this.activeRift.destroy();
        }

        //Get rift, then create.
        this.activeRift = this.nextRift != null ? this.nextRift : getRandomRift();
        this.activeRift.createRift();
        this.nextRift = getRandomRift();
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (GameAPI.isMainWorld(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHandle(PlayerPortalEvent event) {
        //Wut?
        if (event.getFrom() == null || !GameAPI.isMainWorld(event.getFrom().getWorld())) return;

        RiftPortal portal = RiftPortal.getRiftPortalFromBlock(event.getFrom().getBlock());
        if (portal == null) return;
        event.setCancelled(true);
        handlePortalTeleport(event.getPlayer(), portal, event.getFrom());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLand(ProjectileHitEvent event) {
        Block block = event.getEntity().getLocation().getBlock();
        if (event.getEntity() instanceof Arrow) {
            EntityArrow arrow = ((CraftArrow) event.getEntity()).getHandle();
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                if (arrow.getBukkitEntity().isDead()) return;

                int x = (int) ReflectionAPI.getObjectFromField("h", EntityArrow.class, arrow), y = (int) ReflectionAPI.getObjectFromField("at", EntityArrow.class, arrow), z = (int) ReflectionAPI.getObjectFromField("au", EntityArrow.class, arrow);
                if (x == -1 && y == -1 && z == -1) return;
                int id = block.getWorld().getBlockTypeIdAt(x, y, z);
                if (id == Material.END_GATEWAY.getId() || id == Material.OBSIDIAN.getId()) {
                    arrow.getBukkitEntity().remove();
                }
            }, 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_GATEWAY) {
            event.setCancelled(true);
            if (event.getFrom() == null || !GameAPI.isMainWorld(event.getFrom().getWorld())) return;

            Block block = event.getTo().getBlock();
            //Use the bottom block.
            if (block.getRelative(BlockFace.DOWN).getType() == Material.END_GATEWAY)
                block = block.getRelative(BlockFace.DOWN);

            RiftPortal portal = RiftPortal.getRiftPortalFromBlock(block);
            if (portal == null) {
                return;
            }

            handlePortalTeleport(event.getPlayer(), portal, block.getLocation());
        }
    }

    private void handlePortalTeleport(Player player, RiftPortal portal, Location from) {
        Party party = Affair.getParty(portal.getPortalOwner());

        if (portal.isDoneGenerating()) {
            if (party != null) {
                if (party.isMember(player)) {
                    portal.handlePortalUse(player, from.getBlock());
                    return;
                }
            } else if (player.equals(portal.getPortalOwner())) {
                Affair.createParty(portal.getPortalOwner());
                portal.handlePortalUse(player, from.getBlock());
                return;
            }
        } else {
            player.sendMessage(ChatColor.RED + "Please wait for the rift to fully open!");
            player.setVelocity(new Vector(1, 0, 0));
        }
        player.sendMessage(ChatColor.RED + "You must be in " + portal.getPortalOwner().getName() + "'s Party to enter their Rift!");
        player.setVelocity(new Vector(1, 0, 0));

    }

    public void handleLogout(Player player) {
        RiftPortal active = RiftPortal.getRiftPortal(player);
        if (active != null) {
            //Destroy rift.
            active.removePortals(false);
        }
    }

    @EventHandler
    public void worldChangeEvent(PlayerChangedWorldEvent event) {
        GameAPI.addCooldown(event.getPlayer(), MetadataUtils.Metadata.WORLD_CHANGE, 2);
//        MetadataUtils.Metadata.WORLD_CHANGE.set(event.getPlayer(), System.currentTimeMillis());
    }

    public WorldRift getRandomRift() {
        Map<Item.ItemTier, List<WorldRift>> riftMap = new HashMap<>();
        for (WorldRift rift : worldRiftLocations) {
            Item.ItemTier tier = Item.ItemTier.getByTier(rift.getTier());
            List<WorldRift> current = riftMap.computeIfAbsent(tier, l -> Lists.newArrayList());
            current.add(rift);
        }

        Random r = ThreadLocalRandom.current();
        int random = r.nextInt(1000);
        int tierChosen;
        if (random < 300 && riftMap.containsKey(Item.ItemTier.TIER_1)) {
            tierChosen = 1;
        } else if (random <= 500 && riftMap.containsKey(Item.ItemTier.TIER_2)) {
            tierChosen = 2;
        } else if (random <= 700 && riftMap.containsKey(Item.ItemTier.TIER_3)) {
            tierChosen = 3;
        } else if (random <= 800 && riftMap.containsKey(Item.ItemTier.TIER_4)) {
            tierChosen = 4;
        } else if (random >= 900 && riftMap.containsKey(Item.ItemTier.TIER_5)) {
            tierChosen = 5;
        } else {
            tierChosen = 1;
        }

        List<WorldRift> available = riftMap.get(Item.ItemTier.getByTier(tierChosen));
        if (available.size() <= 0) {
            Bukkit.getLogger().info("Unable to find any rifts for tier " + tierChosen);
            return getRandomRift();
        }

        Bukkit.getLogger().info("Found valid rifts for tier: " + tierChosen + "(" + random + " chosen)");
//        int tierChosen = r.nextInt(4) == 0 && riftMap.containsKey(Item.ItemTier.TIER_5) ? 5 : r.nextInt(4) == 0 && riftMap.containsKey(Item.ItemTier.TIER_4) ? ;
        //get random rift.
        return available.get(r.nextInt(available.size()));
    }
}
