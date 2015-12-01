package net.dungeonrealms.game.mechanics.generic;

/**
 * Created by Nick on 10/23/2015.
 */
public interface GenericMechanic {

    public EnumPriority startPriority();

    public void startInitialization();

    public void stopInvocation();

}
