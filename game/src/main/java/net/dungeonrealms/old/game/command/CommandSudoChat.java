package net.dungeonrealms.old.game.command;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.common.old.game.database.player.rank.Rank;
import net.dungeonrealms.old.game.player.chat.Chat;
import net.dungeonrealms.old.game.player.chat.GameChat;
import net.dungeonrealms.old.game.player.json.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/23/2016
 */
public class CommandSudoChat extends BaseCommand {
    public CommandSudoChat(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isDev(player)) return false;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Invalid usage! /sudochat <name> <type> <message>");
            return false;
        }


        if (Bukkit.getPlayer(args[0]) == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not online.");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        StringBuilder message = new StringBuilder(args[2]);
        for (int arg = 3; arg < args.length; arg++) message.append(" ").append(args[arg]);


        switch (args[1]) {
            case "global":
                String finalChat = Chat.getInstance().checkForBannedWords(message.toString());

                StringBuilder prefix = new StringBuilder();
                prefix.append(GameChat.getPreMessage(player, true, GameChat.getGlobalType(finalChat)));

                if (finalChat.contains("@i@") && player.getEquipment().getItemInMainHand() != null && player.getEquipment().getItemInMainHand().getType() != Material.AIR) {
                    String aprefix = prefix.toString();
                    String[] split = finalChat.split("@i@");
                    String after = "";
                    String before = "";
                    if (split.length > 0)
                        before = split[0];
                    if (split.length > 1)
                        after = split[1];


                    ItemStack stack = player.getInventory().getItemInMainHand();

                    List<String> hoveredChat = new ArrayList<>();
                    ItemMeta meta = stack.getItemMeta();
                    hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : stack.getType().name()));
                    if (meta.hasLore())
                        hoveredChat.addAll(meta.getLore());
                    final JSONMessage normal = new JSONMessage(ChatColor.WHITE + aprefix, ChatColor.WHITE);
                    normal.addText(before + "");
                    normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
                    normal.addText(after);
                    Bukkit.getOnlinePlayers().forEach(normal::sendToPlayer);
                    return true;
                }

                Bukkit.getOnlinePlayers().forEach(newPlayer -> newPlayer.sendMessage(prefix.toString() + finalChat));
                break;

            case "local":
                player.chat(message.toString());
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Invalid type.");
                break;
        }


        return true;
    }
}
