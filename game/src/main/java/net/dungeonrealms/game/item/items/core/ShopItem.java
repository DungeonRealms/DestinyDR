package net.dungeonrealms.game.item.items.core;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.functional.ItemPeaceOrb;
import net.dungeonrealms.game.item.items.functional.ItemProtectionScroll;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * An item that can be put in a menu shop.
 * <p>
 * Created April 10th, 2017.
 *
 * @author Kneesnap
 */
public class ShopItem extends ItemGeneric {

    @Getter
    @Setter
    private ItemGeneric soldItem;

    @Setter
    @Getter
    private ShardTier shardTier = null;

    @Getter
    private int shards;

    @Getter
    private int eCashCost;

    @Getter
    private ShopItemClick callback;

    protected List<String> originalLore;

    public ShopItem(ItemStack item) {
        super(item.clone());
        setSoldItem(new VanillaItem(getItem()));
        this.originalLore = getItem().getItemMeta().getLore();
    }


    public ShopItem(ItemGeneric purchase) {
        this(purchase, (player, item) -> {
            item.getSoldItem().setAntiDupe(true);
            ItemGeneric toGive = item.getSoldItem();
            if (toGive instanceof ProfessionItem) {
                try {
                    toGive = toGive.getClass().newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (toGive instanceof ItemProtectionScroll) {
                try {
                    toGive = new ItemProtectionScroll(((ItemProtectionScroll) toGive).getTier());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (toGive instanceof ItemPeaceOrb) {
                toGive = new ItemPeaceOrb(true);
            }
//            toGive.setAntiDupe(true);
            GameAPI.giveOrDropItem(player, toGive.generateItem());
            return true;
        });
    }

    public ShopItem(ItemGeneric purchase, ShopItemClick cb) {
        super(ItemType.SHOP);
        this.originalLore = purchase.generateItem().getItemMeta().getLore();
        this.soldItem = purchase;
        this.callback = cb;
        setFlag(ItemFlag.MENU, true);
    }

    public ShopItem setECash(int eCash) {
        this.eCashCost = eCash;
        return this;
    }

    public ShopItem setOnClick(ShopItemClick c) {
        this.callback = c;
        return this;
    }

    @Override
    public void loadItem() {
        setECash(getTagInt("eCash"));
        this.shards = getTagInt("shards");
        if (hasTag("shardTier"))
            setShardTier(ShardTier.getByTier(getTagInt("shardTier")));
        super.loadItem();
    }

    @Override
    public void updateItem() {
        if (this.originalLore != null)
            this.originalLore.forEach(this::addLore);

        if (getECashCost() > 0) {
            addLore(ChatColor.WHITE + "" + getECashCost() + ChatColor.GREEN + " E-Cash");
            setTagInt("eCash", getECashCost());
        }

        if (getShards() > 0) {
            addLore(ChatColor.WHITE + "" + getShards() + getShardTier().getColor() + " Portal Key Shards");
            setTagInt("shardTier", getShardTier().getTier());
            setTagInt("shards", getShards());
        }

        super.updateItem();
    }

    @Override
    protected ItemStack getStack() {
        getSoldItem().setAntiDupe(false);
        return getSoldItem().generateItem();
    }

    public static boolean isShopItem(ItemStack item) {
        return isType(item, ItemType.SHOP);
    }

    public ShopItem setShards(int i, ShardTier byTier) {
        this.shards = i;
        setShardTier(byTier);
        return this;
    }

    public ShopItem setGems(int price) {
        setPrice(price);
        return this;
    }

    public interface ShopItemClick {
        public boolean onClick(Player p, ShopItem shop);
    }
}
