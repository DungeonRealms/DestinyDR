package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import org.bukkit.DyeColor;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A basic class that represents all custom DR armor.
 *
 * @author Kneesnap
 */
public class ItemArmor extends CombatItem {

    public final static ItemType[] ARMOR = new ItemType[]{ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET};
    public final static ItemType[] ARMOR_SHIELD = new ItemType[]{ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET, ItemType.SHIELD};

    public ItemArmor() {
        this(ARMOR);
    }

    public ItemArmor(ItemType... type) {
        super(type);
    }

    public ItemArmor(ItemStack item) {
        super(item);
    }

    /**
     * Generates an entire armor set with the parameters of this item.
     */
    public ItemStack[] generateArmorSet(boolean withShield) {
        int desiredMin = Math.max(getMinRarityItems(), 1);
        Item.ItemRarity mRare = getMaxRarity();
        int genned = 0;
        ItemStack[] ret = new ItemStack[withShield ? ARMOR_SHIELD.length : ARMOR.length];
        for (int i = 0; i < (withShield ? ARMOR_SHIELD.length : ARMOR.length); i++) {
            ItemArmor armor = (ItemArmor) new ItemArmor(ARMOR[i]).setTier(getTier()).setGlowing(isGlowing());
            boolean needToForce = mRare != null && genned < desiredMin;
            //If we are forcing a min amount of items, make the items that arent forced random rarities.
            armor.setRarity(needToForce ? mRare : mRare != null && genned >= desiredMin ? Item.ItemRarity.getRandomRarity() : getRarity());
            if (needToForce) genned++;
            ret[i] = armor.generateItem();
        }
        return ret;
    }

    public ItemStack[] generateArmorSet() {
        return generateArmorSet(false);
    }

    @Override
    protected void applyEnchantStats() {
        getAttributes().multiplyStat(ArmorAttributeType.HEALTH_POINTS, 1.05);
        getAttributes().multiplyStat(ArmorAttributeType.HEALTH_REGEN, 1.05);

        if (getAttributes().containsKey(ArmorAttributeType.ENERGY_REGEN))
            getAttributes().addStat(ArmorAttributeType.ENERGY_REGEN, 1);

    }

    @Override
    public ItemStack generateItem() {
//        ItemStack item = super.generateItem();
//        if (getItemType() != null && getItemType() == ItemType.SHIELD) {
//            ItemMeta meta = item.getItemMeta();
//            BlockStateMeta blockMeta = (BlockStateMeta) meta;
//            BlockState state = blockMeta.getBlockState();
//            Banner banner = (Banner) state;
//
//
//            Item.ItemTier tier = getTier();
//
//            if (tier == Item.ItemTier.TIER_2) {
//                banner.setBaseColor(DyeColor.WHITE);
//                banner.addPattern(new Pattern(DyeColor.GRAY, PatternType.BRICKS));
//                banner.addPattern(new Pattern(DyeColor.GRAY, PatternType.getByIdentifier("ss")));
//            } else if (tier == Item.ItemTier.TIER_3) {
//                banner.setBaseColor(DyeColor.WHITE);
//            } else if (tier == Item.ItemTier.TIER_4) {
//                banner.setBaseColor(DyeColor.LIGHT_BLUE);
//            } else if (tier == Item.ItemTier.TIER_5) {
//                banner.setBaseColor(DyeColor.YELLOW);
//            }
//
//            if (tier.getTierId() >= 3 && tier.getTierId() <= 5) {
//                banner.addPattern(new Pattern(DyeColor.GRAY, PatternType.getByIdentifier("gru")));
//            }
//
//            state.update();
//            blockMeta.setBlockState(state);
//            banner.update();
//            item.setItemMeta(blockMeta);
//        }
        return super.generateItem();
    }

    @Override
    protected double getBaseRepairCost() {
        if (getAttributes().hasAttribute(ArmorAttributeType.ARMOR)) {
            return getAttributes().getAttribute(ArmorAttributeType.ARMOR).getMiddle();
        } else if (getAttributes().hasAttribute(ArmorAttributeType.DAMAGE)) {
            return getAttributes().getAttribute(ArmorAttributeType.DAMAGE).getMiddle();
        } else {
            Utils.log.info("Armor did not have either stat for repair cost?");
        }
        return 1000;
    }

    @Override
    protected void onItemBreak(Player player) {
        PlayerWrapper.getWrapper(player).calculateAllAttributes();
    }

    public static boolean isArmor(ItemStack item) {
        return ItemArmorHelmet.isHelmet(item) || ItemArmorChestplate.isChestplate(item)
                || ItemArmorLeggings.isLeggings(item) || ItemArmorBoots.isBoots(item) || ItemArmorShield.isShield(item);
    }
}
