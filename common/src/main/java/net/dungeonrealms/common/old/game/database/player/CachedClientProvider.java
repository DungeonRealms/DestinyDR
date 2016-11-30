package net.dungeonrealms.common.old.game.database.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/21/2016
 */

public abstract class CachedClientProvider<D> {

    protected Map<UUID, D> PLAYER_DATA_CACHE = new ConcurrentHashMap<>();

    /**
     * @param player Player object
     * @return Player's data
     */
    public D createIfNotExists(Player player) {
        if (!this.PLAYER_DATA_CACHE.containsKey(player.getUniqueId()))
            this.PLAYER_DATA_CACHE.put(player.getUniqueId(), cache(player));
        return this.PLAYER_DATA_CACHE.get(player.getUniqueId());
    }


    public Map<UUID, D> getCache() {
        return PLAYER_DATA_CACHE;
    }

    /**
     * @param uuid UUID
     * @return Player's data
     */
    public D get(UUID uuid) {
        return this.PLAYER_DATA_CACHE.get(uuid);
    }


    /**
     * @param uuid UUID
     * @return If player data is cached
     */
    public boolean isPresent(UUID uuid) {
        return PLAYER_DATA_CACHE.containsKey(uuid);
    }

    /**
     * @param uuid UUID
     */
    public void delete(UUID uuid) {
        if (PLAYER_DATA_CACHE.containsKey(uuid))
            PLAYER_DATA_CACHE.remove(uuid);
    }

    /**
     * @param player Player object
     * @param data   Data type
     */
    public void set(OfflinePlayer player, D data) {
        set(player.getUniqueId(), data);
    }

    /**
     * @param uuid UUID
     * @param data Data type
     */
    private void set(UUID uuid, D data) {
        this.PLAYER_DATA_CACHE.put(uuid, data);
    }

    /**
     * @param player Player
     * @return New player data
     */
    protected abstract D cache(OfflinePlayer player, Object... params);
}

