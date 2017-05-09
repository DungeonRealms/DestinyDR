package net.dungeonrealms.game.command.menu;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.item.items.functional.ecash.ItemPet;
import net.dungeonrealms.game.player.menu.CraftingMenu;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPet extends BaseCommand {

    public CommandPet() {
        super("pet", "/<command>", "Opens the player pets menu.", "pets");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        Player player = (Player) sender;
        if (args.length == 0) {
            ItemPet.spawnPet(player);
        } else if (args.length == 1) {
        	CraftingMenu.addPetItem(player);
        }
        
        return true;
    }
}
