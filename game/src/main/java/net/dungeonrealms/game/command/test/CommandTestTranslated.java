package net.dungeonrealms.game.command.test;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.functional.cluescrolls.ClueScrollItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 6/14/2017.
 */
public class CommandTestTranslated extends BaseCommand {

    public CommandTestTranslated() {
        super("translate");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if(!Rank.isDev(player)) return true;
        ItemStack stack = player.getEquipment().getItemInMainHand();
        if(stack == null || !stack.getType().equals(Material.MAP)) return true;
        if(!PersistentItem.isType(stack, ItemType.CLUE_SCROLL)) return true;
        ClueScrollItem item = new ClueScrollItem(stack);
        item.setTranslated(true);
        player.getEquipment().setItemInMainHand(item.generateItem());
        return true;
    }
}
