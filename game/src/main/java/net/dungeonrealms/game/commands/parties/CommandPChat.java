package net.dungeonrealms.game.commands.parties;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.world.party.Affair;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 11/11/2015.
 */
public class CommandPChat extends BasicCommand {

    public CommandPChat(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (Affair.getInstance().isInParty(player)) {

            if (args.length > 0) {
                StringBuilder message = new StringBuilder();

                for (String rw : args) {
                    message.append(rw).append(" ");
                }

                Affair.AffairO party = Affair.getInstance().getParty(player).get();

                List<Player> everyone = new ArrayList<>();
                {
                    everyone.add(party.getOwner());
                    everyone.addAll(party.getMembers());
                }
                String finalChat = Chat.getInstance().checkForBannedWords(message.toString());


                if (finalChat.contains("@i@") && player.getEquipment().getItemInMainHand() != null && player.getEquipment().getItemInMainHand().getType() != Material.AIR) {
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
                    String prefix = ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + "> " + ChatColor.GRAY + GameChat.getName(player, Rank.getInstance().getRank(player.getUniqueId()), true) + ChatColor.GRAY + ": ";
                    final JSONMessage normal = new JSONMessage(prefix, org.bukkit.ChatColor.WHITE);
                    normal.addText(before + "");
                    normal.addHoverText(hoveredChat, org.bukkit.ChatColor.BOLD + org.bukkit.ChatColor.UNDERLINE.toString() + "SHOW");
                    normal.addText(after);
                    everyone.stream().forEach(normal::sendToPlayer);
                    return true;
                }



                everyone.stream().forEach(player1 -> player1.sendMessage(ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + "> " + ChatColor.GRAY + GameChat.getName(player, Rank.getInstance().getRank(player.getUniqueId()), true) + ChatColor.GRAY + ": " + message.toString()));
            } else {
                player.sendMessage(ChatColor.RED + "Unfinished"); // @todo: toggle <P> chat!
            }

        } else {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
        }

        return false;
    }
}
