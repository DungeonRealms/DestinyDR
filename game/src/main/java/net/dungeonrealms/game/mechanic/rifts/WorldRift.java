package net.dungeonrealms.game.mechanic.rifts;

import net.dungeonrealms.game.world.item.Item;
import org.bukkit.Location;

public class WorldRift extends Rift {
    public WorldRift(Location spawn, int tier, Item.ElementalAttribute elementalType, String nearbyCity) {
        super(spawn, tier, elementalType, nearbyCity);
    }

    @Override
    public void onRiftStart() {
        super.onRiftStart();
    }

    @Override
    public int getMaxMobLimit() {
        return tier * 10;
    }

    @Override
    public int getSpawnDelay() {
        return 1;
    }

    @Override
    public void onRiftTick() {

    }
}
