package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.ItemGem;
import net.dungeonrealms.game.mastery.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 7/9/2017.
 */
public class GemCrateReward extends AbstractCrateReward {

    public GemCrateReward() {
        super(Material.EMERALD, ChatColor.GREEN + "Gems", ChatColor.GRAY + "The currency of Andalucia");
    }

    @Override
    public void giveReward(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        int maxGemsToGive = wrapper.getLevel() * 2;
        int minGemsToGive = wrapper.getLevel() / 2;
        if(minGemsToGive < 1) minGemsToGive = 1;
        if(maxGemsToGive < 10) minGemsToGive = 10;
        int gemsToGive = Utils.randInt(minGemsToGive, maxGemsToGive);
        GameAPI.giveOrDropItem(player,new ItemGem(gemsToGive).generateItem());
    }

    @Override
    public boolean canReceiveReward(Player player) {
        return true;
    }
}
