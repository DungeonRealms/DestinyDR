package net.dungeonrealms.game.world.entities.utils;

import net.dungeonrealms.game.world.entities.Entities;
import net.minecraft.server.v1_8_R3.Entity;

import java.util.UUID;

/**
 * Created by Kieran on 9/19/2015.
 */
public class EntityAPI {

    public static boolean hasPetOut(UUID uuid) {
        return Entities.PLAYER_PETS.containsKey(uuid);
    }

    public static boolean hasMountOut(UUID uuid) {
        return Entities.PLAYER_MOUNTS.containsKey(uuid);
    }

    public static Entity getPlayerPet(UUID uuid) {
        return Entities.PLAYER_PETS.get(uuid);
    }

    public static Entity getPlayerMount(UUID uuid) {
        return Entities.PLAYER_MOUNTS.get(uuid);
    }

    public static void removePlayerPetList(UUID uuid) {
        if (Entities.PLAYER_PETS.containsKey(uuid)) {
            Entities.PLAYER_PETS.remove(uuid);
        }
    }

    public static void removePlayerMountList(UUID uuid) {
        if (Entities.PLAYER_MOUNTS.containsKey(uuid)) {
            Entities.PLAYER_MOUNTS.remove(uuid);
        }
    }

    public static void addPlayerPetList(UUID uuid, Entity entity) {
        Entities.PLAYER_PETS.put(uuid, entity);
    }

    public static void addPlayerMountList(UUID uuid, Entity entity) {
        Entities.PLAYER_MOUNTS.put(uuid, entity);
    }

}
