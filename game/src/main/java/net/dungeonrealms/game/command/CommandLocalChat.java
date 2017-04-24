package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Brad on 08/06/2015.
 */
public class CommandLocalChat extends BaseCommand {

    public CommandLocalChat(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof ConsoleCommandSender) return false;

        if (args.length <= 0) {
            sender.sendMessage(ChatColor.RED + "/l <message>");
            return true;
        }

        StringBuilder chatMessage = new StringBuilder();

        for (String arg : args) {
            chatMessage.append(arg).append(" ");
        }

        String finalChat = Chat.getInstance().checkForBannedWords(chatMessage.toString());

        if(Chat.containsIllegal(chatMessage.toString())){
            sender.sendMessage(ChatColor.RED + "Your message contains illegal characters.");
            return true;
        }
        Player player = (Player) sender;

        if (finalChat.contains(".com") || finalChat.contains(".net") || finalChat.contains(".org") || finalChat.contains("http://") || finalChat.contains("www."))
            if (!Rank.isDev(player)) {
                player.sendMessage(ChatColor.RED + "No " + ChatColor.UNDERLINE + "URL's" + ChatColor.RED + " in chat!");
                return true;
            }


        UUID uuid = player.getUniqueId();

        StringBuilder prefix = new StringBuilder();

        prefix.append(GameChat.getPreMessage(player, true, "local"));

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

            GameAPI.getNearbyPlayers(player.getLocation(), 75).forEach(normal::sendToPlayer);
            return true;
        }

        if (GameAPI.getNearbyPlayers(player.getLocation(), 75).size() >= 2) {
            GameAPI.getNearbyPlayers(player.getLocation(), 75).forEach(otherPlayer -> otherPlayer.sendMessage(GameChat.getPreMessage(player, false, "local") + finalChat));
        } else {
            player.sendMessage(GameChat.getPreMessage(player, false, "local") + finalChat);
            player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "No one heard you...");
        }
        return true;
    }
}
