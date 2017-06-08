package net.dungeonrealms.game.mechanic.rifts;

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
import net.dungeonrealms.game.mechanic.ReflectionAPI;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.minecraft.server.v1_9_R2.EntityArrow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
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

//    private int RESPAWN_TIME = 20 * 60 * 60;

    private int RESPAWN_TIME = 20 * 60 * 5;

    @Override
    public void startInitialization() {
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        this.loadRifts();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            this.spawnRift();
            //Every hour?
        }, 20 * 10, RESPAWN_TIME);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            if (this.activeRift != null)
                this.activeRift.onRiftTick();
        }, 20, 20);
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
        this.activeRift = getRandomRift();
        this.activeRift.createRift();
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
        if (!portal.isDoneGenerating()) {
            event.setCancelled(true);
        }

        Party party = Affair.getParty(portal.getPortalOwner());
        if (party != null) {
            if (party.isMember(event.getPlayer())) {
                //Let them teleport?
//                EndGateway
                return;
            }
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "You must be in " + portal.getPortalOwner().getName() + "'s Party to enter their Rift!");
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

    public void handleLogout(Player player) {
        RiftPortal active = RiftPortal.getRiftPortal(player);
        if (active != null) {
            //Destroy rift.
            active.removePortals();
        }
    }

    public WorldRift getRandomRift() {
        //get random rift.
        return this.worldRiftLocations.get(ThreadLocalRandom.current().nextInt(this.worldRiftLocations.size()));
    }
}
