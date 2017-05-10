package net.dungeonrealms.game.world.shops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class SoldShopItem {

    private UUID sellerUUID;
    private String seller;
    private ItemStack itemSold;
    private double soldPrice;
    private String buyer;
}
