package xyz.dungeonrealms.mechanics.world;

/**
 * Created by Nick on 12/11/2015.
 */
public interface Playable {

    public String getIdentifier();

    public default long getExistenceTime() {
        return System.currentTimeMillis() / 1000l;
    }

}
