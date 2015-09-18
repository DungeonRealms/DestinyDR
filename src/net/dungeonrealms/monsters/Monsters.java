package net.dungeonrealms.monsters;

import net.dungeonrealms.mastery.NMSUtils;
import net.dungeonrealms.monsters.entities.EntityPirate;
import net.minecraft.server.v1_8_R3.EntityZombie;

/**
 * Created by Nick on 9/17/2015.
 */
public class Monsters {

    static Monsters instance = null;

    public static Monsters getInstance() {
        if (instance == null) {
            return new Monsters();
        }
        return instance;
    }

    public void startInitialization() {
        NMSUtils nmsUtils = new NMSUtils();

        nmsUtils.registerEntity("Pirate", 54, EntityZombie.class, EntityPirate.class);
    }

}
