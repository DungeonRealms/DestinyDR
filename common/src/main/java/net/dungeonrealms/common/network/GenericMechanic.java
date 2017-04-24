package net.dungeonrealms.common.network;

/**
 * Created by Nick on 10/23/2015.
 */
public interface GenericMechanic {

    EnumPriority startPriority();

    void startInitialization();

    void stopInvocation();

}
