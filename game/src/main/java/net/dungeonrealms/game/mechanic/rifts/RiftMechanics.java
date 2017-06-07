package net.dungeonrealms.game.mechanic.rifts;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
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
//    private Map<Location, Rift> riftLocations = new HashMap<>();

    private List<Rift> riftLocations = new LinkedList<>();

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

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            if (this.activeRift != null) {
                this.activeRift.onRiftTick();
            }
        }, 20, 20);
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
            this.activeRift.destroy();
        }

        //Get rift, then create.
        this.activeRift = getRandomRift();
        this.activeRift.createRift();
    }

    public Rift getRandomRift() {
        //get random rift.
        return this.riftLocations.get(ThreadLocalRandom.current().nextInt(this.riftLocations.size()));
    }
}
