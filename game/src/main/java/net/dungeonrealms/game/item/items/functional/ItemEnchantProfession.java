package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.ProfessionAttribute;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ItemEnchantProfession extends ItemEnchantScroll {

    @Getter
    private AttributeList attributes = new AttributeList();

    public ItemEnchantProfession(ItemType type, String enchantType) {
        this(type, enchantType, null);
    }

    public ItemEnchantProfession(ItemType type, String enchantType, ProfessionItem pi) {
        super(null, type, enchantType);
        if (pi != null)
            this.attributes.addStats(pi.getAttributes());
    }

    public ItemEnchantProfession(ItemStack stack) {
        super(stack);
        this.attributes.load(getTag(), applyTo().getType().getAttributeBank().getAttributes());
    }

    @Override
    protected boolean isApplicable(ItemStack item) {
        return isType(item, applyTo());
    }

    @Override
    public void updateItem() {
        getAttributes().save(this);
        super.updateItem();
    }

    @Override
    public void enchant(Player player, ItemGear gear) {
        for (AttributeType at : getAttributes().getAttributes())
            gear.getAttributes().setStat(at, getAttributes().getAttribute(at));
    }

    /**
     * Add a profession attribute to this enchant.
     *
     * @param attr
     */
    public ItemEnchantProfession addEnchant(ProfessionAttribute attr) {
        Item.ItemTier tier = getTier();

        if (tier != null) {
            int minRange = tier.ordinal() - 1;
            int maxRange = tier.ordinal();
            if (maxRange >= attr.getPercentRange().length)
                maxRange = attr.getPercentRange()[attr.getPercentRange().length - 1];

            int current = Utils.randInt(attr.getPercentRange()[minRange], attr.getPercentRange()[maxRange]);
            this.attributes.setStat(attr, attr.getRandomValueFromTier(tier));

            Bukkit.getLogger().info("Adding " + current + " profession from " + minRange + " - " + maxRange);
        }
        return this;
    }

    public ItemEnchantProfession addEnchant(ProfessionAttribute attr, int level) {
        if (level == -1) {
            addEnchant(attr);
            return this;
        }
        this.attributes.setStat(attr, level);
        return this;
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.WHITE + ChatColor.BOLD.toString() + "Scroll: " + ChatColor.YELLOW + this.enchantType + " Enchant";
    }

    @Override
    protected String[] getLore() {
        return arr("Imbues a " + this.enchantType.toLowerCase() + " with special attributes.");
    }

    /**
     * Get a list of possible attribute types.
     */
    protected abstract ItemType applyTo();
}
