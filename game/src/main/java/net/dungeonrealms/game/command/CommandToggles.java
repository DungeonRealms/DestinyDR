package net.dungeonrealms.game.command;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.player.inventory.menus.guis.TogglesGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandToggles extends BaseCommand {
    public CommandToggles() {
        super("toggles", "/<command> [args]", "View and manage your profile toggles.", null, Lists.newArrayList("toggle", "gm"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            new TogglesGUI(player, null).open(player, null);
        }
        return true;
    }
}
