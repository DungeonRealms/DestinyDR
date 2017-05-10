package net.dungeonrealms.game.mechanic.generic;

import java.util.ArrayList;
import java.util.List;

public class MechanicManager {

	private static List<GenericMechanic> mechanics = new ArrayList<>();

    public static void registerMechanic(GenericMechanic genericMechanic) {
    	mechanics.add(genericMechanic);
    }

    /**
     * Loads all of our game mechanics.
     */
    public static void loadMechanics() {
    	for (EnumPriority ep : EnumPriority.values())
    		if (ep != EnumPriority.NO_STARTUP)
    			mechanics.stream().filter(gm -> gm.startPriority() == ep).forEach(GenericMechanic::startInitialization);
    }

    /**
     * Shutdown all of our mechanics.
     */
    public static void stopMechanics() {
    	for (EnumPriority ep : EnumPriority.values())
    		mechanics.stream().filter(gm -> gm.startPriority() == ep).forEach(GenericMechanic::stopInvocation);
    	mechanics.clear(); // We don't need to track these anymore, and it prevents the shutdown from being called twice.
    }
}

