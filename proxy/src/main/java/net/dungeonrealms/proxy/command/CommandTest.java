package net.dungeonrealms.proxy.command;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.data.DataKey;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/25/2016
 */
public class CommandTest extends Command {

    public CommandTest() {
        super("test", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        DataKey<String> key = BTLPDataKeys.ThirdPartyVariableDataKey.builder().id("level").scope(DataKey.Scope.PLAYER).bukkit().build();
        System.out.println(BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayer((ProxiedPlayer) sender).get(key));
    }
}
