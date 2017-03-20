package net.dungeonrealms.game.mechanic;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.miscellaneous.Graveyard;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class GraveyardMechanic implements GenericMechanic {

    private static GraveyardMechanic instance;

    private File graveyardFile;
    private FileConfiguration config;


    @Getter
    private List<Graveyard> graveyards = Lists.newArrayList();

    @Override
    @SneakyThrows
    public void startInitialization() {
        instance = this;
        this.graveyardFile = new File(DungeonRealms.getInstance().getDataFolder(), "graveyards.yml");
        if (!this.graveyardFile.exists())
            this.graveyardFile.createNewFile();

        this.config = YamlConfiguration.loadConfiguration(this.graveyardFile);


        ConfigurationSection section = config.getConfigurationSection("graveyards");
        if (section == null) return;

        for (String name : section.getKeys(false)) {
            Location location = Utils.getLocation(section.getString(name + ".location"));
            if (location != null) this.graveyards.add(new Graveyard(name, location));
        }
    }

    public Graveyard getGraveyard(String name) {
        for (Graveyard yard : this.graveyards)
            if (yard.getName().equalsIgnoreCase(name))
            	return yard;
        return null;
    }

    public Graveyard getClosestGraveyard(Location location) {
        Graveyard closest = null;
        double minDistance = -1;
        for (Graveyard yard : this.graveyards) {
            if (yard.getLocation().getWorld() != location.getWorld()) continue;

            double distance = yard.getLocation().distanceSquared(location);
            if (minDistance == -1 || distance < minDistance) {
                closest = yard;
                minDistance = distance;
            }
        }

        return closest;
    }

    public void removeGraveyard(Graveyard yard) {
        this.graveyards.remove(yard);
        this.config.set("graveyards." + yard.getName(), null);
        this.saveGraveyardFile();
    }

    public void addGraveyard(Graveyard yard) {
        this.graveyards.add(yard);
        this.config.set("graveyards." + yard.getName() + ".location", Utils.getStringFromLocation(yard.getLocation(), false));
        this.saveGraveyardFile();
    }

    @SneakyThrows
    public void saveGraveyardFile() {
        this.config.save(graveyardFile);
    }

    @Override
    public void stopInvocation() {
        instance = null;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    public static GraveyardMechanic get() {
        return instance;
    }
}
