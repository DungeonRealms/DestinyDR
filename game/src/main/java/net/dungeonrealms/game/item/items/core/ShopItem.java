package net.dungeonrealms.game.item.items.core;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

    public ShopItem(ItemStack item) {
        super(item);
    }

    public ShopItem(ItemGeneric purchase) {
        this(purchase, (player, item) -> {
            item.getSoldItem().setAntiDupe(true);
            GameAPI.giveOrDropItem(player, item.getSoldItem().generateItem());
            return true;
        });
    }

    public ShopItem(ItemGeneric purchase, ShopItemClick cb) {
        super(ItemType.SHOP);
        this.soldItem = purchase;
        this.callback = cb;
        setData(ItemData.MENU, true);
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

    public void setShards(int i, ShardTier byTier) {
        this.shards = i;
        setShardTier(byTier);
    }

    public interface ShopItemClick {
        public boolean onClick(Player p, ShopItem shop);
    }
}
