package net.dungeonrealms.game.command.menu;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMount;
import net.dungeonrealms.game.player.inventory.menus.guis.MountSelectionGUI;
import net.dungeonrealms.game.player.menu.CraftingMenu;
import net.dungeonrealms.game.world.entity.util.MountUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 29-May-16.
 */
public class CommandMount extends BaseCommand {

    public CommandMount() {
        super("mount", "/<command>", "Opens the player mounts menu.", null, Lists.newArrayList("mounts"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;


        Player player = (Player) sender;

        if(label.equals("mounts")){
            new MountSelectionGUI(player, null).open(player, null);
            return true;
        }

        if (args.length == 0) {
            MountUtils.removeMount(player);
            ItemMount.attemptSummonMount(player, null);
        } else if (args.length == 1) {
        	CraftingMenu.addMountItem(player);
        }
        
        return true;
    }
}
