package net.dungeonrealms.old.game.miscellaneous;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class Repair {

    @Getter
    ItemStack item;

    @Getter
    Item repairItem;

    @Getter
    String repairing;

}
