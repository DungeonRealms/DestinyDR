package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.player.altars.items.recipeitems.ItemHemitite;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRecipeItem extends BaseCommand {

    public CommandRecipeItem() {
        super("recipeitem", "/recipeitem <item>", "Recipe Item Test");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player){
            Player player = (Player) sender;
            player.getInventory().addItem(new ItemHemitite().generateItem());
            return true;
        }
            return false;
    }
}
