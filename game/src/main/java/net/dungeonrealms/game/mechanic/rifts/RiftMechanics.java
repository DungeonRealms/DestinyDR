package net.dungeonrealms.game.mechanic.rifts;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftBeacon;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by iFamasssxD.
 */
public class RiftMechanics implements GenericMechanic, Listener {

    private static RiftMechanics instance;

    public static RiftMechanics getInstance() {
        if (instance == null) instance = new RiftMechanics();
        return instance;
    }
//    private Map<Location, Rift> riftLocations = new HashMap<>();

    private Set<Rift> riftLocations = new HashSet<>();

    //Currently active rift, 1 in a world at a time.
    private Rift activeRift = null;

    private File file;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    private int RESPAWN_TIME = 20 * 60 * 60;

    @Override
    public void startInitialization() {
        this.loadRifts();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {

            //Every hour?
        }, 20, RESPAWN_TIME);
    }

    @SneakyThrows
    private void loadRifts() {
        this.file = new File(DungeonRealms.getInstance().getDataFolder(), "rifts.json");
        if (!this.file.exists())
            this.file.createNewFile();


        JsonReader reader = new JsonReader(new FileReader(this.file));
        this.riftLocations = gson.fromJson(reader, new TypeToken<HashSet<Rift>>() {
        }.getType());
    }

    @SneakyThrows
    private void saveRifts() {
        String string = gson.toJson(riftLocations);

        FileWriter writer = new FileWriter(this.file);
        writer.write(string);
        writer.flush();
        writer.close();
    }

    @Override
    public void stopInvocation() {
        this.saveRifts();
    }

    public void spawnRift() {
        //Spawn this rift, destroying the other one.
        if (this.activeRift != null) {
            this.activeRift.createRift();
        }
    }

    @AllArgsConstructor
    abstract class Rift {

        private Location beaconLocation;

        private int tier;

        private int waves;

        private Item.ElementalAttribute attribute;

        private String nearbyCity;


        private transient List<Entity> spawnedEntities = Lists.newArrayList();

        public void onRiftEnd() {

        }

        public void createRift() {
            Block beaconBlock = beaconLocation.getBlock();
            if (!beaconBlock.getChunk().isLoaded())
                beaconBlock.getChunk().load();

            beaconBlock.setType(Material.BEACON);

            Beacon beacon = (Beacon)beaconBlock.getState();
            CraftBeacon beac = (CraftBeacon)beacon;
//            beac.getTileEntity().setProperty();
        }

        public void destroy() {
            if (!spawnedEntities.isEmpty()) {
                for (Entity ent : spawnedEntities) {
                    ent.remove();
                }
                spawnedEntities.clear();
            }

        }
    }
}
