package net.dungeonrealms.game.command.test;

//This is for test purposes only and the command will be removed in the future

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.item.items.core.CombatItem;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHealerSet extends BaseCommand {
    public CommandHealerSet() {
        super("healerset", "/healerset", "Gives user a set of healer armor");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            if(!Rank.isHeadGM((Player) sender)){return false;}
            CombatItem gear = new ItemArmor();
            gear.setTier(5);
            gear.setTagString("customId", "healer");
            gear.setTagString("setBonus", "healer");
            ((Player) sender).getInventory().addItem(gear.generateItem());
            return true;
        }
        return false;
    }
}