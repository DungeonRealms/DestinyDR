package net.dungeonrealms.game.commands.newcommands;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.network.bungeecord.BungeeUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RealmTestCommand extends BasicCommand {

    public RealmTestCommand(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (!Rank.isDev(p)) {
            return false;
        }
        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.ENTERINGREALM, p.getUniqueId().toString(), true);
        GameAPI.handleLogout(p.getUniqueId());
        BungeeUtils.sendToServer(p.getName(), "realms1");
        return true;
    }

}
