package net.dungeonrealms.old.game.tab;

import codecrafter47.bungeetablistplus.api.bukkit.BungeeTabListPlusBukkitAPI;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.old.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.old.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.old.game.tab.column.CharacterTabColumn;
import net.dungeonrealms.old.game.tab.column.FriendTabColumn;
import net.dungeonrealms.old.game.tab.column.GuildTabColumn;
import net.dungeonrealms.old.game.tab.column.StatisticsTabColumn;

import java.util.HashSet;
import java.util.Set;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/4/2016
 */
public class TabMechanics implements GenericMechanic {

    // INSTANCE //
    protected static TabMechanics instance = null;

    private Set<Column> COLUMNS = new HashSet<>();

    private final long TRANSITION_TIME = 2000L;

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.BISHOPS;
    }

    public static TabMechanics getInstance() {
        if (instance == null) {
            instance = new TabMechanics();
        }
        return instance;
    }

    @Override
    public void startInitialization() {
        Constants.log.info("Registering all tab variables for the BungeeCord tab");

        // REGISTER COLUMNS HERE //
        COLUMNS.add(new CharacterTabColumn().register());
        COLUMNS.add(new GuildTabColumn().register());
        COLUMNS.add(new FriendTabColumn().register());
        COLUMNS.add(new StatisticsTabColumn().register());

        COLUMNS.forEach(col ->
                col.getVariablesToRegister().forEach(var -> BungeeTabListPlusBukkitAPI.registerVariable(DungeonRealms.getInstance(), var)));
    }

    @Override
    public void stopInvocation() {

    }

}
