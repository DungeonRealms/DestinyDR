package net.dungeonrealms.game.tab;

import codecrafter47.bungeetablistplus.api.bukkit.BungeeTabListPlusBukkitAPI;
import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.tab.column.CharacterTabColumn;
import net.dungeonrealms.game.tab.column.FriendTabColumn;
import net.dungeonrealms.game.tab.column.GuildTabColumn;
import net.dungeonrealms.game.tab.column.StatisticsTabColumn;

import java.util.HashSet;
import java.util.Set;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/4/2016
 */
public class TabMechanics implements GenericMechanic {

    // INSTANCE //
	@Getter private static TabMechanics instance = new TabMechanics();

    private Set<Column> COLUMNS = new HashSet<>();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.BISHOPS;
    }

    @Override @SneakyThrows
    public void startInitialization() {
        Constants.log.info("Registering all tab variables for the BungeeCord tab");

        // REGISTER COLUMNS HERE //
        COLUMNS.add(new CharacterTabColumn().register());
        COLUMNS.add(new GuildTabColumn().register());
        COLUMNS.add(new FriendTabColumn().register());
        COLUMNS.add(new StatisticsTabColumn().register());

        for (Column c : COLUMNS)
            for (Variable v : c.getVariablesToRegister())
                BungeeTabListPlusBukkitAPI.registerVariable(DungeonRealms.getInstance(), v);
    }

    @Override
    public void stopInvocation() {

    }

}
