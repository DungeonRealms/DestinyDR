package net.dungeonrealms.world;

import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 11/7/2015.
 */
public class Runes implements GenericMechanic, Listener {

    static Runes instance = null;

    public static Runes getInstance() {
        if (instance == null) {
            instance = new Runes();
        }
        return instance;
    }

    public ItemStack getRune(RuneTier tier, RuneType type) {
        return null;
    }

    public enum RuneTier {

    }

    public enum RuneType {
        DAMAGE,
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.BISHOPS;
    }

    @Override
    public void startInitialization() {

    }

    @Override
    public void stopInvocation() {

    }
}
