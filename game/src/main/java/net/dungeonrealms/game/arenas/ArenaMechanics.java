package net.dungeonrealms.game.arenas;

import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/30/2016
 */
public class ArenaMechanics implements GenericMechanic {

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {

    }

    @Override
    public void stopInvocation() {

    }
}
