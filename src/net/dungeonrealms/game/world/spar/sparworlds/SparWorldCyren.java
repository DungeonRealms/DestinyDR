package net.dungeonrealms.game.world.spar.sparworlds;

/**
 * Created by Nick on 12/15/2015.
 */
public class SparWorldCyren implements SparWorld {

    @Override
    public String getName() {
        return "CYREN";
    }

    @Override
    public int[] getLocations() {
        return new int[]{
                366,37,1325,
                366,37,1359
        };
    }
}
