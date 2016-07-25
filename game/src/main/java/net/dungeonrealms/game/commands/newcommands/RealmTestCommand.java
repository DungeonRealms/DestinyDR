package net.dungeonrealms.game.commands.newcommands;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.common.game.database.type.EnumOperators;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
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
        GameAPI.handleLogout(p.getUniqueId(), true);
        BungeeUtils.sendToServer(p.getName(), "realms1");
        return true;
    }

}
