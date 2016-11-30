package net.dungeonrealms.old.game.world.entity.type.monster.boss;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/22/2016
 */

public interface WorldBoss extends Boss {

    /**
     * Return's current phase
     *
     * @return Current phase
     */
    int getPhase();

    /**
     * Switch boss phase
     *
     * @param phase
     */
    void switchPhase(int phase);

}
