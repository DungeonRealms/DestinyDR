package net.dungeonrealms.game.item.items.functional.accessories;

import lombok.Getter;
import net.dungeonrealms.game.world.item.Item;

@Getter
public class EnchantTrinketData extends TrinketData {

    private Item.AttributeType type;
    private int min, max;

    public EnchantTrinketData(Item.AttributeType type, int min, int max) {
        super(null);
        this.type = type;
        this.min = min;
        this.max = max;
    }
}
