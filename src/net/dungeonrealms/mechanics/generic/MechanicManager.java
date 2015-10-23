package net.dungeonrealms.mechanics.generic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 10/23/2015.
 */
public class MechanicManager {

    List<GenericMechanic> Mechanics = new ArrayList<>();

    public void registerMechanic(GenericMechanic genericMechanic) {
        Mechanics.add(genericMechanic);
    }

    /**
     * Method thats called on mechanics startInitialization()
     *
     * @since 1.0
     */
    public void loadMechanics() {
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.POPE).forEach(GenericMechanic::startInitialization);
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.CARDINALS).forEach(GenericMechanic::startInitialization);
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.ARCHBISHOPS).forEach(GenericMechanic::startInitialization);
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.BISHOPS).forEach(GenericMechanic::startInitialization);
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.PRIESTS).forEach(GenericMechanic::startInitialization);
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.CATHOLICS).forEach(GenericMechanic::startInitialization);
    }

    /**
     * Method thats called before the plugin shutsdown!
     *
     * @since 1.0
     */
    public void stopInvocation() {
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.POPE).forEach(GenericMechanic::stopInvocation);
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.CARDINALS).forEach(GenericMechanic::stopInvocation);
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.ARCHBISHOPS).forEach(GenericMechanic::stopInvocation);
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.BISHOPS).forEach(GenericMechanic::stopInvocation);
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.PRIESTS).forEach(GenericMechanic::stopInvocation);
        Mechanics.stream().filter(gm -> gm.startPriority() == EnumPriority.CATHOLICS).forEach(GenericMechanic::stopInvocation);
    }

}

