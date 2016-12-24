package net.dungeonrealms.proxy.command;

import net.dungeonrealms.common.Constants;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;

public class CommandAlert
        extends Command {

    public CommandAlert() {
        super("alert");
    }

    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer && !Arrays.asList(Constants.DEVELOPERS).contains(sender.getName())) return;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must add a message!");
        } else {
            StringBuilder builder = new StringBuilder();
            // Remove &h
            if (args[0].startsWith("&h")) args[0] = args[0].substring(2, args[0].length());
            else builder.append(ChatColor.translateAlternateColorCodes('&', "&7>> &6DungeonRealms " + ChatColor.RED));

            for (String s : args) {
                builder.append(ChatColor.translateAlternateColorCodes('&', s));
                builder.append(" ");
            }

            String chatMessage = builder.substring(0, builder.length() - 1);

            ProxyServer.getInstance().broadcast(chatMessage);

            StringBuilder titleBuilder = new StringBuilder();

            String[] arrayOfString;
            int j = (arrayOfString = args).length;
            for (int i = 0; i < j; i++) {
                String s = arrayOfString[i];
                titleBuilder.append(ChatColor.translateAlternateColorCodes('&', s));
                titleBuilder.append(" ");
            }

            String titleMessage = titleBuilder.substring(0, titleBuilder.length() - 1);

            Title title = ProxyServer.getInstance().createTitle();

            TextComponent titleComp = new TextComponent("Dungeon Realms");
            titleComp.setBold(true);
            titleComp.setColor(ChatColor.GOLD);

            TextComponent titleSubComp = new TextComponent(titleMessage);
            titleSubComp.setColor(ChatColor.RED);

            title.title(titleComp);
            title.subTitle(titleSubComp);

            title.fadeIn(40);
            title.stay(100);
            title.fadeOut(40);

            for (ProxiedPlayer proxplayer : ProxyServer.getInstance().getPlayers()) proxplayer.sendTitle(title);
        }
    }
}