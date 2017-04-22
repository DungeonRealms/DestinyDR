package net.dungeonrealms.game.command.toggle;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to toggle glow.
 * 
 * Created on March 11th, 2017
 * @author Kneesnap
 */

public class CommandToggleGlow extends BaseCommand {

    public CommandToggleGlow() {
        super("toggleglow", "/<command>", "Toggles item rarity glow.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return false;

        wrapper.getToggles().setGlow(!wrapper.getToggles().isGlow());

        return true;
    }

}