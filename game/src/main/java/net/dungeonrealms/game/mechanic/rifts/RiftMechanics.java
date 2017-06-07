package net.dungeonrealms.game.mechanic.rifts;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.material.MaterialData;

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
    //Currently active rift, 1 in a world at a time.
    private WorldRift activeRift = null;

    private File file;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

//    private int RESPAWN_TIME = 20 * 60 * 60;

    private int RESPAWN_TIME = 20 * 60 * 2;

    @Override
    public void startInitialization() {
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

    public WorldRift getRandomRift() {
        //get random rift.
        return this.worldRiftLocations.get(ThreadLocalRandom.current().nextInt(this.worldRiftLocations.size()));
    }
}
