package xyz.dungeonrealms.mechanics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 12/10/2015.
 */
public class MechanicManager {

    List<DRMechanic> mechanicList = new ArrayList<>();

    public void registerMechanic(DRMechanic mechanic) {
        mechanicList.add(mechanic);
    }

    public void loadMechanics() {
        mechanicList.stream().forEach(DRMechanic::onStart);
    }

}
