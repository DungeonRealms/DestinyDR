package net.dungeonrealms.old.game.combat;

import net.dungeonrealms.old.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.old.game.mechanic.generic.GenericMechanic;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Matthew E on 10/29/2016 at 12:42 PM.
 */
public class CombatLogMechanics implements GenericMechanic {

    public ConcurrentHashMap<UUID, LoggedNPC> loggedMap;

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {
        this.loggedMap = new ConcurrentHashMap<>();
    }

    @Override
    public void stopInvocation() {

    }
}
