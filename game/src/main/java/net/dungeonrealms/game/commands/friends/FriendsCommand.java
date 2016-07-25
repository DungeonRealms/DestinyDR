package net.dungeonrealms.game.commands.friends;

import io.netty.buffer.Unpooled;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.minecraft.server.v1_9_R2.EnumHand;
import net.minecraft.server.v1_9_R2.PacketDataSerializer;
import net.minecraft.server.v1_9_R2.PacketPlayOutCustomPayload;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by chase on 7/7/2016.
 */
public class FriendsCommand extends BasicCommand {

    public FriendsCommand(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;
        ItemStack book = getFriendsBook(player);
        final ItemStack savedItem = player.getInventory().getItemInMainHand();
        player.getInventory().setItemInMainHand(book);
        PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());
        packetdataserializer.a(EnumHand.MAIN_HAND);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|BOpen", packetdataserializer));
        player.getInventory().setItemInMainHand(savedItem);
        return false;
    }

    private ItemStack getFriendsBook(Player player) {
        ItemStack stack = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bm = (BookMeta) stack.getItemMeta();
        List<String> pages = new ArrayList<>();
        String new_line = "\n" + ChatColor.WHITE.toString() + "`" + "\n";


        int count = 0;
        String nextLine = "\n";
        String friendsPage_string = (ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "   Friends List  " + new_line);
        ArrayList<String> friendsList = FriendHandler.getInstance().getFriendsList(player.getUniqueId());
        for (String uuidString : friendsList) {
            UUID uuid = UUID.fromString(uuidString);
            String playerName = DatabaseAPI.getInstance().getOfflineName(uuid);
            String shard = DatabaseAPI.getInstance().getFormattedShardName(uuid);
            boolean isOnline = (boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, uuid);
            long currentTime = System.currentTimeMillis();
            long endTime = Long.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.LAST_LOGOUT, uuid)));
            long millis = currentTime - endTime;
            long second = (millis / 1000) % 60;
            long minute = (millis / (1000 * 60)) % 60;
            long hour = (millis / (1000 * 60 * 60)) % 24;
            String time = "";

            if (hour > 0) {
                time += hour + "h " + minute + "m " + second + "s ";
            } else if (minute > 0) {
                time += minute + "m " + second + "s ";

            } else {
                time += second + "s ";
            }
            if (hour > 99)
                time = "Many moons.";
            time += nextLine;

            if (playerName.length() >= 15)
                playerName = playerName.substring(0, 15);
            friendsPage_string += (isOnline ? ChatColor.GREEN + ChatColor.BOLD.toString() + "O" : ChatColor.DARK_RED + ChatColor.BOLD.toString() + "O") + ChatColor.BLACK + ChatColor.BOLD.toString() + " " + playerName + nextLine;
            friendsPage_string += (isOnline ? ChatColor.BLACK + "Shard: " + ChatColor.BOLD + shard + nextLine : ChatColor.BLACK + "Last On: " + time);


            count++;
            if (count == 5 || uuidString.equalsIgnoreCase(friendsList.get(friendsList.size() - 1))) {
                count = 0;
                pages.add(friendsPage_string);
                friendsPage_string = (ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "   Friends List  " + new_line);
                if (uuidString.equalsIgnoreCase(friendsList.get(friendsList.size() - 1)))
                    break;
            }
        }

        if (friendsList.isEmpty()) {
            pages.add(friendsPage_string);
        }

        bm.setAuthor("King Bulwar");
        bm.setPages(pages);
        stack.setItemMeta(bm);
        return stack;
    }
}
