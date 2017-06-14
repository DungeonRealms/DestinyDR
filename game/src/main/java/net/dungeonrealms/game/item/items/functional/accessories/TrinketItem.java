package net.dungeonrealms.game.item.items.functional.accessories;

import lombok.Getter;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import org.bukkit.inventory.ItemStack;

public class TrinketItem extends FunctionalItem {

    @Getter
    public Integer value;

    @Getter
    private Trinket trinket;

    @Getter
    private TrinketType trinketType;

    public TrinketItem(ItemStack item) {
        super(item);
        if (hasTag("value"))
            this.value = getTagInt("value");

        if (hasTag("trinket"))
            this.trinket = Trinket.getFromName(getTagString("trinket"));

        if (hasTag("trinketType"))
            this.trinketType = TrinketType.valueOf(getTagString("trinketType"));
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(trinketType.getMaterial());
    }

    public TrinketItem(TrinketType trinketType) {
        super(ItemType.TRINKET);

        this.trinketType = trinketType;
        this.trinket = trinketType.getRandomTrinket();
        this.value = this.trinket.getValue();
    }

    public TrinketItem(TrinketType trinketType, Trinket trinket) {
        super(ItemType.TRINKET);

        this.trinketType = trinketType;
        this.trinket = trinket;
        this.value = this.trinket.getValue();
    }

    @Override
    public void updateItem() {
        if (value != null) {
            setTagInt("value", value);

        }
        if (trinket != null)
            setTagString("trinket", trinket.name());

        if (trinketType != null)
            setTagString("trinketType", trinketType.name());

        super.updateItem();
    }

    @Override
    public String getDisplayName() {
        String name = trinketType.getName();
        String prefix = trinket.getPrefix();
        if (prefix != null) {
            name = prefix + " " + name;
        }

        if (trinket.getSuffix() != null)
            name = name + " " + trinket.getSuffix();

        return trinketType.getNameColor() + name;
    }

    @Override
    protected String[] getLore() {
        String line;
        if (getTrinket().getData() instanceof EnchantTrinketData && value != null) {
            EnchantTrinketData data = (EnchantTrinketData) getTrinket().getData();
            line = ChatColor.RED.toString() + data.getType().getPrefix() + getValue() + "%";
        } else {
            line = ChatColor.GRAY + ChatColor.ITALIC.toString() + getTrinket().getData().getDescription();
        }
        return new String[]{line,
                "",
                ChatColor.GRAY + "Usable only in Trinket Slot"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[]{};
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrinketItem) {
            TrinketItem item = (TrinketItem) obj;
            if (getTrinket().equals(item.getTrinket()) && (getValue() != null && getValue().equals(item.getValue()) || getValue() == null && item.getValue() == null) && trinketType.equals(item.getTrinketType())) {
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return getTrinket().name() + " Val: " + getValue() + " Type: " + trinketType.name();
    }
}
