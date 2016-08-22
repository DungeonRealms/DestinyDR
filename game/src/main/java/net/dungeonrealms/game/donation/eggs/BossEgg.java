package net.dungeonrealms.game.donation.eggs;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/22/2016
 */

@Getter
public abstract class BossEgg {

    private final String ID, name;
    private final int levelRequirement;

    public BossEgg(String ID, String name, int levelRequirement){
        this.ID = ID;
        this.name = name;
        this.levelRequirement = levelRequirement;
    }

    public abstract ItemStack create();

    public void createInstance() {





    }

}
