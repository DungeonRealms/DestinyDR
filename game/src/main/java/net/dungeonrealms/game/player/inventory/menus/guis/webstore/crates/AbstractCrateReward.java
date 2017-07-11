package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import lombok.Getter;
import net.dungeonrealms.game.item.items.functional.cluescrolls.AbstractClueReward;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

/**
 * Created by Rar349 on 7/7/2017.
 */
@Getter
public abstract class AbstractCrateReward {

    private final MaterialData displayMaterial;
    private final String displayName;
    private final String[] displayLore;

    public AbstractCrateReward(Material displayMaterial, String displayName, String... displayLore) {
        this(displayMaterial, (byte)0, displayName, displayLore);
    }

    public AbstractCrateReward(Material displayMaterial,byte durability, String displayName, String... displayLore) {
        this.displayMaterial = new MaterialData(displayMaterial, durability);
        this.displayName = displayName;
        this.displayLore = displayLore;
    }
    public abstract void giveReward(Player player);

    public abstract boolean canReceiveReward(Player player);
}
