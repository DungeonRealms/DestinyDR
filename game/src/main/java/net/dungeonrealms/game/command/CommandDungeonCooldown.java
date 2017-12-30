package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.dungeons.DungeonType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandDungeonCooldown  extends BaseCommand {
    public CommandDungeonCooldown() {
        super("dungeoncooldown", "/<command>", "Display player's dungeon cooldowns.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player;
        if(sender instanceof Player) {
            player = (Player) sender;

            DungeonType[] dungeons = DungeonType.values();
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

            for(int i = 0; i < dungeons.length; i++) {
                player.sendMessage(dungeons[i].getDisplayName() + ": " + dungeons[i].getCooldownString(wrapper));
            }

            return true;
        }
        return false;
    }
}
