package net.dungeonrealms.game.item.items.functional.accessories;

import lombok.Getter;
import net.dungeonrealms.game.world.item.Item;
import org.apache.commons.lang.StringUtils;

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


    public String getDisplayPrefix() {
        if (type == Item.ArmorAttributeType.STRENGTH) return "Forceful";
        if (type == Item.ArmorAttributeType.GEM_FIND) return "Golden";
        if (type == Item.ArmorAttributeType.ITEM_FIND) return "Treasured";

        return getType().getDisplayPrefix();
    }

    public String getDisplaySuffix(boolean bold) {
        if (type == Item.ArmorAttributeType.ENERGY_REGEN) return "Agility";
        if (type == Item.WeaponAttributeType.DAMAGE) return "Destruction";
        if (type == Item.ArmorAttributeType.GEM_FIND) return "Pickpocketing";
        if (type == Item.ArmorAttributeType.STRENGTH || type == Item.ArmorAttributeType.VITALITY || type == Item.ArmorAttributeType.INTELLECT || type == Item.ArmorAttributeType.DEXTERITY)
            return StringUtils.capitaliseAllWords(type.getNBTName());

        return getType().getDisplaySuffix(bold);
    }
}
