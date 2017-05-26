package net.dungeonrealms.game.mechanic;

import lombok.AllArgsConstructor;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class RiftMechanics implements GenericMechanic {

    private Map<Location, Rift> riftLocations = new HashMap<>();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void startInitialization() {

    }

    @Override
    public void stopInvocation() {

    }


    @AllArgsConstructor
    class Rift {
        int tier;
        String nearbyCity;
    }
}
