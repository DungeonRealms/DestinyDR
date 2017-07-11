package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 7/7/2017.
 */
public class PurchaseableCrateReward extends AbstractCrateReward {

    private Purchaseables purchaseable;
    private int numberToGive;

    public PurchaseableCrateReward(Purchaseables purchaseables) {
        this(purchaseables,1);
    }

    public PurchaseableCrateReward(Purchaseables purchaseable, int numberToGive) {
        super(purchaseable.getItemType(), (byte)purchaseable.getMeta(),purchaseable.getName(), purchaseable.getDescription().toArray(new String[purchaseable.getDescription().size()]));
        this.purchaseable = purchaseable;
        this.numberToGive = numberToGive;
    }

    @Override
    public void giveReward(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        purchaseable.setNumberOwned(wrapper, purchaseable.getNumberOwned(wrapper) + numberToGive);
    }

    @Override
    public boolean canReceiveReward(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        return !purchaseable.isCanHaveMultiple() ? purchaseable.getNumberOwned(wrapper) == 0 : true;
    }
}
