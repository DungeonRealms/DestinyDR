package net.dungeonrealms.game.quests;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.quests.objects.Quest;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created by chase on 7/15/2016.
 */
public class Quests implements GenericMechanic {
    private static final File file = new File(DungeonRealms.getInstance().getDataFolder() + "//quests.yml");
    private static final YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

    private static final CopyOnWriteArrayList<Quest> quests = new CopyOnWriteArrayList<Quest>();

    public static void getQuest(String identifier) {

    }


    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void startInitialization() {
        quests.addAll(yml.getConfigurationSection("quests").getKeys(false).stream().map(Quest::new).collect(Collectors.toList()));

    }

    @Override
    public void stopInvocation() {

    }
}
