package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.ItemEXPLamp;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 7/9/2017.
 */
public class WisdomCrateReward extends AbstractCrateReward {


    public WisdomCrateReward() {
        super(Material.EXP_BOTTLE, ChatColor.GREEN + "Wisdom", ChatColor.GRAY + "EXP in a bottle");
    }

    @Override
    public void giveReward(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        GameAPI.giveOrDropItem(player, new ItemEXPLamp(ItemEXPLamp.ExpType.PLAYER, wrapper.getLevel() * 10).generateItem());
    }

    @Override
    public boolean canReceiveReward(Player player) {
        return true;
    }
}
