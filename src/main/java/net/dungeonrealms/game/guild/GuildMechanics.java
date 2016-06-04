package net.dungeonrealms.game.guild;

import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class GuildMechanics implements GenericMechanic {

    private static GuildMechanics instance = null;

    public static GuildMechanics getInstance() {
        if (instance == null) {
            instance = new GuildMechanics();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }


    @Override
    public void startInitialization() {


    }

    @Override
    public void stopInvocation() {

    }

    public void doLogin(Player player) {


    }



}
